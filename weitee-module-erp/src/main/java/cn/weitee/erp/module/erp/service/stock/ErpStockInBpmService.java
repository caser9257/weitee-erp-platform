package cn.weitee.erp.module.erp.service.stock;

import cn.weitee.erp.module.erp.controller.admin.stock.vo.in.ErpStockInCancelApprovalReqVO;
import cn.weitee.erp.module.erp.controller.admin.stock.vo.in.ErpStockInSubmitReqVO;

public interface ErpStockInBpmService {

    String submitStockIn(Long userId, ErpStockInSubmitReqVO reqVO);

    void cancelStockInApproval(Long userId, ErpStockInCancelApprovalReqVO reqVO);

    void handleProcessInstanceResult(Long stockInId, String processInstanceId, Integer status, String reason);

}
