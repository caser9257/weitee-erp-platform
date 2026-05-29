package cn.iocoder.yudao.module.erp.service.purchase;

import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInCancelApprovalReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInSubmitReqVO;

public interface ErpPurchaseInBpmService {

    String submitPurchaseIn(Long userId, ErpPurchaseInSubmitReqVO reqVO);

    void cancelPurchaseInApproval(Long userId, ErpPurchaseInCancelApprovalReqVO reqVO);

    void handleProcessInstanceResult(Long purchaseInId, String processInstanceId, Integer status, String reason);

}
