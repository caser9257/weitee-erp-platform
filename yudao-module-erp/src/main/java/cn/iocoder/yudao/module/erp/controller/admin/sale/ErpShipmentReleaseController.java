package cn.iocoder.yudao.module.erp.controller.admin.sale;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.ShipmentReleaseCheckReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.ShipmentReleasePageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.ShipmentReleasePageVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.ShipmentReleaseResultVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.ShipmentReleaseStatsVO;
import cn.iocoder.yudao.module.erp.service.sale.ErpShipmentReleaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * 发货放行审核 Controller
 *
 * @author system
 */
@Tag(name = "ERP - 发货放行审核")
@RestController
@RequestMapping("/erp/shipment-release")
@Validated
@Slf4j
public class ErpShipmentReleaseController {

    @Resource
    private ErpShipmentReleaseService erpShipmentReleaseService;

    @GetMapping("/page")
    @PreAuthorize("@ss.hasPermission('erp:shipment-release:query')")
    @Operation(summary = "分页查询发货放行订单")
    public CommonResult<PageResult<ShipmentReleasePageVO>> getReleasePage(@Validated ShipmentReleasePageReqVO reqVO) {
        return success(erpShipmentReleaseService.getReleasePage(reqVO));
    }

    @GetMapping("/stats")
    @PreAuthorize("@ss.hasPermission('erp:shipment-release:query')")
    @Operation(summary = "获取发货放行统计")
    public CommonResult<ShipmentReleaseStatsVO> getReleaseStats() {
        return success(erpShipmentReleaseService.getReleaseStats());
    }

    @PostMapping("/check")
    @PreAuthorize("@ss.hasPermission('erp:shipment-release:check')")
    @Operation(summary = "校验发货放行")
    public CommonResult<ShipmentReleaseResultVO> checkRelease(@Validated @RequestBody ShipmentReleaseCheckReqVO reqVO) {
        ShipmentReleaseResultVO result = erpShipmentReleaseService.checkRelease(reqVO.getOrderId());
        return success(result);
    }

    @PostMapping("/submit-finance")
    @PreAuthorize("@ss.hasPermission('erp:shipment-release:submit')")
    @Operation(summary = "提交财务审核")
    public CommonResult<Boolean> submitFinanceApproval(@RequestParam("orderId") Long orderId) {
        Long approverId = getLoginUserId();
        erpShipmentReleaseService.submitFinanceApproval(orderId, approverId);
        return success(true);
    }

    @PostMapping("/approve")
    @PreAuthorize("@ss.hasPermission('erp:shipment-release:approve')")
    @Operation(summary = "财务审核通过")
    public CommonResult<Boolean> approveFinance(
            @RequestParam("orderId") Long orderId,
            @RequestParam(value = "remark", required = false) String remark) {
        Long approverId = getLoginUserId();
        erpShipmentReleaseService.approveFinance(orderId, approverId, remark);
        return success(true);
    }

    @PostMapping("/reject")
    @PreAuthorize("@ss.hasPermission('erp:shipment-release:reject')")
    @Operation(summary = "财务审核驳回")
    public CommonResult<Boolean> rejectFinance(
            @RequestParam("orderId") Long orderId,
            @RequestParam("reason") String reason) {
        Long approverId = getLoginUserId();
        erpShipmentReleaseService.rejectFinance(orderId, approverId, reason);
        return success(true);
    }

    @PostMapping("/create-sale-out")
    @PreAuthorize("@ss.hasPermission('erp:shipment-release:check')")
    @Operation(summary = "从放行创建出库单")
    public CommonResult<Long> createSaleOutFromRelease(
            @RequestParam("orderId") Long orderId,
            @RequestParam("warehouseId") Long warehouseId) {
        Long saleOutId = erpShipmentReleaseService.createSaleOutFromRelease(orderId, warehouseId, getLoginUserId());
        return success(saleOutId);
    }

}
