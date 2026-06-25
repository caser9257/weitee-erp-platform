package cn.weitee.erp.module.erp.service.mrp;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.stockreservation.ErpMrpStockReservationPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.stockreservation.ErpMrpStockReservationProjectSummaryRespVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.stockreservation.ErpMrpStockReservationRespVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.stockreservation.ErpMrpStockReservationSummaryPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.stockreservation.ErpMrpStockReservationSummaryRespVO;

import java.util.List;

public interface ErpMrpStockReservationService {

    PageResult<ErpMrpStockReservationRespVO> getStockReservationPage(ErpMrpStockReservationPageReqVO pageReqVO);

    PageResult<ErpMrpStockReservationSummaryRespVO> getStockReservationSummaryPage(
            ErpMrpStockReservationSummaryPageReqVO pageReqVO);

    List<ErpMrpStockReservationProjectSummaryRespVO> getStockReservationProjectSummaryList(Long productId);

}
