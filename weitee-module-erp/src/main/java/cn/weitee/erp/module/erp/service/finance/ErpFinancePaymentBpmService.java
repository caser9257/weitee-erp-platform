package cn.weitee.erp.module.erp.service.finance;

import cn.weitee.erp.module.erp.controller.admin.finance.vo.payment.ErpFinancePaymentCancelApprovalReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.payment.ErpFinancePaymentSubmitReqVO;

public interface ErpFinancePaymentBpmService {

    String submitFinancePayment(Long userId, ErpFinancePaymentSubmitReqVO reqVO);

    void cancelFinancePaymentApproval(Long userId, ErpFinancePaymentCancelApprovalReqVO reqVO);

    void handleProcessInstanceResult(Long paymentId, String processInstanceId, Integer status, String reason);

}
