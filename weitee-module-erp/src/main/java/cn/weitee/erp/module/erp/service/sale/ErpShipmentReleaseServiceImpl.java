package cn.weitee.erp.module.erp.service.sale;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.sale.vo.ShipmentReleasePageReqVO;
import cn.weitee.erp.module.erp.controller.admin.sale.vo.ShipmentReleasePageVO;
import cn.weitee.erp.module.erp.controller.admin.sale.vo.ShipmentReleaseResultVO;
import cn.weitee.erp.module.erp.controller.admin.sale.vo.ShipmentReleaseStatsVO;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpSaleOrderDO;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpSaleOrderItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpSaleOutDO;
import cn.weitee.erp.module.erp.dal.mysql.sale.ErpSaleOrderItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.sale.ErpSaleOrderMapper;
import cn.weitee.erp.module.erp.dal.mysql.sale.ErpSaleOutMapper;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.enums.ShipmentReleaseRule;
import cn.weitee.erp.module.erp.service.finance.ErpFinanceReceiptService;
import cn.weitee.erp.module.crm.service.contract.CrmContractService;
import cn.weitee.erp.module.crm.dal.dataobject.contract.CrmContractDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.*;

/**
 * 发货放行校验服务实现
 *
 * @author system
 */
@Service
@Validated
@Slf4j
public class ErpShipmentReleaseServiceImpl implements ErpShipmentReleaseService {

    @Resource
    private ErpSaleOrderMapper erpSaleOrderMapper;
    @Resource
    private ErpSaleOrderItemMapper erpSaleOrderItemMapper;
    @Resource
    private ErpSaleOutMapper erpSaleOutMapper;
    @Resource
    private ErpSaleOutService erpSaleOutService;

    @Resource
    private ErpFinanceReceiptService erpFinanceReceiptService;

    @Resource
    private CrmContractService crmContractService;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private PlatformTransactionManager transactionManager;

    @Override
    public ShipmentReleaseResultVO checkRelease(Long orderId) {
        // 1. 获取订单信息
        ErpSaleOrderDO order = erpSaleOrderMapper.selectById(orderId);
        if (order == null) {
            return buildBlockedResult("订单不存在");
        }

        // 2. 单条查询合同（批量场景请使用 checkReleaseInternal + 预取的 contractMap/receivedAmountMap）
        CrmContractDO contract = null;
        if (order.getContractId() != null) {
            try {
                contract = crmContractService.getContract(order.getContractId());
            } catch (Exception e) {
                log.warn("[checkRelease] 获取合同信息失败，contractId={}", order.getContractId(), e);
            }
        }
        // 3. 单条查询已收款金额
        BigDecimal receivedAmount = BigDecimal.ZERO;
        try {
            receivedAmount = erpFinanceReceiptService.getReceivedAmountByOrderId(orderId);
        } catch (Exception e) {
            log.warn("[checkRelease] 获取收款金额失败，orderId={}", orderId, e);
        }
        return checkReleaseInternal(order, contract, receivedAmount);
    }

    /**
     * 放行校验的纯内存计算内核，不发起任何数据库/远程查询。
     *
     * <p>用于消除分页场景下逐行调用 {@link #checkRelease(Long)} 产生的 N+1 查询：
     * 调用方需提前批量查出订单关联的合同和已收款金额，再调用本方法完成校验。</p>
     *
     * @param order          订单（必须非空）
     * @param contract       订单关联的合同，可为 null（表示未关联或查询失败）
     * @param receivedAmount 订单已收款金额（预取，不可为 null，无数据时传 {@link BigDecimal#ZERO}）
     */
    private ShipmentReleaseResultVO checkReleaseInternal(ErpSaleOrderDO order, CrmContractDO contract,
                                                          BigDecimal receivedAmount) {
        List<ShipmentReleaseResultVO.ReleaseCheckDetailVO> details = new ArrayList<>();
        List<String> blockerReasons = new ArrayList<>();

        // 1. 校验订单状态
        ShipmentReleaseResultVO.ReleaseCheckDetailVO orderStatusCheck = checkOrderStatus(order);
        details.add(orderStatusCheck);
        if (!orderStatusCheck.isPassed()) {
            blockerReasons.add(orderStatusCheck.getMessage());
            return buildResult(false, "BLOCKED", blockerReasons, null, details);
        }

        // 2. 校验合同关联
        if (order.getContractId() == null) {
            ShipmentReleaseResultVO.ReleaseCheckDetailVO contractCheck = new ShipmentReleaseResultVO.ReleaseCheckDetailVO();
            contractCheck.setCheckItem("合同关联校验");
            contractCheck.setPassed(false);
            contractCheck.setMessage("订单未关联合同");
            details.add(contractCheck);
            blockerReasons.add("订单未关联合同");
            return buildResult(false, "BLOCKED", blockerReasons, null, details);
        }

        // 3. 解析放行规则（取自合同，缺省为签约即发）
        String releaseRule = "SIGN_AND_SHIP";
        BigDecimal prepaymentRatio = null;
        if (contract != null) {
            if (contract.getShipmentReleaseRule() != null) {
                releaseRule = contract.getShipmentReleaseRule();
            }
            prepaymentRatio = contract.getPrepaymentRatio();
        }

        // 4. 校验放行规则
        ShipmentReleaseResultVO.ReleaseCheckDetailVO ruleCheck = checkReleaseRule(order, releaseRule, prepaymentRatio, receivedAmount);
        details.add(ruleCheck);
        if (!ruleCheck.isPassed()) {
            blockerReasons.add(ruleCheck.getMessage());
            return buildResult(false, "BLOCKED", blockerReasons, null, details);
        }

        // 5. 通用校验：财务审核
        ShipmentReleaseResultVO.ReleaseCheckDetailVO financeCheck = checkFinanceApproval(order);
        details.add(financeCheck);
        if (!financeCheck.isPassed()) {
            blockerReasons.add(financeCheck.getMessage());
            return buildResult(false, "FINANCE_REVIEW", blockerReasons, "财务人员", details);
        }

        // 6. 所有校验通过
        return buildResult(true, "RELEASED", new ArrayList<>(), null, details);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitFinanceApproval(Long orderId, Long approverId) {
        // 1. 校验订单存在
        ErpSaleOrderDO order = erpSaleOrderMapper.selectById(orderId);
        if (order == null) {
            throw exception(SHIPMENT_RELEASE_ORDER_NOT_EXISTS);
        }
        // 2. 校验状态：只有 BLOCKED 状态才能提交财务审核
        if (!"BLOCKED".equals(order.getShipmentReleaseStatus())) {
            throw exception(SHIPMENT_RELEASE_STATUS_INVALID);
        }

        // 3. 更新订单状态为待财务审核
        ErpSaleOrderDO updateOrder = new ErpSaleOrderDO();
        updateOrder.setId(orderId);
        updateOrder.setShipmentReleaseStatus("FINANCE_REVIEW");
        erpSaleOrderMapper.updateById(updateOrder);

        log.info("订单[{}]已提交财务审核，审核人[{}]", orderId, approverId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveFinance(Long orderId, Long approverId, String remark) {
        // 1. 校验订单存在
        ErpSaleOrderDO order = erpSaleOrderMapper.selectById(orderId);
        if (order == null) {
            throw exception(SHIPMENT_RELEASE_ORDER_NOT_EXISTS);
        }
        // 2. 校验状态：只有 FINANCE_REVIEW 状态才能审批通过
        if (!"FINANCE_REVIEW".equals(order.getShipmentReleaseStatus())) {
            throw exception(SHIPMENT_RELEASE_STATUS_INVALID);
        }

        // 3. 更新订单状态为已放行
        ErpSaleOrderDO updateOrder = new ErpSaleOrderDO();
        updateOrder.setId(orderId);
        updateOrder.setShipmentReleaseStatus("RELEASED");
        updateOrder.setShipmentReleaseReason(null);
        erpSaleOrderMapper.updateById(updateOrder);

        log.info("订单[{}]财务审核通过，审核人[{}]，意见[{}]", orderId, approverId, remark);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectFinance(Long orderId, Long approverId, String reason) {
        // 1. 校验订单存在
        ErpSaleOrderDO order = erpSaleOrderMapper.selectById(orderId);
        if (order == null) {
            throw exception(SHIPMENT_RELEASE_ORDER_NOT_EXISTS);
        }
        // 2. 校验状态：只有 FINANCE_REVIEW 状态才能驳回
        if (!"FINANCE_REVIEW".equals(order.getShipmentReleaseStatus())) {
            throw exception(SHIPMENT_RELEASE_STATUS_INVALID);
        }

        // 3. 更新订单状态为阻塞
        ErpSaleOrderDO updateOrder = new ErpSaleOrderDO();
        updateOrder.setId(orderId);
        updateOrder.setShipmentReleaseStatus("BLOCKED");
        updateOrder.setShipmentReleaseReason(reason);
        erpSaleOrderMapper.updateById(updateOrder);

        log.info("订单[{}]财务审核驳回，审核人[{}]，原因[{}]", orderId, approverId, reason);
    }

    /**
     * 校验订单状态
     */
    private ShipmentReleaseResultVO.ReleaseCheckDetailVO checkOrderStatus(ErpSaleOrderDO order) {
        ShipmentReleaseResultVO.ReleaseCheckDetailVO detail = new ShipmentReleaseResultVO.ReleaseCheckDetailVO();
        detail.setCheckItem("订单状态校验");

        // 检查订单是否已审批通过
        if (order.getStatus() == null || order.getStatus() != ErpAuditStatus.APPROVE.getStatus()) {
            detail.setPassed(false);
            detail.setMessage("订单未审批通过，当前状态：" + order.getStatus());
        } else {
            detail.setPassed(true);
            detail.setMessage("订单已审批通过");
        }

        return detail;
    }

    /**
     * 校验收款规则
     */
    private ShipmentReleaseResultVO.ReleaseCheckDetailVO checkReleaseRule(ErpSaleOrderDO order, String releaseRule,
                                                                           BigDecimal prepaymentRatio, BigDecimal receivedAmount) {
        ShipmentReleaseResultVO.ReleaseCheckDetailVO detail = new ShipmentReleaseResultVO.ReleaseCheckDetailVO();
        detail.setCheckItem("放行规则校验");

        // 签约即发：直接通过
        if ("SIGN_AND_SHIP".equals(releaseRule)) {
            detail.setPassed(true);
            detail.setMessage("签约即发，规则校验通过");
            return detail;
        }

        // 到账后发：检查收款金额
        if ("AFTER_PAYMENT".equals(releaseRule)) {
            return checkPaymentReceived(order, detail, receivedAmount);
        }

        // 达到预付款比例后发：检查预付款金额
        if ("AFTER_PREPAYMENT".equals(releaseRule)) {
            return checkPrepaymentReceived(order, detail, prepaymentRatio, receivedAmount);
        }

        // 财务审核后发：检查财务审核状态
        if ("FINANCE_APPROVAL".equals(releaseRule)) {
            return checkFinanceApprovalStatus(order, detail);
        }

        // 默认：签约即发
        detail.setPassed(true);
        detail.setMessage("默认签约即发，规则校验通过");
        return detail;
    }

    /**
     * 检查收款金额是否满足
     */
    private ShipmentReleaseResultVO.ReleaseCheckDetailVO checkPaymentReceived(ErpSaleOrderDO order,
                                                                               ShipmentReleaseResultVO.ReleaseCheckDetailVO detail,
                                                                               BigDecimal receivedAmount) {
        BigDecimal orderAmount = order.getTotalPrice() != null ? order.getTotalPrice() : BigDecimal.ZERO;

        if (receivedAmount.compareTo(orderAmount) >= 0) {
            detail.setPassed(true);
            detail.setMessage("已收款金额 ¥" + receivedAmount + " >= 订单金额 ¥" + orderAmount);
        } else {
            detail.setPassed(false);
            detail.setMessage("已收款金额 ¥" + receivedAmount + " < 订单金额 ¥" + orderAmount);
        }

        return detail;
    }

    /**
     * 检查预付款金额是否满足
     */
    private ShipmentReleaseResultVO.ReleaseCheckDetailVO checkPrepaymentReceived(ErpSaleOrderDO order,
                                                                                   ShipmentReleaseResultVO.ReleaseCheckDetailVO detail,
                                                                                   BigDecimal prepaymentRatio,
                                                                                   BigDecimal receivedAmount) {
        BigDecimal orderAmount = order.getTotalPrice() != null ? order.getTotalPrice() : BigDecimal.ZERO;

        // 计算预付款金额：优先使用合同的预付款比例，否则使用订单的定金
        BigDecimal prepaymentAmount;
        String ratioSource;
        if (prepaymentRatio != null && prepaymentRatio.compareTo(BigDecimal.ZERO) > 0) {
            prepaymentAmount = orderAmount.multiply(prepaymentRatio).divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP);
            ratioSource = "合同预付款比例 " + prepaymentRatio + "%";
        } else {
            prepaymentAmount = order.getDepositPrice() != null ? order.getDepositPrice() : BigDecimal.ZERO;
            ratioSource = "订单定金";
        }

        if (receivedAmount.compareTo(prepaymentAmount) >= 0) {
            detail.setPassed(true);
            detail.setMessage("已收款 ¥" + receivedAmount + " >= 预付款 ¥" + prepaymentAmount + "（" + ratioSource + "）");
        } else {
            detail.setPassed(false);
            detail.setMessage("已收款 ¥" + receivedAmount + " < 预付款 ¥" + prepaymentAmount + "（" + ratioSource + "）");
        }

        return detail;
    }

    /**
     * 检查财务审核状态
     */
    private ShipmentReleaseResultVO.ReleaseCheckDetailVO checkFinanceApprovalStatus(ErpSaleOrderDO order,
                                                                                     ShipmentReleaseResultVO.ReleaseCheckDetailVO detail) {
        String releaseStatus = order.getShipmentReleaseStatus();
        
        if ("RELEASED".equals(releaseStatus)) {
            detail.setPassed(true);
            detail.setMessage("财务审核已通过");
        } else if ("FINANCE_REVIEW".equals(releaseStatus)) {
            detail.setPassed(false);
            detail.setMessage("等待财务审核");
        } else {
            detail.setPassed(false);
            detail.setMessage("财务审核未通过或未提交");
        }

        return detail;
    }

    /**
     * 校验财务审核（通用校验）
     */
    private ShipmentReleaseResultVO.ReleaseCheckDetailVO checkFinanceApproval(ErpSaleOrderDO order) {
        ShipmentReleaseResultVO.ReleaseCheckDetailVO detail = new ShipmentReleaseResultVO.ReleaseCheckDetailVO();
        detail.setCheckItem("财务审核校验");

        // 业务确认：所有发货都需要财务审核
        String releaseStatus = order.getShipmentReleaseStatus();
        
        if ("RELEASED".equals(releaseStatus)) {
            detail.setPassed(true);
            detail.setMessage("财务审核已通过");
        } else {
            detail.setPassed(false);
            detail.setMessage("需要财务审核，当前状态：" + releaseStatus);
        }

        return detail;
    }

    /**
     * 构建阻塞结果
     */
    private ShipmentReleaseResultVO buildBlockedResult(String reason) {
        List<String> blockerReasons = new ArrayList<>();
        blockerReasons.add(reason);
        return buildResult(false, "BLOCKED", blockerReasons, null, new ArrayList<>());
    }

    /**
     * 构建结果
     */
    private ShipmentReleaseResultVO buildResult(boolean releasable, String releaseStatus,
                                                 List<String> blockerReasons, String pendingRole,
                                                 List<ShipmentReleaseResultVO.ReleaseCheckDetailVO> details) {
        ShipmentReleaseResultVO result = new ShipmentReleaseResultVO();
        result.setReleasable(releasable);
        result.setReleaseStatus(releaseStatus);
        result.setBlockerReasons(blockerReasons);
        result.setPendingRole(pendingRole);
        result.setDetails(details);
        return result;
    }

    @Override
    public PageResult<ShipmentReleasePageVO> getReleasePage(ShipmentReleasePageReqVO reqVO) {
        // 1. 构建查询条件
        LambdaQueryWrapper<ErpSaleOrderDO> query = new LambdaQueryWrapper<ErpSaleOrderDO>()
                .like(StrUtil.isNotBlank(reqVO.getOrderNo()), ErpSaleOrderDO::getNo, reqVO.getOrderNo())
                .eq(reqVO.getCustomerId() != null, ErpSaleOrderDO::getCustomerId, reqVO.getCustomerId())
                .eq(reqVO.getSaleUserId() != null, ErpSaleOrderDO::getSaleUserId, reqVO.getSaleUserId())
                .like(StrUtil.isNotBlank(reqVO.getContractNo()), ErpSaleOrderDO::getContractNo, reqVO.getContractNo())
                .eq(StrUtil.isNotBlank(reqVO.getReleaseStatus()), ErpSaleOrderDO::getShipmentReleaseStatus, reqVO.getReleaseStatus())
                .eq(ErpSaleOrderDO::getStatus, ErpAuditStatus.APPROVE.getStatus()) // 只查询已审批通过的订单
                .orderByDesc(ErpSaleOrderDO::getId);

        // 2. 执行分页查询
        PageResult<ErpSaleOrderDO> orderPage = erpSaleOrderMapper.selectPage(reqVO, query);
        List<ErpSaleOrderDO> orders = orderPage.getList();
        if (CollUtil.isEmpty(orders)) {
            return PageResult.empty(orderPage.getTotal());
        }

        // 3. 批量预取本页涉及的合同 + 已收款金额，消除逐行查询的 N+1
        Set<Long> contractIds = orders.stream()
                .map(ErpSaleOrderDO::getContractId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, CrmContractDO> contractMap;
        try {
            contractMap = crmContractService.getContractMap(contractIds);
        } catch (Exception e) {
            log.warn("[getReleasePage] 批量获取合同信息失败，contractIds={}", contractIds, e);
            contractMap = java.util.Collections.emptyMap();
        }
        Set<Long> orderIds = orders.stream().map(ErpSaleOrderDO::getId).collect(Collectors.toSet());
        Map<Long, BigDecimal> receivedAmountMap;
        try {
            receivedAmountMap = erpFinanceReceiptService.getReceivedAmountByOrderIds(orderIds);
        } catch (Exception e) {
            log.warn("[getReleasePage] 批量获取收款金额失败，orderIds={}", orderIds, e);
            receivedAmountMap = java.util.Collections.emptyMap();
        }

        // 4. 转换为 VO（纯内存计算，不再逐行查库）
        Map<Long, CrmContractDO> finalContractMap = contractMap;
        Map<Long, BigDecimal> finalReceivedAmountMap = receivedAmountMap;
        List<ShipmentReleasePageVO> voList = orders.stream()
                .map(order -> convertToReleasePageVO(order,
                        order.getContractId() != null ? finalContractMap.get(order.getContractId()) : null,
                        finalReceivedAmountMap.getOrDefault(order.getId(), BigDecimal.ZERO)))
                .collect(Collectors.toList());

        return new PageResult<>(voList, orderPage.getTotal());
    }

    /**
     * 将订单 DO 转换为发货放行分页 VO
     *
     * <p>合同与已收款金额均由调用方（{@link #getReleasePage}）批量预取传入，
     * 本方法内部只做 {@link #checkReleaseInternal} 纯内存计算，不发起任何数据库查询，
     * 从而消除分页场景下逐行查询产生的 N+1 问题。</p>
     */
    private ShipmentReleasePageVO convertToReleasePageVO(ErpSaleOrderDO order, CrmContractDO contract,
                                                          BigDecimal receivedAmount) {
        ShipmentReleasePageVO vo = new ShipmentReleasePageVO();
        vo.setOrderId(order.getId());
        vo.setOrderNo(order.getNo());
        vo.setCustomerId(order.getCustomerId());
        vo.setSaleUserId(order.getSaleUserId());
        vo.setContractId(order.getContractId());
        vo.setContractNo(order.getContractNo());
        vo.setOrderTotalPrice(order.getTotalPrice());
        vo.setReleaseStatus(order.getShipmentReleaseStatus());
        vo.setShipmentReleaseReason(order.getShipmentReleaseReason());
        vo.setDeliveryDate(order.getDeliveryDate());
        vo.setCreateTime(order.getCreateTime());

        // 计算应收金额
        BigDecimal totalPrice = order.getTotalPrice() != null ? order.getTotalPrice() : BigDecimal.ZERO;
        vo.setReceivedAmount(receivedAmount);
        vo.setReceivableAmount(totalPrice.subtract(receivedAmount));

        // 获取放行校验结果（纯内存计算，不再查库）
        try {
            ShipmentReleaseResultVO checkResult = checkReleaseInternal(order, contract, receivedAmount);
            vo.setBlockerReasons(checkResult.getBlockerReasons());
            vo.setPendingRole(checkResult.getPendingRole());
        } catch (Exception e) {
            log.warn("[convertToReleasePageVO] 获取放行校验结果失败，orderId={}", order.getId(), e);
            vo.setBlockerReasons(new ArrayList<>());
        }

        return vo;
    }

    @Override
    public ShipmentReleaseStatsVO getReleaseStats() {
        // 仅查询状态列，单次 SQL 取回全部已审批订单的放行状态，在内存中一次性分组计数，
        // 避免原先 4 条独立 COUNT 查询对同一张表反复全表/索引扫描。
        List<ErpSaleOrderDO> orders = erpSaleOrderMapper.selectList(
                new LambdaQueryWrapper<ErpSaleOrderDO>()
                        .select(ErpSaleOrderDO::getId, ErpSaleOrderDO::getShipmentReleaseStatus)
                        .eq(ErpSaleOrderDO::getStatus, ErpAuditStatus.APPROVE.getStatus()));

        ShipmentReleaseStatsVO stats = new ShipmentReleaseStatsVO();
        stats.setTotalCount((long) orders.size());
        long blockedCount = 0L;
        long financeReviewCount = 0L;
        long releasedCount = 0L;
        for (ErpSaleOrderDO order : orders) {
            String releaseStatus = order.getShipmentReleaseStatus();
            if ("BLOCKED".equals(releaseStatus)) {
                blockedCount++;
            } else if ("FINANCE_REVIEW".equals(releaseStatus)) {
                financeReviewCount++;
            } else if ("RELEASED".equals(releaseStatus)) {
                releasedCount++;
            }
        }
        stats.setBlockedCount(blockedCount);
        stats.setFinanceReviewCount(financeReviewCount);
        stats.setReleasedCount(releasedCount);

        return stats;
    }

    @Override
    public Long createSaleOutFromRelease(Long orderId, Long warehouseId, Long userId) {
        RLock lock = redissonClient.getLock("erp:shipment-release:create-sale-out:" + orderId);
        if (!lock.tryLock()) {
            throw exception(SALE_OUT_ALREADY_EXISTS);
        }
        try {
            return executeInRequiredTransaction(() -> doCreateSaleOutFromRelease(orderId, warehouseId));
        } finally {
            unlockIfHeld(lock);
        }
    }

    private <T> T executeInRequiredTransaction(java.util.function.Supplier<T> supplier) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        return transactionTemplate.execute(status -> supplier.get());
    }

    private void unlockIfHeld(RLock lock) {
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }

    private Long doCreateSaleOutFromRelease(Long orderId, Long warehouseId) {
        // 1. 校验放行状态
        ErpSaleOrderDO order = erpSaleOrderMapper.selectById(orderId);
        if (order == null) {
            throw exception(SALE_ORDER_NOT_EXISTS);
        }
        if (!"RELEASED".equals(order.getShipmentReleaseStatus())) {
            throw exception(SHIPMENT_RELEASE_NOT_RELEASED);
        }

        // 2. 检查是否已有出库单
        Long existCount = erpSaleOutMapper.selectCount(
                new LambdaQueryWrapper<ErpSaleOutDO>()
                        .eq(ErpSaleOutDO::getOrderId, orderId)
                        .ne(ErpSaleOutDO::getStatus, ErpAuditStatus.REJECT.getStatus()));
        if (existCount > 0) {
            throw exception(SALE_OUT_ALREADY_EXISTS);
        }

        // 3. 获取订单项
        List<ErpSaleOrderItemDO> orderItems = erpSaleOrderItemMapper.selectListByOrderId(orderId);
        if (CollUtil.isEmpty(orderItems)) {
            throw exception(SALE_ORDER_ITEM_NOT_EXISTS);
        }

        // 4. 构建出库单保存请求
        cn.weitee.erp.module.erp.controller.admin.sale.vo.out.ErpSaleOutSaveReqVO reqVO =
                new cn.weitee.erp.module.erp.controller.admin.sale.vo.out.ErpSaleOutSaveReqVO();
        reqVO.setOrderId(orderId);
        reqVO.setAccountId(order.getAccountId());
        reqVO.setOutTime(java.time.LocalDateTime.now());

        // 5. 构建出库项
        List<cn.weitee.erp.module.erp.controller.admin.sale.vo.out.ErpSaleOutSaveReqVO.Item> outItems =
                new java.util.ArrayList<>();
        for (ErpSaleOrderItemDO orderItem : orderItems) {
            // 计算剩余可出库数量
            BigDecimal remainCount = orderItem.getCount().subtract(
                    orderItem.getOutCount() != null ? orderItem.getOutCount() : BigDecimal.ZERO);
            if (remainCount.compareTo(BigDecimal.ZERO) <= 0) {
                continue; // 已全部出库，跳过
            }

            cn.weitee.erp.module.erp.controller.admin.sale.vo.out.ErpSaleOutSaveReqVO.Item outItem =
                    new cn.weitee.erp.module.erp.controller.admin.sale.vo.out.ErpSaleOutSaveReqVO.Item();
            outItem.setWarehouseId(warehouseId);
            outItem.setProductId(orderItem.getProductId());
            outItem.setProductUnitId(orderItem.getProductUnitId());
            outItem.setCount(remainCount);
            outItem.setProductPrice(orderItem.getProductPrice());
            outItem.setTaxPercent(orderItem.getTaxPercent());
            outItems.add(outItem);
        }

        if (outItems.isEmpty()) {
            throw exception(SALE_ORDER_ITEM_ALL_OUTED);
        }
        reqVO.setItems(outItems);

        // 6. 创建出库单
        Long saleOutId = erpSaleOutService.createSaleOut(reqVO);

        log.info("[createSaleOutFromRelease] 从发货放行创建出库单成功，orderId={}, saleOutId={}", orderId, saleOutId);
        return saleOutId;
    }

}
