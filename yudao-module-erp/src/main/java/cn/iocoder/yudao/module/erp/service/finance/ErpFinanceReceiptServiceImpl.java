package cn.iocoder.yudao.module.erp.service.finance;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.receipt.ErpFinanceReceiptPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.receipt.ErpFinanceReceiptSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceReceiptDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceReceiptItemDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOutDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOrderDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleReturnDO;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinanceReceiptItemMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinanceReceiptMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.sale.ErpSaleOrderMapper;
import cn.iocoder.yudao.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.iocoder.yudao.module.erp.enums.ErpAuditStatus;
import cn.iocoder.yudao.module.erp.enums.common.ErpBizTypeEnum;
import cn.iocoder.yudao.module.erp.service.sale.ErpCustomerService;
import cn.iocoder.yudao.module.erp.service.sale.ErpSaleOutService;
import cn.iocoder.yudao.module.erp.service.sale.ErpSaleReturnService;
import cn.iocoder.yudao.module.erp.service.project.ErpProjectLifecycleService;
import cn.iocoder.yudao.module.erp.service.project.event.ProjectLifecycleRefreshEvent;
import cn.iocoder.yudao.module.erp.util.ErpTransactionUtils;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.*;

// TODO 芋艿：记录操作日志

/**
 * ERP 收款单 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class ErpFinanceReceiptServiceImpl implements ErpFinanceReceiptService {

    @Resource
    private ErpFinanceReceiptMapper erpFinanceReceiptMapper;
    @Resource
    private ErpFinanceReceiptItemMapper erpFinanceReceiptItemMapper;

    @Resource
    private ErpNoRedisDAO noRedisDAO;

    @Resource
    private ErpCustomerService customerService;
    @Resource
    private ErpAccountService accountService;
    @Resource
    private ErpSaleOutService saleOutService;
    @Resource
    private ErpSaleReturnService saleReturnService;
    @Resource
    private ErpSaleOrderMapper saleOrderMapper;
    @Resource
    private ErpProjectLifecycleService projectLifecycleService;
    @Resource
    private ApplicationEventPublisher eventPublisher;

    @Resource
    private AdminUserApi adminUserApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createFinanceReceipt(ErpFinanceReceiptSaveReqVO createReqVO) {
        // 1.1 校验订单项的有效性
        List<ErpFinanceReceiptItemDO> receiptItems = validateFinanceReceiptItems(
                createReqVO.getCustomerId(), createReqVO.getItems());
        // 1.2 校验客户
        customerService.validateCustomer(createReqVO.getCustomerId());
        // 1.3 校验结算账户
        if (createReqVO.getAccountId() != null) {
            accountService.validateAccount(createReqVO.getAccountId());
        }
        // 1.4 校验财务人员
        if (createReqVO.getFinanceUserId() != null) {
            adminUserApi.validateUser(createReqVO.getFinanceUserId());
        }
        // 1.5 生成收款单号，并校验唯一性
        String no = noRedisDAO.generate(ErpNoRedisDAO.FINANCE_RECEIPT_NO_PREFIX);
        if (erpFinanceReceiptMapper.selectByNo(no) != null) {
            throw exception(FINANCE_RECEIPT_NO_EXISTS);
        }

        // 2.1 插入收款单
        ErpFinanceReceiptDO receipt = BeanUtils.toBean(createReqVO, ErpFinanceReceiptDO.class, in -> in
                .setNo(no).setStatus(ErpAuditStatus.PROCESS.getStatus()));
        calculateTotalPrice(receipt, receiptItems);
        erpFinanceReceiptMapper.insert(receipt);
        // 2.2 插入收款单项
        receiptItems.forEach(o -> o.setReceiptId(receipt.getId()));
        erpFinanceReceiptItemMapper.insertBatch(receiptItems);

        // 3. 更新销售出库、退货的收款金额情况
        updateSalePrice(receiptItems);
        return receipt.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFinanceReceipt(ErpFinanceReceiptSaveReqVO updateReqVO) {
        // 1.1 校验存在
        ErpFinanceReceiptDO receipt = validateFinanceReceiptExists(updateReqVO.getId());
        if (ErpAuditStatus.APPROVE.getStatus().equals(receipt.getStatus())) {
            throw exception(FINANCE_RECEIPT_UPDATE_FAIL_APPROVE, receipt.getNo());
        }
        // 1.2 校验客户
        customerService.validateCustomer(updateReqVO.getCustomerId());
        // 1.3 校验结算账户
        if (updateReqVO.getAccountId() != null) {
            accountService.validateAccount(updateReqVO.getAccountId());
        }
        // 1.4 校验财务人员
        if (updateReqVO.getFinanceUserId() != null) {
            adminUserApi.validateUser(updateReqVO.getFinanceUserId());
        }
        // 1.5 校验收款单项的有效性
        List<ErpFinanceReceiptItemDO> receiptItems = validateFinanceReceiptItems(
                updateReqVO.getCustomerId(), updateReqVO.getItems());

        // 2.1 更新收款单
        ErpFinanceReceiptDO updateObj = BeanUtils.toBean(updateReqVO, ErpFinanceReceiptDO.class);
        calculateTotalPrice(updateObj, receiptItems);
        erpFinanceReceiptMapper.updateById(updateObj);
        // 2.2 更新收款单项
        updateFinanceReceiptItemList(updateReqVO.getId(), receiptItems);
    }

    private void calculateTotalPrice(ErpFinanceReceiptDO receipt, List<ErpFinanceReceiptItemDO> receiptItems) {
        receipt.setTotalPrice(getSumValue(receiptItems, ErpFinanceReceiptItemDO::getReceiptPrice, BigDecimal::add, BigDecimal.ZERO));
        receipt.setReceiptPrice(receipt.getTotalPrice().subtract(receipt.getDiscountPrice()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFinanceReceiptStatus(Long id, Integer status) {
        boolean approve = ErpAuditStatus.APPROVE.getStatus().equals(status);
        // 1.1 校验存在
        ErpFinanceReceiptDO receipt = validateFinanceReceiptExists(id);
        // 1.2 校验状态
        if (receipt.getStatus().equals(status)) {
            throw exception(approve ? FINANCE_RECEIPT_APPROVE_FAIL : FINANCE_RECEIPT_PROCESS_FAIL);
        }

        // 2. 更新状态
        int updateCount = erpFinanceReceiptMapper.updateByIdAndStatus(id, receipt.getStatus(),
                new ErpFinanceReceiptDO().setStatus(status));
        if (updateCount == 0) {
            throw exception(approve ? FINANCE_RECEIPT_APPROVE_FAIL : FINANCE_RECEIPT_PROCESS_FAIL);
        }

        // 3. 审批通过时，事务提交后触发项目生命周期刷新
        if (approve) {
            Long finalReceiptId = id;
            ErpTransactionUtils.afterCommit(() -> {
                triggerProjectLifecycleRefresh(finalReceiptId);
            });
        }
    }

    /**
     * 触发收款单关联项目的生命周期刷新（异步）
     */
    private void triggerProjectLifecycleRefresh(Long receiptId) {
        try {
            List<ErpFinanceReceiptItemDO> items = erpFinanceReceiptItemMapper.selectListByReceiptId(receiptId);
            for (ErpFinanceReceiptItemDO item : items) {
                if (ObjectUtil.equal(item.getBizType(), cn.iocoder.yudao.module.erp.enums.common.ErpBizTypeEnum.SALE_OUT.getType())) {
                    ErpSaleOutDO saleOut = saleOutService.validateSaleOut(item.getBizId());
                    if (saleOut != null && saleOut.getOrderId() != null) {
                        ErpSaleOrderDO saleOrder = saleOrderMapper.selectById(saleOut.getOrderId());
                        if (saleOrder != null && saleOrder.getProjectId() != null) {
                            // 异步触发项目生命周期刷新
                            eventPublisher.publishEvent(new ProjectLifecycleRefreshEvent(
                                    saleOrder.getProjectId(), "收款单审批通过"));
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("[triggerProjectLifecycleRefresh] 触发项目生命周期刷新事件失败，receiptId={}", receiptId, e);
        }
    }

    private List<ErpFinanceReceiptItemDO> validateFinanceReceiptItems(
            Long customerId,
            List<ErpFinanceReceiptSaveReqVO.Item> list) {
        return convertList(list, o -> BeanUtils.toBean(o, ErpFinanceReceiptItemDO.class, item -> {
            if (ObjectUtil.equal(item.getBizType(), ErpBizTypeEnum.SALE_OUT.getType())) {
                ErpSaleOutDO saleOut = saleOutService.validateSaleOut(item.getBizId());
                Assert.equals(saleOut.getCustomerId(), customerId, "客户必须相同");
                item.setTotalPrice(saleOut.getTotalPrice()).setBizNo(saleOut.getNo());
            } else if (ObjectUtil.equal(item.getBizType(), ErpBizTypeEnum.SALE_RETURN.getType())) {
                ErpSaleReturnDO saleReturn = saleReturnService.validateSaleReturn(item.getBizId());
                Assert.equals(saleReturn.getCustomerId(), customerId, "客户必须相同");
                item.setTotalPrice(saleReturn.getTotalPrice().negate()).setBizNo(saleReturn.getNo());
            } else {
                throw new IllegalArgumentException("业务类型不正确：" + item.getBizType());
            }
        }));
    }

    private void updateFinanceReceiptItemList(Long id, List<ErpFinanceReceiptItemDO> newList) {
        // 第一步，对比新老数据，获得添加、修改、删除的列表
        List<ErpFinanceReceiptItemDO> oldList = erpFinanceReceiptItemMapper.selectListByReceiptId(id);
        List<List<ErpFinanceReceiptItemDO>> diffList = diffList(oldList, newList, // id 不同，就认为是不同的记录
                (oldVal, newVal) -> oldVal.getId().equals(newVal.getId()));

        // 第二步，批量添加、修改、删除
        if (CollUtil.isNotEmpty(diffList.get(0))) {
            diffList.get(0).forEach(o -> o.setReceiptId(id));
            erpFinanceReceiptItemMapper.insertBatch(diffList.get(0));
        }
        if (CollUtil.isNotEmpty(diffList.get(1))) {
            erpFinanceReceiptItemMapper.updateBatch(diffList.get(1));
        }
        if (CollUtil.isNotEmpty(diffList.get(2))) {
            erpFinanceReceiptItemMapper.deleteByIds(convertList(diffList.get(2), ErpFinanceReceiptItemDO::getId));
        }

        // 第三步，更新销售出库、退货的收款金额情况
        updateSalePrice(CollectionUtils.newArrayList(diffList));
    }

    private void updateSalePrice(List<ErpFinanceReceiptItemDO> receiptItems) {
        receiptItems.forEach(receiptItem -> {
            BigDecimal totalReceiptPrice = erpFinanceReceiptItemMapper.selectReceiptPriceSumByBizIdAndBizType(
                    receiptItem.getBizId(), receiptItem.getBizType());
            if (ErpBizTypeEnum.SALE_OUT.getType().equals(receiptItem.getBizType())) {
                saleOutService.updateSaleInReceiptPrice(receiptItem.getBizId(), totalReceiptPrice);
            } else if (ErpBizTypeEnum.SALE_RETURN.getType().equals(receiptItem.getBizType())) {
                saleReturnService.updateSaleReturnRefundPrice(receiptItem.getBizId(), totalReceiptPrice.negate());
            } else {
                throw new IllegalArgumentException("业务类型不正确：" + receiptItem.getBizType());
            }
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFinanceReceipt(List<Long> ids) {
        // 1. 校验不处于已审批
        List<ErpFinanceReceiptDO> receipts = erpFinanceReceiptMapper.selectByIds(ids);
        if (CollUtil.isEmpty(receipts)) {
            return;
        }
        receipts.forEach(receipt -> {
            if (ErpAuditStatus.APPROVE.getStatus().equals(receipt.getStatus())) {
                throw exception(FINANCE_RECEIPT_DELETE_FAIL_APPROVE, receipt.getNo());
            }
        });

        // 2. 遍历删除，并记录操作日志
        receipts.forEach(receipt -> {
            // 2.1 删除收款单
            erpFinanceReceiptMapper.deleteById(receipt.getId());
            // 2.2 删除收款单项
            List<ErpFinanceReceiptItemDO> receiptItems = erpFinanceReceiptItemMapper.selectListByReceiptId(receipt.getId());
            erpFinanceReceiptItemMapper.deleteByIds(convertSet(receiptItems, ErpFinanceReceiptItemDO::getId));

            // 2.3 更新销售出库、退货的收款金额情况
            updateSalePrice(receiptItems);
        });
    }

    private ErpFinanceReceiptDO validateFinanceReceiptExists(Long id) {
        ErpFinanceReceiptDO receipt = erpFinanceReceiptMapper.selectById(id);
        if (receipt == null) {
            throw exception(FINANCE_RECEIPT_NOT_EXISTS);
        }
        return receipt;
    }

    @Override
    public ErpFinanceReceiptDO getFinanceReceipt(Long id) {
        return erpFinanceReceiptMapper.selectById(id);
    }

    @Override
    public PageResult<ErpFinanceReceiptDO> getFinanceReceiptPage(ErpFinanceReceiptPageReqVO pageReqVO) {
        return erpFinanceReceiptMapper.selectPage(pageReqVO);
    }

    // ==================== 收款单项 ====================

    @Override
    public List<ErpFinanceReceiptItemDO> getFinanceReceiptItemListByReceiptId(Long receiptId) {
        return erpFinanceReceiptItemMapper.selectListByReceiptId(receiptId);
    }

    @Override
    public List<ErpFinanceReceiptItemDO> getFinanceReceiptItemListByReceiptIds(Collection<Long> receiptIds) {
        if (CollUtil.isEmpty(receiptIds)) {
            return Collections.emptyList();
        }
        return erpFinanceReceiptItemMapper.selectListByReceiptIds(receiptIds);
    }

    @Override
    public BigDecimal getReceivedAmountByOrderId(Long orderId) {
        // 1. 查询该订单关联的所有销售出库单
        List<ErpSaleOutDO> saleOutList = saleOutService.getSaleOutListByOrderId(orderId);
        if (CollUtil.isEmpty(saleOutList)) {
            return BigDecimal.ZERO;
        }

        // 2. 汇总所有出库单的已收款金额
        BigDecimal totalReceived = BigDecimal.ZERO;
        for (ErpSaleOutDO saleOut : saleOutList) {
            if (saleOut.getReceiptPrice() != null) {
                totalReceived = totalReceived.add(saleOut.getReceiptPrice());
            }
        }
        return totalReceived;
    }

    @Override
    public Map<Long, BigDecimal> getReceivedAmountByOrderIds(Collection<Long> orderIds) {
        if (CollUtil.isEmpty(orderIds)) {
            return java.util.Collections.emptyMap();
        }
        Map<Long, BigDecimal> result = new java.util.HashMap<>();
        for (Long orderId : orderIds) {
            result.put(orderId, getReceivedAmountByOrderId(orderId));
        }
        return result;
    }

}
