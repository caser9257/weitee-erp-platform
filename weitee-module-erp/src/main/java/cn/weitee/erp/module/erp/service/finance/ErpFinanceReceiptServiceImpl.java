package cn.weitee.erp.module.erp.service.finance;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.collection.CollectionUtils;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.receipt.ErpFinanceReceiptPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.receipt.ErpFinanceReceiptSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceReceiptDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceReceiptItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpSaleOutDO;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpSaleOrderDO;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpSaleReturnDO;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceReceiptItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceReceiptMapper;
import cn.weitee.erp.module.erp.dal.mysql.sale.ErpSaleOrderMapper;
import cn.weitee.erp.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.enums.common.ErpBizTypeEnum;
import cn.weitee.erp.module.erp.service.sale.ErpCustomerService;
import cn.weitee.erp.module.erp.service.sale.ErpSaleOrderService;
import cn.weitee.erp.module.erp.service.sale.ErpSaleOutService;
import cn.weitee.erp.module.erp.service.sale.ErpSaleReturnService;
import cn.weitee.erp.module.erp.service.project.ErpProjectLifecycleService;
import cn.weitee.erp.module.erp.service.project.event.ProjectLifecycleRefreshEvent;
import cn.weitee.erp.module.erp.util.ErpTransactionUtils;
import cn.weitee.erp.module.system.api.user.AdminUserApi;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.*;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.*;

// TODO 芋艿：记录操作日志

/**
 * ERP 收款单 Service 实现类
 *
 * @author WeTai
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
    private ErpSaleOrderService saleOrderService;
    @Resource
    private ErpProjectLifecycleService projectLifecycleService;
    @Resource
    private ApplicationEventPublisher eventPublisher;

    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private PlatformTransactionManager transactionManager;

    @Override
    public Long createFinanceReceipt(ErpFinanceReceiptSaveReqVO createReqVO) {
        validateFinanceUser(createReqVO.getFinanceUserId());
        return executeInRequiredTransaction(() -> createFinanceReceiptInTransaction(createReqVO));
    }

    Long createFinanceReceiptInTransaction(ErpFinanceReceiptSaveReqVO createReqVO) {
        // 1.1 校验订单项的有效性
        List<ErpFinanceReceiptItemDO> receiptItems = validateFinanceReceiptItems(
                createReqVO.getCustomerId(), createReqVO.getItems());
        // 1.2 校验客户
        customerService.validateCustomer(createReqVO.getCustomerId());
        // 1.3 校验结算账户
        if (createReqVO.getAccountId() != null) {
            accountService.validateAccount(createReqVO.getAccountId());
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

        return receipt.getId();
    }

    @Override
    public void updateFinanceReceipt(ErpFinanceReceiptSaveReqVO updateReqVO) {
        validateFinanceUser(updateReqVO.getFinanceUserId());
        executeInRequiredTransaction(() -> updateFinanceReceiptInTransaction(updateReqVO));
    }

    void updateFinanceReceiptInTransaction(ErpFinanceReceiptSaveReqVO updateReqVO) {
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

    private void validateFinanceUser(Long financeUserId) {
        if (financeUserId != null) {
            adminUserApi.validateUser(financeUserId);
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

    private void calculateTotalPrice(ErpFinanceReceiptDO receipt, List<ErpFinanceReceiptItemDO> receiptItems) {
        receipt.setTotalPrice(getSumValue(receiptItems, ErpFinanceReceiptItemDO::getReceiptPrice, BigDecimal::add, BigDecimal.ZERO));
        receipt.setReceiptPrice(receipt.getTotalPrice().subtract(receipt.getDiscountPrice()));
    }

    @Override
    public void updateFinanceReceiptStatus(Long id, Integer status) {
        boolean approve = ErpAuditStatus.APPROVE.getStatus().equals(status);
        boolean process = ErpAuditStatus.PROCESS.getStatus().equals(status);
        if (!approve && !process) {
            throw exception(FINANCE_RECEIPT_PROCESS_FAIL);
        }
        // 1.1 校验存在
        ErpFinanceReceiptDO receipt = validateFinanceReceiptExists(id);
        // 1.2 校验状态
        if (receipt.getStatus().equals(status)) {
            throw exception(approve ? FINANCE_RECEIPT_APPROVE_FAIL : FINANCE_RECEIPT_PROCESS_FAIL);
        }

        // 在事务外获取 Redisson 锁，防止锁在事务内获取导致锁超时前无法释放
        List<ErpFinanceReceiptItemDO> receiptItems = erpFinanceReceiptItemMapper.selectListByReceiptId(id);
        List<RLock> locks = approve ? lockBizEntities(receiptItems) : Collections.emptyList();
        try {
            executeInRequiredTransaction(() -> {
                doUpdateFinanceReceiptStatus(id, status, receipt, receiptItems, locks, approve);
            });
        } finally {
            unlockNow(locks);
        }
    }

    private void doUpdateFinanceReceiptStatus(Long id, Integer status, ErpFinanceReceiptDO receipt,
                                               List<ErpFinanceReceiptItemDO> receiptItems,
                                               List<RLock> locks, boolean approve) {
        // 3. 审批通过时重新校验剩余可收金额
        if (approve) {
            reValidateReceiptAmounts(receipt, receiptItems);
        }

        // 4. 更新状态
        int updateCount = erpFinanceReceiptMapper.updateByIdAndStatus(id, receipt.getStatus(),
                new ErpFinanceReceiptDO().setStatus(status));
        if (updateCount == 0) {
            throw exception(approve ? FINANCE_RECEIPT_APPROVE_FAIL : FINANCE_RECEIPT_PROCESS_FAIL);
        }

        // 5. 按审批后口径刷新销售出库、退货的收款金额情况
        updateSalePrice(receiptItems);

        // 6. 审批通过或驳回/作废时，事务提交后更新关联销售订单收款状态
        Long finalReceiptId = id;
        ErpTransactionUtils.afterCommit(() -> {
            if (approve) {
                triggerProjectLifecycleRefresh(finalReceiptId);
            }
            updateRelatedSaleOrderReceipt(finalReceiptId);
        });
    }

    private void reValidateReceiptAmounts(ErpFinanceReceiptDO receipt, List<ErpFinanceReceiptItemDO> receiptItems) {
        for (ErpFinanceReceiptItemDO item : receiptItems) {
            BigDecimal bizTotalPrice;
            if (ObjectUtil.equal(item.getBizType(), ErpBizTypeEnum.SALE_OUT.getType())) {
                ErpSaleOutDO saleOut = saleOutService.validateSaleOut(item.getBizId());
                bizTotalPrice = saleOut.getTotalPrice();
            } else if (ObjectUtil.equal(item.getBizType(), ErpBizTypeEnum.SALE_RETURN.getType())) {
                ErpSaleReturnDO saleReturn = saleReturnService.validateSaleReturn(item.getBizId());
                bizTotalPrice = saleReturn.getTotalPrice();
            } else {
                throw new IllegalArgumentException("业务类型不正确：" + item.getBizType());
            }
            BigDecimal approvedReceiptPrice = erpFinanceReceiptItemMapper.selectReceiptPriceSumByBizIdAndBizType(
                    item.getBizId(), item.getBizType());
            BigDecimal availablePrice = ObjectUtil.defaultIfNull(bizTotalPrice, BigDecimal.ZERO)
                    .abs().subtract(ObjectUtil.defaultIfNull(approvedReceiptPrice, BigDecimal.ZERO));
            BigDecimal receiptPrice = ObjectUtil.defaultIfNull(item.getReceiptPrice(), BigDecimal.ZERO);
            if (receiptPrice.compareTo(availablePrice) > 0) {
                throw new IllegalArgumentException("本次收款不能超过业务单剩余可收金额");
            }
        }
    }

    private List<RLock> lockBizEntities(List<ErpFinanceReceiptItemDO> receiptItems) {
        if (CollUtil.isEmpty(receiptItems)) {
            return Collections.emptyList();
        }
        List<String> lockKeys = receiptItems.stream()
                .map(item -> "erp:finance-receipt:biz:" + item.getBizType() + ":" + item.getBizId())
                .distinct()
                .sorted()
                .toList();
        List<RLock> locks = new ArrayList<>(lockKeys.size());
        try {
            for (String lockKey : lockKeys) {
                RLock lock = redissonClient.getLock(lockKey);
                if (!lock.tryLock()) {
                    throw new IllegalArgumentException("业务单正在被其他收款单审批，请稍后重试");
                }
                locks.add(lock);
            }
            return locks;
        } catch (Exception ex) {
            unlockNow(locks);
            if (ex instanceof RuntimeException re) {
                throw re;
            }
            throw new IllegalArgumentException("业务单加锁失败", ex);
        }
    }

    private void unlockAfterTransaction(List<RLock> locks) {
        if (CollUtil.isEmpty(locks)) {
            return;
        }
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            unlockNow(locks);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                unlockNow(locks);
            }
        });
    }

    private void unlockNow(List<RLock> locks) {
        for (int index = locks.size() - 1; index >= 0; index--) {
            RLock lock = locks.get(index);
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 更新关联销售订单的收款状态
     */
    private void updateRelatedSaleOrderReceipt(Long receiptId) {
        try {
            List<ErpFinanceReceiptItemDO> items = erpFinanceReceiptItemMapper.selectListByReceiptId(receiptId);
            // 收集关联的销售订单ID
            java.util.Set<Long> orderIds = new java.util.HashSet<>();
            for (ErpFinanceReceiptItemDO item : items) {
                if (ObjectUtil.equal(item.getBizType(), ErpBizTypeEnum.SALE_OUT.getType())) {
                    ErpSaleOutDO saleOut = saleOutService.validateSaleOut(item.getBizId());
                    if (saleOut != null && saleOut.getOrderId() != null) {
                        orderIds.add(saleOut.getOrderId());
                    }
                }
            }
            // 更新每个订单的收款状态
            for (Long orderId : orderIds) {
                saleOrderService.updateSaleOrderReceiptPrice(orderId);
            }
        } catch (Exception e) {
            log.error("[updateRelatedSaleOrderReceipt] 更新销售订单收款状态失败，receiptId={}，需人工介入", receiptId, e);
        }
    }

    /**
     * 触发收款单关联项目的生命周期刷新（异步）
     */
    private void triggerProjectLifecycleRefresh(Long receiptId) {
        try {
            List<ErpFinanceReceiptItemDO> items = erpFinanceReceiptItemMapper.selectListByReceiptId(receiptId);
            for (ErpFinanceReceiptItemDO item : items) {
                if (ObjectUtil.equal(item.getBizType(), cn.weitee.erp.module.erp.enums.common.ErpBizTypeEnum.SALE_OUT.getType())) {
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
                validateReceiptPriceNotExceed(item, saleOut.getTotalPrice());
            } else if (ObjectUtil.equal(item.getBizType(), ErpBizTypeEnum.SALE_RETURN.getType())) {
                ErpSaleReturnDO saleReturn = saleReturnService.validateSaleReturn(item.getBizId());
                Assert.equals(saleReturn.getCustomerId(), customerId, "客户必须相同");
                item.setTotalPrice(saleReturn.getTotalPrice().negate()).setBizNo(saleReturn.getNo());
                validateReceiptPriceNotExceed(item, saleReturn.getTotalPrice());
            } else {
                throw new IllegalArgumentException("业务类型不正确：" + item.getBizType());
            }
        }));
    }

    private void validateReceiptPriceNotExceed(ErpFinanceReceiptItemDO item, BigDecimal bizTotalPrice) {
        BigDecimal approvedReceiptPrice = erpFinanceReceiptItemMapper.selectReceiptPriceSumByBizIdAndBizType(
                item.getBizId(), item.getBizType());
        BigDecimal availablePrice = ObjectUtil.defaultIfNull(bizTotalPrice, BigDecimal.ZERO)
                .abs().subtract(ObjectUtil.defaultIfNull(approvedReceiptPrice, BigDecimal.ZERO));
        BigDecimal receiptPrice = ObjectUtil.defaultIfNull(item.getReceiptPrice(), BigDecimal.ZERO);
        if (receiptPrice.compareTo(availablePrice) > 0) {
            throw new IllegalArgumentException("本次收款不能超过业务单剩余可收金额");
        }
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
        // 1. 批量查询所有订单关联的销售出库单（一次查询，替代循环单条查询）
        List<ErpSaleOutDO> saleOutList = saleOutService.getSaleOutListByOrderIds(orderIds);
        // 2. 按 orderId 分组汇总已收款金额
        Map<Long, BigDecimal> result = new java.util.HashMap<>();
        for (ErpSaleOutDO saleOut : saleOutList) {
            if (saleOut.getReceiptPrice() == null) {
                continue;
            }
            result.merge(saleOut.getOrderId(), saleOut.getReceiptPrice(), BigDecimal::add);
        }
        // 3. 确保所有 orderId 都有值（未查到出库单的订单默认 0）
        for (Long orderId : orderIds) {
            result.putIfAbsent(orderId, BigDecimal.ZERO);
        }
        return result;
    }

}
