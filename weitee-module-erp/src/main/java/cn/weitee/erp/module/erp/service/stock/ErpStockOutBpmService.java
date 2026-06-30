package cn.weitee.erp.module.erp.service.stock;

import cn.weitee.erp.module.erp.controller.admin.stock.vo.out.ErpStockOutCancelApprovalReqVO;
import cn.weitee.erp.module.erp.controller.admin.stock.vo.out.ErpStockOutSubmitReqVO;

public interface ErpStockOutBpmService {

    String submitStockOut(Long userId, ErpStockOutSubmitReqVO reqVO);

    void cancelStockOutApproval(Long userId, ErpStockOutCancelApprovalReqVO reqVO);

    void handleProcessInstanceResult(Long stockOutId, String processInstanceId, Integer status, String reason);

}