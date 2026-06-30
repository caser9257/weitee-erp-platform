package cn.weitee.erp.module.erp.service.purchase;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.number.MoneyUtils;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.returns.ErpPurchaseReturnPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.returns.ErpPurchaseReturnSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseOrderDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseReturnDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseReturnItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockDO;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinancePaymentAllocateMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseReturnItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseReturnMapper;
import cn.weitee.erp.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.enums.ErpFinancePaymentAllocateStatusEnum;
import cn.weitee.erp.module.erp.enums.common.ErpBizTypeEnum;
import cn.weitee.erp.module.erp.enums.stock.ErpStockRecordBizTypeEnum;
import cn.weitee.erp.module.erp.service.finance.ErpAccountService;
import cn.weitee.erp.module.erp.service.finance.ErpApStatementService;
import cn.weitee.erp.module.erp.service.finance.ErpFinanceBizHookService;
import cn.weitee.erp.module.erp.service.product.ErpProductService;
import cn.weitee.erp.module.erp.service.stock.ErpStockRecordService;
import cn.weitee.erp.module.erp.service.stock.ErpStockService;
import cn.weitee.erp.module.erp.service.stock.bo.ErpStockRecordCreateReqBO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.*;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.*;

// TODO 芋艿：记录操作日志

/**
 * ERP 采购退货 Service 实现类
 *
 * @author WeTai
 */
@Service
@Validated
@Slf4j
public class ErpPurchaseReturnServiceImpl implements ErpPurchaseReturnService {

    @Resource
    private ErpPurchaseReturnMapper erpPurchaseReturnMapper;
    @Resource
    private ErpPurchaseReturnItemMapper erpPurchaseReturnItemMapper;

    @Resource
    private ErpNoRedisDAO noRedisDAO;

    @Resource
    private ErpProductService productService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private ErpPurchaseOrderService purchaseOrderService;
    @Resource
    private ErpAccountService accountService;
    @Resource
    @Lazy
    private ErpApStatementService apStatementService;
    @Resource
    @Lazy
    private ErpFinanceBizHookService financeBizHookService;
    @Resource
    private ErpFinancePaymentAllocateMapper erpFinancePaymentAllocateMapper;
    @Resource
    private ErpStockRecordService stockRecordService;
    @Resource
    private ErpStockService stockService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPurchaseReturn(ErpPurchaseReturnSaveReqVO createReqVO) {
        // 1.1 校验采购订单已审核
        ErpPurchaseOrderDO purchaseOrder = purchaseOrderService.validatePurchaseOrder(createReqVO.getOrderId());
        // 1.2 校验退货项的有效性
        List<ErpPurchaseReturnItemDO> purchaseReturnItems = validatePurchaseReturnItems(createReqVO.getItems());
        // 1.3 校验结算账户
        accountService.validateAccount(createReqVO.getAccountId());
        // 1.4 生成退货单号，并校验唯一性
        String no = noRedisDAO.generate(ErpNoRedisDAO.PURCHASE_RETURN_NO_PREFIX);
        if (erpPurchaseReturnMapper.selectByNo(no) != null) {
            throw exception(PURCHASE_RETURN_NO_EXISTS);
        }

        // 2.1 插入退货
        ErpPurchaseReturnDO purchaseReturn = BeanUtils.toBean(createReqVO, ErpPurchaseReturnDO.class, in -> in
                .setNo(no).setStatus(ErpAuditStatus.DRAFT.getStatus()))
                .setOrderNo(purchaseOrder.getNo()).setSupplierId(purchaseOrder.getSupplierId());
        calculateTotalPrice(purchaseReturn, purchaseReturnItems);
        erpPurchaseReturnMapper.insert(purchaseReturn);
        // 2.2 插入退货项
        purchaseReturnItems.forEach(o -> o.setReturnId(purchaseReturn.getId()));
        erpPurchaseReturnItemMapper.insertBatch(purchaseReturnItems);

        // 3. 更新采购订单的退货数量
        updatePurchaseOrderReturnCount(createReqVO.getOrderId());
        return purchaseReturn.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePurchaseReturn(ErpPurchaseReturnSaveReqVO updateReqVO) {
        // 1.1 校验存在
        ErpPurchaseReturnDO purchaseReturn = validatePurchaseReturnExists(updateReqVO.getId());
        if (ErpAuditStatus.APPROVE.getStatus().equals(purchaseReturn.getStatus())) {
            throw exception(PURCHASE_RETURN_UPDATE_FAIL_APPROVE, purchaseReturn.getNo());
        }
        if (ErpAuditStatus.PROCESS.getStatus().equals(purchaseReturn.getStatus())) {
            throw exception(PURCHASE_RETURN_UPDATE_FAIL_PROCESSING, purchaseReturn.getNo());
        }
        // 1.2 校验采购订单已审核
        ErpPurchaseOrderDO purchaseOrder = purchaseOrderService.validatePurchaseOrder(updateReqVO.getOrderId());
        // 1.3 校验结算账户
        accountService.validateAccount(updateReqVO.getAccountId());
        // 1.4 校验订单项的有效性
        List<ErpPurchaseReturnItemDO> purchaseReturnItems = validatePurchaseReturnItems(updateReqVO.getItems());

        // 2.1 更新退货
        ErpPurchaseReturnDO updateObj = BeanUtils.toBean(updateReqVO, ErpPurchaseReturnDO.class)
                .setOrderNo(purchaseOrder.getNo()).setSupplierId(purchaseOrder.getSupplierId());
        calculateTotalPrice(updateObj, purchaseReturnItems);
        erpPurchaseReturnMapper.updateById(updateObj);
        // 2.2 更新退货项
        updatePurchaseReturnItemList(updateReqVO.getId(), purchaseReturnItems);

        // 3.1 更新采购订单的出库数量
        updatePurchaseOrderReturnCount(updateObj.getOrderId());
        // 3.2 注意：如果采购订单编号变更了，需要更新“老”采购订单的出库数量
        if (ObjectUtil.notEqual(purchaseReturn.getOrderId(), updateObj.getOrderId())) {
            updatePurchaseOrderReturnCount(purchaseReturn.getOrderId());
        }
    }

    private void calculateTotalPrice(ErpPurchaseReturnDO purchaseReturn, List<ErpPurchaseReturnItemDO> purchaseReturnItems) {
        purchaseReturn.setTotalCount(getSumValue(purchaseReturnItems, ErpPurchaseReturnItemDO::getCount, BigDecimal::add));
        purchaseReturn.setTotalProductPrice(getSumValue(purchaseReturnItems, ErpPurchaseReturnItemDO::getTotalPrice, BigDecimal::add, BigDecimal.ZERO));
        purchaseReturn.setTotalTaxPrice(getSumValue(purchaseReturnItems, ErpPurchaseReturnItemDO::getTaxPrice, BigDecimal::add, BigDecimal.ZERO));
        purchaseReturn.setTotalPrice(purchaseReturn.getTotalProductPrice().add(purchaseReturn.getTotalTaxPrice()));
        // 计算优惠价格
        if (purchaseReturn.getDiscountPercent() == null) {
            purchaseReturn.setDiscountPercent(BigDecimal.ZERO);
        }
        purchaseReturn.setDiscountPrice(MoneyUtils.priceMultiplyPercent(purchaseReturn.getTotalPrice(), purchaseReturn.getDiscountPercent()));
        purchaseReturn.setTotalPrice(purchaseReturn.getTotalPrice().subtract(purchaseReturn.getDiscountPrice()).add(purchaseReturn.getOtherPrice()));
    }

    private void updatePurchaseOrderReturnCount(Long orderId) {
        // 1.1 查询采购订单对应的采购出库单列表
        List<ErpPurchaseReturnDO> purchaseReturns = erpPurchaseReturnMapper.selectApprovedListByOrderId(orderId);
        // 1.2 查询对应的采购订单项的退货数量
        Map<Long, BigDecimal> returnCountMap = erpPurchaseReturnItemMapper.selectOrderItemCountSumMapByReturnIds(
                convertList(purchaseReturns, ErpPurchaseReturnDO::getId));
        // 2. 更新采购订单的出库数量
        purchaseOrderService.updatePurchaseOrderReturnCount(orderId, returnCountMap);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePurchaseReturnStatus(Long id, Integer status) {
        boolean approve = ErpAuditStatus.APPROVE.getStatus().equals(status);
        // 1.1 校验存在
        ErpPurchaseReturnDO purchaseReturn = validatePurchaseReturnExists(id);
        // 1.2 校验状态
        if (purchaseReturn.getStatus().equals(status)) {
            throw exception(approve ? PURCHASE_RETURN_APPROVE_FAIL : PURCHASE_RETURN_PROCESS_FAIL);
        }
        // 1.3 校验已退款
        if (!approve && hasApprovedAllocate(id)) {
            throw exception(PURCHASE_RETURN_PROCESS_FAIL_EXISTS_REFUND);
        }

        // 2. 更新状态
        int updateCount = erpPurchaseReturnMapper.updateByIdAndStatus(id, purchaseReturn.getStatus(),
                new ErpPurchaseReturnDO().setStatus(status));
        if (updateCount == 0) {
            throw exception(approve ? PURCHASE_RETURN_APPROVE_FAIL : PURCHASE_RETURN_PROCESS_FAIL);
        }

        // 3. 变更库存
        if (approve) {
            apStatementService.createStatementForPurchaseReturn(purchaseReturn);
        }
        List<ErpPurchaseReturnItemDO> purchaseReturnItems = erpPurchaseReturnItemMapper.selectListByReturnId(id);
        Integer bizType = approve ? ErpStockRecordBizTypeEnum.PURCHASE_RETURN.getType()
                : ErpStockRecordBizTypeEnum.PURCHASE_RETURN_CANCEL.getType();
        // 批量查询库存（消除 N+1）
        Set<Long> returnProductIds = convertSet(purchaseReturnItems, ErpPurchaseReturnItemDO::getProductId);
        List<ErpStockDO> stockList = stockService.getStockListByProductIds(returnProductIds);
        Map<String, ErpStockDO> stockMap = stockList.stream().collect(Collectors.toMap(
                s -> s.getProductId() + ":" + s.getWarehouseId(), s -> s, (a, b) -> a));
        purchaseReturnItems.forEach(purchaseReturnItem -> {
            BigDecimal count = approve ? purchaseReturnItem.getCount().negate() : purchaseReturnItem.getCount();
            // 获取加权平均成本作为出库价格
            ErpStockDO stock = stockMap.get(purchaseReturnItem.getProductId() + ":" + purchaseReturnItem.getWarehouseId());
            BigDecimal price = stock != null ? stock.getAverageCost() : null;
            BigDecimal amount = price != null ? price.multiply(purchaseReturnItem.getCount()) : null;
            stockRecordService.createStockRecord(new ErpStockRecordCreateReqBO(
                    purchaseReturnItem.getProductId(), purchaseReturnItem.getWarehouseId(), count,
                    bizType, purchaseReturnItem.getReturnId(), purchaseReturnItem.getId(), purchaseReturn.getNo(),
                    price, amount));
        });
        if (!approve) {
            apStatementService.closeStatementByBiz(ErpBizTypeEnum.PURCHASE_RETURN.getType(), id, "采购退货反审核关闭台账");
            financeBizHookService.handleRollbackBiz(ErpBizTypeEnum.PURCHASE_RETURN.getType(), id,
                    null, "采购退货反审核关闭双账套凭证");
        }
        updatePurchaseOrderReturnCount(purchaseReturn.getOrderId());
        if (approve) {
            financeBizHookService.handleApprovedBiz(ErpBizTypeEnum.PURCHASE_RETURN.getType(), id,
                    defaultTime(purchaseReturn.getReturnTime(), purchaseReturn.getCreateTime(), purchaseReturn.getUpdateTime()).toLocalDate());
        }
    }

    @Override
    public void updatePurchaseReturnStatusManually(Long id, Integer status) {
        throw exception(PURCHASE_RETURN_MANUAL_STATUS_UPDATE_FORBIDDEN, id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePurchaseReturnStatusByBpm(Long id, String processInstanceId, Integer status, String reason) {
        ErpPurchaseReturnDO purchaseReturn = validatePurchaseReturnExists(id);
        if (!ErpAuditStatus.APPROVE.getStatus().equals(status)) {
            throw exception(PURCHASE_RETURN_UPDATE_FAIL_PROCESSING, id);
        }
        int updateCount = erpPurchaseReturnMapper.updateByIdStatusAndProcessInstanceId(id,
                ErpAuditStatus.PROCESS.getStatus(), processInstanceId,
                new ErpPurchaseReturnDO().setStatus(ErpAuditStatus.APPROVE.getStatus()).setProcessInstanceId(null));
        if (updateCount == 0) {
            throw exception(PURCHASE_RETURN_UPDATE_FAIL_PROCESSING, id);
        }

        apStatementService.createStatementForPurchaseReturn(purchaseReturn);
        List<ErpPurchaseReturnItemDO> purchaseReturnItems = erpPurchaseReturnItemMapper.selectListByReturnId(id);
        Set<Long> returnProductIds = convertSet(purchaseReturnItems, ErpPurchaseReturnItemDO::getProductId);
        List<ErpStockDO> stockList = stockService.getStockListByProductIds(returnProductIds);
        Map<String, ErpStockDO> stockMap = stockList.stream().collect(Collectors.toMap(
                s -> s.getProductId() + ":" + s.getWarehouseId(), s -> s, (a, b) -> a));
        purchaseReturnItems.forEach(purchaseReturnItem -> {
            BigDecimal count = purchaseReturnItem.getCount().negate();
            ErpStockDO stock = stockMap.get(purchaseReturnItem.getProductId() + ":" + purchaseReturnItem.getWarehouseId());
            BigDecimal price = stock != null ? stock.getAverageCost() : null;
            BigDecimal amount = price != null ? price.multiply(purchaseReturnItem.getCount()) : null;
            stockRecordService.createStockRecord(new ErpStockRecordCreateReqBO(
                    purchaseReturnItem.getProductId(), purchaseReturnItem.getWarehouseId(), count,
                    ErpStockRecordBizTypeEnum.PURCHASE_RETURN.getType(), purchaseReturnItem.getReturnId(), purchaseReturnItem.getId(), purchaseReturn.getNo(),
                    price, amount));
        });
        updatePurchaseOrderReturnCount(purchaseReturn.getOrderId());
        financeBizHookService.handleApprovedBiz(ErpBizTypeEnum.PURCHASE_RETURN.getType(), id,
                defaultTime(purchaseReturn.getReturnTime(), purchaseReturn.getCreateTime(), purchaseReturn.getUpdateTime()).toLocalDate());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rollbackPurchaseReturnStatusToDraftByBpm(Long id, String processInstanceId, String reason) {
        validatePurchaseReturnExists(id);
        int updateCount = erpPurchaseReturnMapper.resetStatusToDraftByBpm(id, processInstanceId);
        if (updateCount == 0) {
            throw exception(PURCHASE_RETURN_UPDATE_FAIL_PROCESSING, id);
        }
    }

    @Override
    public void updatePurchaseReturnRefundPrice(Long id, BigDecimal refundPrice) {
        ErpPurchaseReturnDO purchaseReturn = erpPurchaseReturnMapper.selectById(id);
        if (purchaseReturn.getRefundPrice().equals(refundPrice)) {
            return;
        }
        if (refundPrice.compareTo(purchaseReturn.getTotalPrice()) > 0) {
            throw exception(PURCHASE_RETURN_FAIL_REFUND_PRICE_EXCEED, refundPrice, purchaseReturn.getTotalPrice());
        }
        erpPurchaseReturnMapper.updateById(new ErpPurchaseReturnDO().setId(id).setRefundPrice(refundPrice));
    }

    private List<ErpPurchaseReturnItemDO> validatePurchaseReturnItems(List<ErpPurchaseReturnSaveReqVO.Item> list) {
        // 1. 校验产品存在
        List<ErpProductDO> productList = productService.validProductList(
                convertSet(list, ErpPurchaseReturnSaveReqVO.Item::getProductId));
        Map<Long, ErpProductDO> productMap = convertMap(productList, ErpProductDO::getId);
        // 2. 转化为 ErpPurchaseReturnItemDO 列表
        return convertList(list, o -> BeanUtils.toBean(o, ErpPurchaseReturnItemDO.class, item -> {
            item.setProductUnitId(productMap.get(item.getProductId()).getUnitId());
            item.setTotalPrice(MoneyUtils.priceMultiply(item.getProductPrice(), item.getCount()));
            if (item.getTotalPrice() == null) {
                return;
            }
            if (item.getTaxPercent() != null) {
                item.setTaxPrice(MoneyUtils.priceMultiplyPercent(item.getTotalPrice(), item.getTaxPercent()));
            }
        }));
    }

    private void updatePurchaseReturnItemList(Long id, List<ErpPurchaseReturnItemDO> newList) {
        // 第一步，对比新老数据，获得添加、修改、删除的列表
        List<ErpPurchaseReturnItemDO> oldList = erpPurchaseReturnItemMapper.selectListByReturnId(id);
        List<List<ErpPurchaseReturnItemDO>> diffList = diffList(oldList, newList, // id 不同，就认为是不同的记录
                (oldVal, newVal) -> oldVal.getId().equals(newVal.getId()));

        // 第二步，批量添加、修改、删除
        if (CollUtil.isNotEmpty(diffList.get(0))) {
            diffList.get(0).forEach(o -> o.setReturnId(id));
            erpPurchaseReturnItemMapper.insertBatch(diffList.get(0));
        }
        if (CollUtil.isNotEmpty(diffList.get(1))) {
            erpPurchaseReturnItemMapper.updateBatch(diffList.get(1));
        }
        if (CollUtil.isNotEmpty(diffList.get(2))) {
            erpPurchaseReturnItemMapper.deleteByIds(convertList(diffList.get(2), ErpPurchaseReturnItemDO::getId));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePurchaseReturn(List<Long> ids) {
        // 1. 校验不处于已审批
        List<ErpPurchaseReturnDO> purchaseReturns = erpPurchaseReturnMapper.selectByIds(ids);
        if (CollUtil.isEmpty(purchaseReturns)) {
            return;
        }
        purchaseReturns.forEach(purchaseReturn -> {
            if (ErpAuditStatus.APPROVE.getStatus().equals(purchaseReturn.getStatus())) {
                throw exception(PURCHASE_RETURN_DELETE_FAIL_APPROVE, purchaseReturn.getNo());
            }
            if (ErpAuditStatus.PROCESS.getStatus().equals(purchaseReturn.getStatus())) {
                throw exception(PURCHASE_RETURN_DELETE_FAIL_PROCESSING, purchaseReturn.getNo());
            }
        });

        // 2. 遍历删除，并记录操作日志
        purchaseReturns.forEach(purchaseReturn -> {
            // 2.1 删除订单
            erpPurchaseReturnMapper.deleteById(purchaseReturn.getId());
            // 2.2 删除订单项
            erpPurchaseReturnItemMapper.deleteByReturnId(purchaseReturn.getId());

            // 2.3 更新采购订单的出库数量
            updatePurchaseOrderReturnCount(purchaseReturn.getOrderId());
        });

    }

    private ErpPurchaseReturnDO validatePurchaseReturnExists(Long id) {
        ErpPurchaseReturnDO purchaseReturn = erpPurchaseReturnMapper.selectById(id);
        if (purchaseReturn == null) {
            throw exception(PURCHASE_RETURN_NOT_EXISTS);
        }
        return purchaseReturn;
    }

    @Override
    public ErpPurchaseReturnDO getPurchaseReturn(Long id) {
        return erpPurchaseReturnMapper.selectById(id);
    }

    @Override
    public ErpPurchaseReturnDO validatePurchaseReturn(Long id) {
        ErpPurchaseReturnDO purchaseReturn = getPurchaseReturn(id);
        if (ObjectUtil.notEqual(purchaseReturn.getStatus(), ErpAuditStatus.APPROVE.getStatus())) {
            throw exception(PURCHASE_RETURN_NOT_APPROVE);
        }
        return purchaseReturn;
    }

    @Override
    public PageResult<ErpPurchaseReturnDO> getPurchaseReturnPage(ErpPurchaseReturnPageReqVO pageReqVO) {
        return erpPurchaseReturnMapper.selectPage(pageReqVO);
    }

    // ==================== 采购退货项 ====================

    private boolean hasApprovedAllocate(Long bizId) {
        return ObjectUtil.defaultIfNull(erpFinancePaymentAllocateMapper.selectCountByBizTypeAndBizIdAndStatus(
                ErpBizTypeEnum.PURCHASE_RETURN.getType(), bizId,
                ErpFinancePaymentAllocateStatusEnum.APPROVED.getStatus()), 0L) > 0;
    }

    @Override
    public List<ErpPurchaseReturnItemDO> getPurchaseReturnItemListByReturnId(Long returnId) {
        return erpPurchaseReturnItemMapper.selectListByReturnId(returnId);
    }

    @Override
    public List<ErpPurchaseReturnItemDO> getPurchaseReturnItemListByReturnIds(Collection<Long> returnIds) {
        if (CollUtil.isEmpty(returnIds)) {
            return Collections.emptyList();
        }
        return erpPurchaseReturnItemMapper.selectListByReturnIds(returnIds);
    }

    private LocalDateTime defaultTime(LocalDateTime... values) {
        for (LocalDateTime value : values) {
            if (value != null) {
                return value;
            }
        }
        return LocalDateTime.now();
    }

}
