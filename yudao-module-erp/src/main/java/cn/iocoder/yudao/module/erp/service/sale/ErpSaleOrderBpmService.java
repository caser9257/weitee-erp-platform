package cn.iocoder.yudao.module.erp.service.sale;

import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.order.ErpSaleOrderCancelApprovalReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.order.ErpSaleOrderSubmitReqVO;

public interface ErpSaleOrderBpmService {

    String submitSaleOrder(Long userId, ErpSaleOrderSubmitReqVO reqVO);

    void cancelSaleOrderApproval(Long userId, ErpSaleOrderCancelApprovalReqVO reqVO);

    void handleProcessInstanceResult(Long orderId, String processInstanceId, Integer status, String reason);

}
