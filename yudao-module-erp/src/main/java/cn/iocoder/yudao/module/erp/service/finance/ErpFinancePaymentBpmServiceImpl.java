package cn.iocoder.yudao.module.erp.service.finance;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.bpm.service.approval.BpmApprovalRuntimeService;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.payment.ErpFinancePaymentCancelApprovalReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.payment.ErpFinancePaymentSubmitReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinancePaymentDO;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinancePaymentMapper;
import cn.iocoder.yudao.module.erp.enums.ErpAuditStatus;
import cn.iocoder.yudao.module.erp.enums.ErpFinancePaymentBpmConstants;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.FINANCE_PAYMENT_APPROVE_FAIL;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.FINANCE_PAYMENT_BPM_CANCEL_FAIL;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.FINANCE_PAYMENT_BPM_SUBMIT_FAIL;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.FINANCE_PAYMENT_NOT_EXISTS;

@Service
@Validated
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
        String processInstanceId = approvalRuntimeService.submit("erp.finance.payment.submit", payment.getId(), userId);
        erpFinancePaymentMapper.updateById(new ErpFinancePaymentDO()
                .setId(payment.getId())
                .setStatus(ErpAuditStatus.PROCESS.getStatus())
                .setProcessInstanceId(processInstanceId));
        return processInstanceId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelFinancePaymentApproval(Long userId, ErpFinancePaymentCancelApprovalReqVO reqVO) {
        ErpFinancePaymentDO payment = getRequiredFinancePayment(reqVO.getId());
        if (!ObjectUtil.equal(payment.getStatus(), ErpAuditStatus.PROCESS.getStatus())
                || StrUtil.isBlank(payment.getProcessInstanceId())) {
            throw exception(FINANCE_PAYMENT_BPM_CANCEL_FAIL);
        }
        approvalRuntimeService.cancel("erp.finance.payment.submit", payment.getId(), userId, reqVO.getReason());
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
