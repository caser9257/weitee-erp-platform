package cn.iocoder.yudao.module.erp.controller.admin.mrp;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.stockreservation.ErpMrpStockReservationPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.stockreservation.ErpMrpStockReservationProjectSummaryRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.stockreservation.ErpMrpStockReservationRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.stockreservation.ErpMrpStockReservationSummaryPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.stockreservation.ErpMrpStockReservationSummaryRespVO;
import cn.iocoder.yudao.module.erp.service.mrp.ErpMrpStockReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - ERP MRP 库存占用追溯")
@RestController
@RequestMapping("/erp/mrp-stock-reservation")
@Validated
public class ErpMrpStockReservationController {

    @Resource
    private ErpMrpStockReservationService stockReservationService;

    @GetMapping("/page")
    @Operation(summary = "获得库存占用追溯分页")
    @PreAuthorize("@ss.hasPermission('erp:mrp-stock-reservation:query')")
    public CommonResult<PageResult<ErpMrpStockReservationRespVO>> getStockReservationPage(
            @Valid ErpMrpStockReservationPageReqVO pageReqVO) {
        return success(stockReservationService.getStockReservationPage(pageReqVO));
    }

    @GetMapping("/summary-page")
    @Operation(summary = "获得库存占用概览分页")
    @PreAuthorize("@ss.hasPermission('erp:mrp-stock-reservation:query')")
    public CommonResult<PageResult<ErpMrpStockReservationSummaryRespVO>> getStockReservationSummaryPage(
            @Valid ErpMrpStockReservationSummaryPageReqVO pageReqVO) {
        return success(stockReservationService.getStockReservationSummaryPage(pageReqVO));
    }

    @GetMapping("/summary-project-list")
    @Operation(summary = "获得物料的项目占用归属列表")
    @PreAuthorize("@ss.hasPermission('erp:mrp-stock-reservation:query')")
    public CommonResult<List<ErpMrpStockReservationProjectSummaryRespVO>> getStockReservationProjectSummaryList(
            @RequestParam("productId") Long productId) {
        return success(stockReservationService.getStockReservationProjectSummaryList(productId));
    }

}
