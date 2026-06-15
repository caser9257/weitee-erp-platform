package cn.iocoder.yudao.module.erp.service.sale;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.ShipmentReleaseResultVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOrderDO;
import cn.iocoder.yudao.module.erp.dal.mysql.sale.ErpSaleOrderMapper;
import cn.iocoder.yudao.module.erp.enums.ErpAuditStatus;
import cn.iocoder.yudao.module.erp.enums.ShipmentReleaseRule;
import cn.iocoder.yudao.module.erp.service.finance.ErpFinanceReceiptService;
import cn.iocoder.yudao.module.crm.service.contract.CrmContractService;
import cn.iocoder.yudao.module.crm.dal.dataobject.contract.CrmContractDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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
    private ErpFinanceReceiptService erpFinanceReceiptService;

    @Resource
    private CrmContractService crmContractService;

    @Override
    public ShipmentReleaseResultVO checkRelease(Long orderId) {
        // 1. 获取订单信息
        ErpSaleOrderDO order = erpSaleOrderMapper.selectById(orderId);
        if (order == null) {
            return buildBlockedResult("订单不存在");
        }

        List<ShipmentReleaseResultVO.ReleaseCheckDetailVO> details = new ArrayList<>();
        List<String> blockerReasons = new ArrayList<>();

        // 2. 校验订单状态
        ShipmentReleaseResultVO.ReleaseCheckDetailVO orderStatusCheck = checkOrderStatus(order);
        details.add(orderStatusCheck);
        if (!orderStatusCheck.isPassed()) {
            blockerReasons.add(orderStatusCheck.getMessage());
            return buildResult(false, "BLOCKED", blockerReasons, null, details);
        }

        // 3. 校验合同关联
        if (order.getContractId() == null) {
            ShipmentReleaseResultVO.ReleaseCheckDetailVO contractCheck = new ShipmentReleaseResultVO.ReleaseCheckDetailVO();
            contractCheck.setCheckItem("合同关联校验");
            contractCheck.setPassed(false);
            contractCheck.setMessage("订单未关联合同");
            details.add(contractCheck);
            blockerReasons.add("订单未关联合同");
            return buildResult(false, "BLOCKED", blockerReasons, null, details);
        }

        // 4. 从合同服务获取放行规则
        String releaseRule = "SIGN_AND_SHIP"; // 默认签约即发
        BigDecimal prepaymentRatio = null;
        try {
            CrmContractDO contract = crmContractService.getContract(order.getContractId());
            if (contract != null) {
                if (contract.getShipmentReleaseRule() != null) {
                    releaseRule = contract.getShipmentReleaseRule();
                }
                prepaymentRatio = contract.getPrepaymentRatio();
                log.info("[checkRelease] 订单[{}]关联合同[{}]，放行规则：{}，预付款比例：{}",
                        orderId, contract.getNo(), releaseRule, prepaymentRatio);
            }
        } catch (Exception e) {
            log.warn("[checkRelease] 获取合同信息失败，使用默认放行规则，contractId={}", order.getContractId(), e);
        }

        // 5. 校验放行规则
        ShipmentReleaseResultVO.ReleaseCheckDetailVO ruleCheck = checkReleaseRule(order, releaseRule, prepaymentRatio);
        details.add(ruleCheck);
        if (!ruleCheck.isPassed()) {
            blockerReasons.add(ruleCheck.getMessage());
            return buildResult(false, "BLOCKED", blockerReasons, null, details);
        }

        // 6. 通用校验：财务审核
        ShipmentReleaseResultVO.ReleaseCheckDetailVO financeCheck = checkFinanceApproval(order);
        details.add(financeCheck);
        if (!financeCheck.isPassed()) {
            blockerReasons.add(financeCheck.getMessage());
            return buildResult(false, "FINANCE_REVIEW", blockerReasons, "财务人员", details);
        }

        // 7. 所有校验通过
        return buildResult(true, "RELEASED", new ArrayList<>(), null, details);
    }

    @Override
    public void submitFinanceApproval(Long orderId, Long approverId) {
        // 更新订单状态为待财务审核
        ErpSaleOrderDO order = erpSaleOrderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        ErpSaleOrderDO updateOrder = new ErpSaleOrderDO();
        updateOrder.setId(orderId);
        updateOrder.setShipmentReleaseStatus("FINANCE_REVIEW");
        erpSaleOrderMapper.updateById(updateOrder);

        log.info("订单[{}]已提交财务审核，审核人[{}]", orderId, approverId);
    }

    @Override
    public void approveFinance(Long orderId, Long approverId, String remark) {
        // 更新订单状态为已放行
        ErpSaleOrderDO order = erpSaleOrderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        ErpSaleOrderDO updateOrder = new ErpSaleOrderDO();
        updateOrder.setId(orderId);
        updateOrder.setShipmentReleaseStatus("RELEASED");
        updateOrder.setShipmentReleaseReason(null);
        erpSaleOrderMapper.updateById(updateOrder);

        log.info("订单[{}]财务审核通过，审核人[{}]，意见[{}]", orderId, approverId, remark);
    }

    @Override
    public void rejectFinance(Long orderId, Long approverId, String reason) {
        // 更新订单状态为阻塞
        ErpSaleOrderDO order = erpSaleOrderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

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
    private ShipmentReleaseResultVO.ReleaseCheckDetailVO checkReleaseRule(ErpSaleOrderDO order, String releaseRule, BigDecimal prepaymentRatio) {
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
            return checkPaymentReceived(order, detail);
        }

        // 达到预付款比例后发：检查预付款金额
        if ("AFTER_PREPAYMENT".equals(releaseRule)) {
            return checkPrepaymentReceived(order, detail, prepaymentRatio);
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
                                                                               ShipmentReleaseResultVO.ReleaseCheckDetailVO detail) {
        // 查询该订单的实际收款金额
        BigDecimal receivedAmount = erpFinanceReceiptService.getReceivedAmountByOrderId(order.getId());
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
                                                                                   BigDecimal prepaymentRatio) {
        // 查询该订单的实际收款金额
        BigDecimal receivedAmount = erpFinanceReceiptService.getReceivedAmountByOrderId(order.getId());
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

}
