package cn.weitee.erp.module.erp.service.sale;

import cn.weitee.erp.module.erp.controller.admin.sale.vo.order.ErpSaleOrderCancelApprovalReqVO;
import cn.weitee.erp.module.erp.controller.admin.sale.vo.order.ErpSaleOrderSubmitReqVO;

public interface ErpSaleOrderBpmService {

    String submitSaleOrder(Long userId, ErpSaleOrderSubmitReqVO reqVO);

    void cancelSaleOrderApproval(Long userId, ErpSaleOrderCancelApprovalReqVO reqVO);

}
