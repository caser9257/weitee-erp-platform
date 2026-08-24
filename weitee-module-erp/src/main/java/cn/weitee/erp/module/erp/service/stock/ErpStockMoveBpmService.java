package cn.weitee.erp.module.erp.service.stock;

import cn.weitee.erp.module.erp.controller.admin.stock.vo.move.ErpStockMoveCancelApprovalReqVO;
import cn.weitee.erp.module.erp.controller.admin.stock.vo.move.ErpStockMoveSubmitReqVO;

public interface ErpStockMoveBpmService {

    void submitStockMove(Long userId, ErpStockMoveSubmitReqVO reqVO);

    void cancelStockMoveApproval(Long userId, ErpStockMoveCancelApprovalReqVO reqVO);
}
