package cn.weitee.erp.module.erp.service.sale;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.number.MoneyUtils;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.erp.controller.admin.sale.vo.out.ErpSaleOutPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.sale.vo.out.ErpSaleOutSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductDO;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpSaleOrderDO;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpSaleOutDO;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpSaleOutItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockDO;
import cn.weitee.erp.module.erp.dal.mysql.sale.ErpSaleOutItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.sale.ErpSaleOutMapper;
import cn.weitee.erp.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.enums.common.ErpBizTypeEnum;
import cn.weitee.erp.module.erp.enums.stock.ErpStockRecordBizTypeEnum;
import cn.weitee.erp.module.erp.service.finance.ErpAccountService;
import cn.weitee.erp.module.erp.service.finance.ErpArStatementService;
import cn.weitee.erp.module.erp.service.finance.ErpFinanceBizHookService;
import cn.weitee.erp.module.erp.service.product.ErpProductService;
import cn.weitee.erp.module.erp.service.product.ErpProductUnitConversionService;
import cn.weitee.erp.module.erp.service.stock.ErpStockBatchAllocationService;
import cn.weitee.erp.module.erp.service.stock.ErpStockRecordService;
import cn.weitee.erp.module.erp.service.stock.ErpStockService;
import cn.weitee.erp.module.erp.service.stock.bo.ErpStockBatchAllocateOutboundReqBO;
import cn.weitee.erp.module.erp.service.stock.bo.ErpStockRecordCreateReqBO;
import cn.weitee.erp.module.erp.service.project.ErpProjectLifecycleService;
import cn.weitee.erp.module.erp.service.project.event.ProjectLifecycleRefreshEvent;
import cn.weitee.erp.module.erp.util.ErpTransactionUtils;
import cn.weitee.erp.module.system.api.user.AdminUserApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.*;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.*;

// TODO 芋艿：记录操作日志

/**
 * ERP 销售出库 Service 实现类
 *
 * @author WeTai
 */
@Service
@Validated
@Slf4j
public class ErpSaleOutServiceImpl implements ErpSaleOutService {

    @Resource
    private ErpSaleOutMapper erpSaleOutMapper;
    @Resource
    private ErpSaleOutItemMapper erpSaleOutItemMapper;

    @Resource
    private ErpNoRedisDAO noRedisDAO;

    @Resource
    private ErpProductService productService;
    @Resource
    private ErpProductUnitConversionService unitConversionService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private ErpSaleOrderService saleOrderService;
    @Resource
    private ErpAccountService accountService;
    @Resource
    private ErpStockRecordService stockRecordService;
    @Resource
    private ErpStockService stockService;
    @Resource
    private ErpStockBatchAllocationService stockBatchAllocationService;
    @Resource
    @Lazy
    private ErpFinanceBizHookService financeBizHookService;
    @Resource
    @Lazy
    private ErpArStatementService arStatementService;
    @Resource
    private ErpProjectLifecycleService projectLifecycleService;
    @Resource
    private ApplicationEventPublisher eventPublisher;

    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private PlatformTransactionManager transactionManager;

    @Override
    public Long createSaleOut(ErpSaleOutSaveReqVO createReqVO) {
        validateSaleUser(createReqVO.getSaleUserId());
        return executeInRequiredTransaction(() -> createSaleOutInTransaction(createReqVO));
    }

    Long createSaleOutInTransaction(ErpSaleOutSaveReqVO createReqVO) {
        // 1.1 校验销售订单已审核
        ErpSaleOrderDO saleOrder = saleOrderService.validateSaleOrder(createReqVO.getOrderId());
        // 1.2 校验出库项的有效性
        List<ErpSaleOutItemDO> saleOutItems = validateSaleOutItems(createReqVO.getItems());
        // 1.3 校验结算账户
        accountService.validateAccount(createReqVO.getAccountId());
        // 1.5 生成出库单号，并校验唯一性
        String no = noRedisDAO.generate(ErpNoRedisDAO.SALE_OUT_NO_PREFIX);
        if (erpSaleOutMapper.selectByNo(no) != null) {
            throw exception(SALE_OUT_NO_EXISTS);
        }

        // 2.1 插入出库
        ErpSaleOutDO saleOut = BeanUtils.toBean(createReqVO, ErpSaleOutDO.class, in -> in
                .setNo(no).setStatus(ErpAuditStatus.PROCESS.getStatus()))
                .setOrderNo(saleOrder.getNo()).setCustomerId(saleOrder.getCustomerId());
        calculateTotalPrice(saleOut, saleOutItems);
        erpSaleOutMapper.insert(saleOut);
        // 2.2 插入出库项
        saleOutItems.forEach(o -> o.setOutId(saleOut.getId()));
        erpSaleOutItemMapper.insertBatch(saleOutItems);

        // 3. 更新销售订单的出库数量
        updateSaleOrderOutCount(createReqVO.getOrderId());
        return saleOut.getId();
    }

    @Override
    public void updateSaleOut(ErpSaleOutSaveReqVO updateReqVO) {
        validateSaleUser(updateReqVO.getSaleUserId());
        executeInRequiredTransaction(() -> updateSaleOutInTransaction(updateReqVO));
    }

    void updateSaleOutInTransaction(ErpSaleOutSaveReqVO updateReqVO) {
        // 1.1 校验存在
        ErpSaleOutDO saleOut = validateSaleOutExists(updateReqVO.getId());
        if (ErpAuditStatus.APPROVE.getStatus().equals(saleOut.getStatus())) {
            throw exception(SALE_OUT_UPDATE_FAIL_APPROVE, saleOut.getNo());
        }
        // 1.2 校验销售订单已审核
        ErpSaleOrderDO saleOrder = saleOrderService.validateSaleOrder(updateReqVO.getOrderId());
        // 1.3 校验结算账户
        accountService.validateAccount(updateReqVO.getAccountId());
        // 1.5 校验订单项的有效性
        List<ErpSaleOutItemDO> saleOutItems = validateSaleOutItems(updateReqVO.getItems());

        // 2.1 更新出库
        ErpSaleOutDO updateObj = BeanUtils.toBean(updateReqVO, ErpSaleOutDO.class)
                .setOrderNo(saleOrder.getNo()).setCustomerId(saleOrder.getCustomerId());
        calculateTotalPrice(updateObj, saleOutItems);
        erpSaleOutMapper.updateById(updateObj);
        // 2.2 更新出库项
        updateSaleOutItemList(updateReqVO.getId(), saleOutItems);

        // 3.1 更新销售订单的出库数量
        updateSaleOrderOutCount(updateObj.getOrderId());
        // 3.2 注意：如果销售订单编号变更了，需要更新“老”销售订单的出库数量
        if (ObjectUtil.notEqual(saleOut.getOrderId(), updateObj.getOrderId())) {
            updateSaleOrderOutCount(saleOut.getOrderId());
        }
    }

    private void validateSaleUser(Long saleUserId) {
        if (saleUserId != null) {
            adminUserApi.validateUser(saleUserId);
        }
    }

    private <T> T executeInRequiredTransaction(java.util.function.Supplier<T> supplier) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        return transactionTemplate.execute(status -> supplier.get());
    }

    private void executeInRequiredTransaction(Runnable runnable) {
        executeInRequiredTransaction(() -> {
            runnable.run();
            return null;
        });
    }

    private void calculateTotalPrice(ErpSaleOutDO saleOut, List<ErpSaleOutItemDO> saleOutItems) {
        saleOut.setTotalCount(getSumValue(saleOutItems, ErpSaleOutItemDO::getCount, BigDecimal::add));
        saleOut.setTotalProductPrice(getSumValue(saleOutItems, ErpSaleOutItemDO::getTotalPrice, BigDecimal::add, BigDecimal.ZERO));
        saleOut.setTotalTaxPrice(getSumValue(saleOutItems, ErpSaleOutItemDO::getTaxPrice, BigDecimal::add, BigDecimal.ZERO));
        saleOut.setTotalPrice(saleOut.getTotalProductPrice().add(saleOut.getTotalTaxPrice()));
        // 计算优惠价格
        if (saleOut.getDiscountPercent() == null) {
            saleOut.setDiscountPercent(BigDecimal.ZERO);
        }
        if (saleOut.getOtherPrice() == null) {
            saleOut.setOtherPrice(BigDecimal.ZERO);
        }
        saleOut.setDiscountPrice(MoneyUtils.priceMultiplyPercent(saleOut.getTotalPrice(), saleOut.getDiscountPercent()));
        saleOut.setTotalPrice(saleOut.getTotalPrice().subtract(saleOut.getDiscountPrice().add(saleOut.getOtherPrice())));
    }

    private void updateSaleOrderOutCount(Long orderId) {
        // 1.1 查询销售订单对应的销售出库单列表
        List<ErpSaleOutDO> saleOuts = erpSaleOutMapper.selectListByOrderId(orderId);
        // 1.2 查询对应的销售订单项的出库数量
        Map<Long, BigDecimal> returnCountMap = erpSaleOutItemMapper.selectOrderItemCountSumMapByOutIds(
                convertList(saleOuts, ErpSaleOutDO::getId));
        // 2. 更新销售订单的出库数量
        saleOrderService.updateSaleOrderOutCount(orderId, returnCountMap);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSaleOutStatus(Long id, Integer status) {
        boolean approve = ErpAuditStatus.APPROVE.getStatus().equals(status);
        // 1.1 校验存在
        ErpSaleOutDO saleOut = validateSaleOutExists(id);
        // 1.2 校验状态
        if (saleOut.getStatus().equals(status)) {
            throw exception(approve ? SALE_OUT_APPROVE_FAIL : SALE_OUT_PROCESS_FAIL);
        }
        // 1.3 校验已退款
        if (!approve && saleOut.getReceiptPrice().compareTo(BigDecimal.ZERO) > 0) {
            throw exception(SALE_OUT_PROCESS_FAIL_EXISTS_RECEIPT);
        }

        // 2. 更新状态（反审核走带 receipt_price=0 的 CAS，关闭「读校验→并发收款回写→改状态」脏状态窗口）
        int updateCount = approve
                ? erpSaleOutMapper.updateByIdAndStatus(id, saleOut.getStatus(), new ErpSaleOutDO().setStatus(status))
                : erpSaleOutMapper.updateByIdAndStatusAndNoReceipt(id, saleOut.getStatus(), new ErpSaleOutDO().setStatus(status));
        if (updateCount == 0) {
            if (approve) {
                throw exception(SALE_OUT_APPROVE_FAIL);
            }
            // 反审核 CAS 失败：区分「并发已回写收款」与「并发状态变更」，给准确提示
            ErpSaleOutDO latest = erpSaleOutMapper.selectById(id);
            if (latest != null && latest.getReceiptPrice() != null
                    && latest.getReceiptPrice().compareTo(BigDecimal.ZERO) > 0) {
                throw exception(SALE_OUT_PROCESS_FAIL_EXISTS_RECEIPT);
            }
            throw exception(SALE_OUT_PROCESS_FAIL);
        }

        // 3. 变更库存
        List<ErpSaleOutItemDO> saleOutItems = erpSaleOutItemMapper.selectListByOutId(id);
        Integer bizType = approve ? ErpStockRecordBizTypeEnum.SALE_OUT.getType()
                : ErpStockRecordBizTypeEnum.SALE_OUT_CANCEL.getType();
        Map<String, ErpStockDO> stockMap = stockService.getStockMapByProductAndWarehouseIds(
                convertSet(saleOutItems, ErpSaleOutItemDO::getProductId),
                convertSet(saleOutItems, ErpSaleOutItemDO::getWarehouseId));
        if (approve) {
            allocateSaleOutBatches(saleOut, saleOutItems);
        } else {
            stockBatchAllocationService.rollbackOutbound(ErpStockRecordBizTypeEnum.SALE_OUT.getType(), id,
                    ErpStockRecordBizTypeEnum.SALE_OUT_CANCEL.getType(), "销售出库反审核");
        }
        saleOutItems.forEach(saleOutItem -> {
            BigDecimal count = approve ? saleOutItem.getCount().negate() : saleOutItem.getCount();
            // 获取加权平均成本作为出库价格
            ErpStockDO stock = stockMap.get(ErpStockService.buildProductWarehouseKey(
                    saleOutItem.getProductId(), saleOutItem.getWarehouseId()));
            BigDecimal price = stock != null ? stock.getAverageCost() : null;
            BigDecimal amount = price != null ? price.multiply(saleOutItem.getCount()) : null;
            stockRecordService.createStockRecord(new ErpStockRecordCreateReqBO(
                    saleOutItem.getProductId(), saleOutItem.getWarehouseId(), count,
                    bizType, saleOutItem.getOutId(), saleOutItem.getId(), saleOut.getNo(),
                    price, amount));
        });
        if (approve) {
            financeBizHookService.handleApprovedBiz(ErpBizTypeEnum.SALE_OUT.getType(), id,
                    defaultTime(saleOut.getOutTime(), saleOut.getCreateTime(), saleOut.getUpdateTime()).toLocalDate());
            // 新增：创建应收台账
            arStatementService.createStatementForSaleOut(saleOut);
            // 事务提交后异步触发项目生命周期刷新
            if (saleOut.getOrderId() != null) {
                Long orderId = saleOut.getOrderId();
                ErpTransactionUtils.afterCommit(() -> {
                    try {
                        ErpSaleOrderDO order = saleOrderService.validateSaleOrder(orderId);
                        if (order != null && order.getProjectId() != null) {
                            eventPublisher.publishEvent(new ProjectLifecycleRefreshEvent(
                                    order.getProjectId(), "销售出库审批通过"));
                        }
                    } catch (Exception e) {
                        log.warn("[updateSaleOutStatus] 触发项目生命周期刷新事件失败，orderId={}", orderId, e);
                    }
                });
            }
        } else {
            financeBizHookService.handleRollbackBiz(ErpBizTypeEnum.SALE_OUT.getType(), id,
                    null, "销售出库反审核关闭双账套凭证");
            // 新增：关闭应收台账
            arStatementService.closeStatementByBiz(ErpBizTypeEnum.SALE_OUT.getType(), id, "销售出库反审核");
        }
    }

    private void allocateSaleOutBatches(ErpSaleOutDO saleOut, List<ErpSaleOutItemDO> saleOutItems) {
        saleOutItems.forEach(item -> stockBatchAllocationService.allocateOutbound(new ErpStockBatchAllocateOutboundReqBO()
                .setProductId(item.getProductId())
                .setWarehouseId(item.getWarehouseId())
                .setCount(item.getCount())
                .setBizType(ErpStockRecordBizTypeEnum.SALE_OUT.getType())
                .setBizId(saleOut.getId())
                .setBizItemId(item.getId())
                .setBizNo(saleOut.getNo())
                .setRemark(item.getRemark())));
    }

    private LocalDateTime defaultTime(LocalDateTime... values) {
        for (LocalDateTime value : values) {
            if (value != null) {
                return value;
            }
        }
        return LocalDateTime.now();
    }

    @Override
    public void updateSaleInReceiptPrice(Long id, BigDecimal receiptPrice) {
        ErpSaleOutDO saleOut = erpSaleOutMapper.selectById(id);
        if (saleOut.getReceiptPrice().equals(receiptPrice)) {
            return;
        }
        if (receiptPrice.compareTo(saleOut.getTotalPrice()) > 0) {
            throw exception(SALE_OUT_FAIL_RECEIPT_PRICE_EXCEED, receiptPrice,  saleOut.getTotalPrice());
        }
        erpSaleOutMapper.updateById(new ErpSaleOutDO().setId(id).setReceiptPrice(receiptPrice));
    }

    private List<ErpSaleOutItemDO> validateSaleOutItems(List<ErpSaleOutSaveReqVO.Item> list) {
        // 1. 校验产品存在
        productService.validProductList(convertSet(list, ErpSaleOutSaveReqVO.Item::getProductId));
        // 2. 转化为 ErpSaleOutItemDO 列表
        List<ErpSaleOutItemDO> items = convertList(list, o -> BeanUtils.toBean(o, ErpSaleOutItemDO.class));
        // 3. 批量换算录入单位：count 换算为基本单位记账数量，inputCount/conversionRate 保留录入口径
        List<ErpProductUnitConversionService.ConversionResult> results = unitConversionService.convertBatch(
                convertList(items, item -> new ErpProductUnitConversionService.ConversionRequest(
                        item.getProductId(), item.getProductUnitId(), item.getCount())));
        for (int i = 0; i < items.size(); i++) {
            ErpSaleOutItemDO item = items.get(i);
            ErpProductUnitConversionService.ConversionResult result = results.get(i);
            item.setProductUnitId(result.getInputUnitId());
            item.setInputCount(result.getInputCount());
            item.setConversionRate(result.getConversionRate());
            item.setCount(result.getBaseCount());
            // 4. 金额计算：单价为录入单位口径，金额 = 单价 × 录入数量
            item.setTotalPrice(MoneyUtils.priceMultiply(item.getProductPrice(), result.getInputCount()));
            if (item.getTotalPrice() == null) {
                continue;
            }
            if (item.getTaxPercent() != null) {
                item.setTaxPrice(MoneyUtils.priceMultiplyPercent(item.getTotalPrice(), item.getTaxPercent()));
            }
        }
        return items;
    }

    private void updateSaleOutItemList(Long id, List<ErpSaleOutItemDO> newList) {
        // 第一步，对比新老数据，获得添加、修改、删除的列表
        List<ErpSaleOutItemDO> oldList = erpSaleOutItemMapper.selectListByOutId(id);
        List<List<ErpSaleOutItemDO>> diffList = diffList(oldList, newList, // id 不同，就认为是不同的记录
                (oldVal, newVal) -> oldVal.getId().equals(newVal.getId()));

        // 第二步，批量添加、修改、删除
        if (CollUtil.isNotEmpty(diffList.get(0))) {
            diffList.get(0).forEach(o -> o.setOutId(id));
            erpSaleOutItemMapper.insertBatch(diffList.get(0));
        }
        if (CollUtil.isNotEmpty(diffList.get(1))) {
            erpSaleOutItemMapper.updateBatch(diffList.get(1));
        }
        if (CollUtil.isNotEmpty(diffList.get(2))) {
            erpSaleOutItemMapper.deleteByIds(convertList(diffList.get(2), ErpSaleOutItemDO::getId));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSaleOut(List<Long> ids) {
        // 1. 校验不处于已审批
        List<ErpSaleOutDO> saleOuts = erpSaleOutMapper.selectByIds(ids);
        if (CollUtil.isEmpty(saleOuts)) {
            return;
        }
        saleOuts.forEach(saleOut -> {
            if (ErpAuditStatus.APPROVE.getStatus().equals(saleOut.getStatus())) {
                throw exception(SALE_OUT_DELETE_FAIL_APPROVE, saleOut.getNo());
            }
            // 已产生收款的出库单禁止删除，避免收款项 bizId 悬空、后续重算脏数据
            if (saleOut.getReceiptPrice() != null && saleOut.getReceiptPrice().compareTo(BigDecimal.ZERO) > 0) {
                throw exception(SALE_OUT_DELETE_FAIL_EXISTS_RECEIPT, saleOut.getNo());
            }
        });

        // 2. 遍历删除，并记录操作日志
        saleOuts.forEach(saleOut -> {
            // 2.1 删除订单
            erpSaleOutMapper.deleteById(saleOut.getId());
            // 2.2 删除订单项
            erpSaleOutItemMapper.deleteByOutId(saleOut.getId());

            // 2.3 更新销售订单的出库数量
            updateSaleOrderOutCount(saleOut.getOrderId());
        });

    }

    private ErpSaleOutDO validateSaleOutExists(Long id) {
        ErpSaleOutDO saleOut = erpSaleOutMapper.selectById(id);
        if (saleOut == null) {
            throw exception(SALE_OUT_NOT_EXISTS);
        }
        return saleOut;
    }

    @Override
    public ErpSaleOutDO getSaleOut(Long id) {
        return erpSaleOutMapper.selectById(id);
    }

    @Override
    public ErpSaleOutDO validateSaleOut(Long id) {
        ErpSaleOutDO saleOut = validateSaleOutExists(id);
        if (ObjectUtil.notEqual(saleOut.getStatus(), ErpAuditStatus.APPROVE.getStatus())) {
            throw exception(SALE_OUT_NOT_APPROVE);
        }
        return saleOut;
    }

    @Override
    public PageResult<ErpSaleOutDO> getSaleOutPage(ErpSaleOutPageReqVO pageReqVO) {
        return erpSaleOutMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ErpSaleOutDO> getSaleOutListByOrderId(Long orderId) {
        return erpSaleOutMapper.selectListByOrderId(orderId);
    }

    @Override
    public List<ErpSaleOutDO> getSaleOutListByOrderIds(Collection<Long> orderIds) {
        return erpSaleOutMapper.selectListByOrderIds(orderIds);
    }

    @Override
    public BigDecimal getTotalReceivedAmount() {
        return erpSaleOutMapper.sumTotalReceiptPrice();
    }

    // ==================== 销售出库项 ====================

    @Override
    public List<ErpSaleOutItemDO> getSaleOutItemListByOutId(Long outId) {
        return erpSaleOutItemMapper.selectListByOutId(outId);
    }

    @Override
    public List<ErpSaleOutItemDO> getSaleOutItemListByOutIds(Collection<Long> outIds) {
        if (CollUtil.isEmpty(outIds)) {
            return Collections.emptyList();
        }
        return erpSaleOutItemMapper.selectListByOutIds(outIds);
    }

}
