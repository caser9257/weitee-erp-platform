package cn.iocoder.yudao.module.erp.service.purchase;

import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderCancelApprovalReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderSubmitReqVO;

public interface ErpPurchaseOrderBpmService {

    String submitPurchaseOrder(Long userId, ErpPurchaseOrderSubmitReqVO reqVO);

    void cancelPurchaseOrderApproval(Long userId, ErpPurchaseOrderCancelApprovalReqVO reqVO);

    void handleProcessInstanceResult(Long orderId, String processInstanceId, Integer status, String reason);

}
