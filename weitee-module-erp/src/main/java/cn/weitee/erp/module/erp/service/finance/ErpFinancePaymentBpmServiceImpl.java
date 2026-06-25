package cn.weitee.erp.module.erp.service.finance;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.weitee.erp.module.bpm.service.approval.BpmApprovalRuntimeService;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.payment.ErpFinancePaymentCancelApprovalReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.payment.ErpFinancePaymentSubmitReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinancePaymentDO;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinancePaymentMapper;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.enums.ErpFinancePaymentBpmConstants;
import cn.weitee.erp.module.erp.util.ErpTransactionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.FINANCE_PAYMENT_APPROVE_FAIL;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.FINANCE_PAYMENT_BPM_CANCEL_FAIL;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.FINANCE_PAYMENT_BPM_SUBMIT_FAIL;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.FINANCE_PAYMENT_NOT_EXISTS;

@Service
@Validated
@Slf4j
public class ErpFinancePaymentBpmServiceImpl implements ErpFinancePaymentBpmService {

    @Resource
    private ErpFinancePaymentMapper erpFinancePaymentMapper;
    @Resource
    private ErpFinancePaymentService financePaymentService;

    @Resource
    private BpmApprovalRuntimeService approvalRuntimeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String submitFinancePayment(Long userId, ErpFinancePaymentSubmitReqVO reqVO) {
        ErpFinancePaymentDO payment = getRequiredFinancePayment(reqVO.getId());
        if (ObjectUtil.equal(payment.getStatus(), ErpAuditStatus.APPROVE.getStatus())) {
            throw exception(FINANCE_PAYMENT_APPROVE_FAIL);
        }
        if (ObjectUtil.equal(payment.getStatus(), ErpAuditStatus.PROCESS.getStatus())
                && StrUtil.isNotBlank(payment.getProcessInstanceId())) {
            throw exception(FINANCE_PAYMENT_BPM_SUBMIT_FAIL);
        }
        // 事务内：只写本地状态
        Long paymentId = payment.getId();
        erpFinancePaymentMapper.updateById(new ErpFinancePaymentDO()
                .setId(paymentId)
                .setStatus(ErpAuditStatus.PROCESS.getStatus())
                .setProcessInstanceId(null)); // 先清空，afterCommit 由 BPM 回调写入

        // 事务外：调 BPM 创建流程
        ErpTransactionUtils.afterCommit(() -> {
            try {
                String processInstanceId = approvalRuntimeService.submit(
                        "erp.finance.payment.submit", paymentId, userId);
                erpFinancePaymentMapper.updateById(new ErpFinancePaymentDO()
                        .setId(paymentId)
                        .setProcessInstanceId(processInstanceId));
            } catch (Exception e) {
                log.error("[submitFinancePayment] BPM 创建失败，paymentId={}", paymentId, e);
                erpFinancePaymentMapper.updateById(new ErpFinancePaymentDO()
                        .setId(paymentId)
                        .setStatus(ErpAuditStatus.FAILED.getStatus())
                        .setProcessInstanceId(null));
            }
        });
        return null; // processInstanceId 由 BPM 回调异步写入
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelFinancePaymentApproval(Long userId, ErpFinancePaymentCancelApprovalReqVO reqVO) {
        ErpFinancePaymentDO payment = getRequiredFinancePayment(reqVO.getId());
        if (!ObjectUtil.equal(payment.getStatus(), ErpAuditStatus.PROCESS.getStatus())
                || StrUtil.isBlank(payment.getProcessInstanceId())) {
            throw exception(FINANCE_PAYMENT_BPM_CANCEL_FAIL);
        }
        String processInstanceId = payment.getProcessInstanceId();
        ErpTransactionUtils.afterCommit(() -> {
            try {
                approvalRuntimeService.cancel("erp.finance.payment.submit", reqVO.getId(), userId, reqVO.getReason());
            } catch (Exception e) {
                log.warn("[cancelFinancePaymentApproval] BPM 撤回失败，paymentId={}, processInstanceId={}",
                        reqVO.getId(), processInstanceId, e);
            }
        });
    }

    @Override
    public void handleProcessInstanceResult(Long paymentId, String processInstanceId, Integer status, String reason) {
        // 旧监听器入口保留兼容，结果回写已收敛到 FinancePaymentResultHandler
    }

    private Map<String, Object> buildVariables(ErpFinancePaymentDO payment) {
        Map<String, Object> variables = new HashMap<>();
        variables.put(ErpFinancePaymentBpmConstants.VARIABLE_PAYMENT_ID, payment.getId());
        variables.put(ErpFinancePaymentBpmConstants.VARIABLE_PAYMENT_NO, payment.getNo());
        variables.put(ErpFinancePaymentBpmConstants.VARIABLE_PAYMENT_TIME, payment.getPaymentTime());
        variables.put(ErpFinancePaymentBpmConstants.VARIABLE_TOTAL_PRICE, defaultAmount(payment.getTotalPrice()));
        variables.put(ErpFinancePaymentBpmConstants.VARIABLE_DISCOUNT_PRICE, defaultAmount(payment.getDiscountPrice()));
        variables.put(ErpFinancePaymentBpmConstants.VARIABLE_PAYMENT_PRICE, defaultAmount(payment.getPaymentPrice()));
        variables.put(ErpFinancePaymentBpmConstants.VARIABLE_SUPPLIER_ID, payment.getSupplierId());
        variables.put(ErpFinancePaymentBpmConstants.VARIABLE_ACCOUNT_ID, payment.getAccountId());
        variables.put(ErpFinancePaymentBpmConstants.VARIABLE_FINANCE_USER_ID, payment.getFinanceUserId());
        variables.put(ErpFinancePaymentBpmConstants.VARIABLE_CREATOR_ID, parseCreatorId(payment.getCreator()));
        return variables;
    }

    private BigDecimal defaultAmount(BigDecimal amount) {
        return ObjectUtil.defaultIfNull(amount, BigDecimal.ZERO);
    }

    private Long parseCreatorId(String creator) {
        return StrUtil.isNumeric(creator) ? Long.valueOf(creator) : null;
    }

    private ErpFinancePaymentDO getRequiredFinancePayment(Long paymentId) {
        ErpFinancePaymentDO payment = erpFinancePaymentMapper.selectById(paymentId);
        if (payment == null) {
            throw exception(FINANCE_PAYMENT_NOT_EXISTS);
        }
        return payment;
    }

}
