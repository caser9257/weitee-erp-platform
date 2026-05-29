package cn.iocoder.yudao.module.erp.service.mrp;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.stockreservation.ErpMrpStockReservationPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.stockreservation.ErpMrpStockReservationProjectSummaryRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.stockreservation.ErpMrpStockReservationRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.stockreservation.ErpMrpStockReservationSummaryPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.stockreservation.ErpMrpStockReservationSummaryRespVO;

import java.util.List;

public interface ErpMrpStockReservationService {

    PageResult<ErpMrpStockReservationRespVO> getStockReservationPage(ErpMrpStockReservationPageReqVO pageReqVO);

    PageResult<ErpMrpStockReservationSummaryRespVO> getStockReservationSummaryPage(
            ErpMrpStockReservationSummaryPageReqVO pageReqVO);

    List<ErpMrpStockReservationProjectSummaryRespVO> getStockReservationProjectSummaryList(Long productId);

}
