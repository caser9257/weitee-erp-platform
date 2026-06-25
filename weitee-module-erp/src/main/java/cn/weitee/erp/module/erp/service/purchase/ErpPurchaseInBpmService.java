package cn.weitee.erp.module.erp.service.purchase;

import cn.weitee.erp.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInCancelApprovalReqVO;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInSubmitReqVO;

public interface ErpPurchaseInBpmService {

    String submitPurchaseIn(Long userId, ErpPurchaseInSubmitReqVO reqVO);

    void cancelPurchaseInApproval(Long userId, ErpPurchaseInCancelApprovalReqVO reqVO);

    void handleProcessInstanceResult(Long purchaseInId, String processInstanceId, Integer status, String reason);

}
