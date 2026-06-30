package cn.weitee.erp.module.erp.service.purchase;

import cn.weitee.erp.module.erp.controller.admin.purchase.vo.returns.ErpPurchaseReturnCancelApprovalReqVO;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.returns.ErpPurchaseReturnSubmitReqVO;

public interface ErpPurchaseReturnBpmService {

    String submitPurchaseReturn(Long userId, ErpPurchaseReturnSubmitReqVO reqVO);

    void cancelPurchaseReturnApproval(Long userId, ErpPurchaseReturnCancelApprovalReqVO reqVO);

    void handleProcessInstanceResult(Long purchaseReturnId, String processInstanceId, Integer status, String reason);

}
