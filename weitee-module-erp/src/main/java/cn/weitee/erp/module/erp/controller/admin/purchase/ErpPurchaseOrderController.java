package cn.weitee.erp.module.erp.controller.admin.purchase;

import cn.weitee.erp.framework.apilog.core.annotation.ApiAccessLog;
import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.framework.common.pojo.PageParam;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.excel.core.util.ExcelUtils;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderBatchUpdateReqVO;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderBatchUpdateResultVO;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderCancelApprovalReqVO;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderExportItemRespVO;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderAuditLogRespVO;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderRejectLogRespVO;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderRespVO;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderSaveReqVO;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderSubmitReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseOrderDO;
import cn.weitee.erp.module.erp.service.purchase.ErpPurchaseOrderBpmService;
import cn.weitee.erp.module.erp.service.purchase.ErpPurchaseOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;

import static cn.weitee.erp.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.weitee.erp.framework.common.pojo.CommonResult.success;
import static cn.weitee.erp.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - ERP 采购订单")
@RestController
@RequestMapping("/erp/purchase-order")
@Validated
public class ErpPurchaseOrderController {

    @Resource
    private ErpPurchaseOrderService purchaseOrderService;
    @Resource
    private ErpPurchaseOrderBpmService purchaseOrderBpmService;
    @Resource
    private ErpPurchaseOrderDisplayService purchaseOrderDisplayService;

    @PostMapping("/create")
    @Operation(summary = "创建采购订单")
    @PreAuthorize("@ss.hasPermission('erp:purchase-order:create')")
    public CommonResult<Long> createPurchaseOrder(@Valid @RequestBody ErpPurchaseOrderSaveReqVO createReqVO) {
        return success(purchaseOrderService.createPurchaseOrder(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新采购订单")
    @PreAuthorize("@ss.hasPermission('erp:purchase-order:update')")
    public CommonResult<Boolean> updatePurchaseOrder(@Valid @RequestBody ErpPurchaseOrderSaveReqVO updateReqVO) {
        purchaseOrderService.updatePurchaseOrder(updateReqVO);
        return success(true);
    }

    @PutMapping("/batch-update")
    @Operation(summary = "批量更新采购订单")
    @PreAuthorize("@ss.hasPermission('erp:purchase-order:update')")
    public CommonResult<ErpPurchaseOrderBatchUpdateResultVO> updatePurchaseOrderBatch(
            @Valid @RequestBody ErpPurchaseOrderBatchUpdateReqVO reqVO) {
        return success(purchaseOrderService.updatePurchaseOrderBatch(reqVO));
    }

    @PutMapping("/update-status")
    @Operation(summary = "更新采购订单状态")
    @PreAuthorize("@ss.hasPermission('erp:purchase-order:update-status')")
    public CommonResult<Boolean> updatePurchaseOrderStatus(@RequestParam("id") Long id,
                                                           @RequestParam("status") Integer status) {
        purchaseOrderService.updatePurchaseOrderStatus(id, status);
        return success(true);
    }

    @PostMapping("/submit")
    @Operation(summary = "提交采购订单审批")
    @PreAuthorize("@ss.hasPermission('erp:purchase-order:submit')")
    public CommonResult<String> submitPurchaseOrder(@Valid @RequestBody ErpPurchaseOrderSubmitReqVO reqVO) {
        return success(purchaseOrderBpmService.submitPurchaseOrder(getLoginUserId(), reqVO));
    }

    @DeleteMapping("/cancel-approval")
    @Operation(summary = "撤回采购订单审批")
    @PreAuthorize("@ss.hasPermission('erp:purchase-order:cancel-approval')")
    public CommonResult<Boolean> cancelPurchaseOrderApproval(@Valid @RequestBody ErpPurchaseOrderCancelApprovalReqVO reqVO) {
        purchaseOrderBpmService.cancelPurchaseOrderApproval(getLoginUserId(), reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除采购订单")
    @Parameter(name = "ids", description = "编号数组", required = true)
    @PreAuthorize("@ss.hasPermission('erp:purchase-order:delete')")
    public CommonResult<Boolean> deletePurchaseOrder(@RequestParam("ids") List<Long> ids) {
        purchaseOrderService.deletePurchaseOrder(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得采购订单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:purchase-order:query')")
    public CommonResult<ErpPurchaseOrderRespVO> getPurchaseOrder(@RequestParam("id") Long id) {
        ErpPurchaseOrderRespVO result = purchaseOrderDisplayService.getDetail(id);
        return success(result);
    }

    @GetMapping("/page")
    @Operation(summary = "获得采购订单分页")
    @PreAuthorize("@ss.hasPermission('erp:purchase-order:query')")
    public CommonResult<PageResult<ErpPurchaseOrderRespVO>> getPurchaseOrderPage(@Valid ErpPurchaseOrderPageReqVO pageReqVO) {
        PageResult<ErpPurchaseOrderDO> pageResult = purchaseOrderService.getPurchaseOrderPage(pageReqVO);
        return success(purchaseOrderDisplayService.buildPageResult(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出采购订单 Excel")
    @PreAuthorize("@ss.hasPermission('erp:purchase-order:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportPurchaseOrderExcel(@Valid ErpPurchaseOrderPageReqVO pageReqVO,
                                          HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ErpPurchaseOrderRespVO> list = purchaseOrderDisplayService.buildPageResult(
                purchaseOrderService.getPurchaseOrderPage(pageReqVO)).getList();
        ExcelUtils.write(response, "采购订单.xls", "数据", ErpPurchaseOrderExportItemRespVO.class,
                purchaseOrderDisplayService.buildExportItemList(list));
    }
}
