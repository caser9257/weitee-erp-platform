package cn.weitee.erp.module.erp.service.finance.approval;

import cn.hutool.core.util.StrUtil;
import cn.weitee.erp.module.bpm.service.approval.provider.ApprovalContext;
import cn.weitee.erp.module.bpm.service.approval.provider.ApprovalContextProvider;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinancePaymentDO;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinancePaymentMapper;
import cn.weitee.erp.module.erp.enums.ErpFinancePaymentBpmConstants;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.FINANCE_PAYMENT_NOT_EXISTS;

/**
 * 付款单审批上下文提供者
 */
@Component
public class FinancePaymentContextProvider implements ApprovalContextProvider {

    @Resource
    private ErpFinancePaymentMapper financePaymentMapper;

    @Override
    public String getSceneCode() {
        return "erp.finance.payment.submit";
    }

    @Override
    public ApprovalContext getContext(Long bizId) {
        ErpFinancePaymentDO payment = financePaymentMapper.selectById(bizId);
        if (payment == null) {
            throw exception(FINANCE_PAYMENT_NOT_EXISTS);
        }

        // 构建流程变量
        Map<String, Object> variables = new HashMap<>();
        variables.put(ErpFinancePaymentBpmConstants.VARIABLE_PAYMENT_ID, payment.getId());
        variables.put(ErpFinancePaymentBpmConstants.VARIABLE_PAYMENT_NO, payment.getNo());
        variables.put(ErpFinancePaymentBpmConstants.VARIABLE_PAYMENT_TIME, payment.getPaymentTime());
        variables.put(ErpFinancePaymentBpmConstants.VARIABLE_TOTAL_PRICE, payment.getTotalPrice());
        variables.put(ErpFinancePaymentBpmConstants.VARIABLE_DISCOUNT_PRICE, payment.getDiscountPrice());
        variables.put(ErpFinancePaymentBpmConstants.VARIABLE_PAYMENT_PRICE, payment.getPaymentPrice());
        variables.put(ErpFinancePaymentBpmConstants.VARIABLE_SUPPLIER_ID, payment.getSupplierId());
        variables.put(ErpFinancePaymentBpmConstants.VARIABLE_ACCOUNT_ID, payment.getAccountId());
        variables.put(ErpFinancePaymentBpmConstants.VARIABLE_FINANCE_USER_ID, payment.getFinanceUserId());

        // 构建通知参数
        Map<String, Object> notifyParams = new HashMap<>();
        notifyParams.put("bizTitle", "付款单 " + payment.getNo());
        notifyParams.put("bizNo", payment.getNo());
        notifyParams.put("amount", payment.getPaymentPrice());

        return ApprovalContext.builder()
                .bizId(payment.getId())
                .bizNo(payment.getNo())
                .bizTitle("付款单 " + payment.getNo())
                .amount(payment.getPaymentPrice())
                .startUserId(parseCreatorId(payment.getCreator()))
                .detailUrl("/finance/payment/detail?id=" + payment.getId())
                .variables(variables)
                .notifyParams(notifyParams)
                .build();
    }

    private Long parseCreatorId(String creator) {
        return creator != null && StrUtil.isNumeric(creator) ? Long.valueOf(creator) : null;
    }
}
