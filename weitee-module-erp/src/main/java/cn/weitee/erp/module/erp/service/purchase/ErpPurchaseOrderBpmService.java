package cn.weitee.erp.module.erp.service.purchase;

import cn.weitee.erp.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderCancelApprovalReqVO;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderSubmitReqVO;

public interface ErpPurchaseOrderBpmService {

    String submitPurchaseOrder(Long userId, ErpPurchaseOrderSubmitReqVO reqVO);

    void cancelPurchaseOrderApproval(Long userId, ErpPurchaseOrderCancelApprovalReqVO reqVO);

}
