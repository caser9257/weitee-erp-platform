package cn.weitee.erp.module.erp.controller.admin.sale;

import cn.hutool.core.collection.CollUtil;
import cn.weitee.erp.framework.apilog.core.annotation.ApiAccessLog;
import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.framework.common.pojo.PageParam;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.collection.MapUtils;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.framework.excel.core.util.ExcelUtils;
import cn.weitee.erp.module.erp.controller.admin.sale.vo.order.ErpSaleOrderBatchUpdateReqVO;
import cn.weitee.erp.module.erp.controller.admin.sale.vo.order.ErpSaleOrderBatchUpdateResultVO;
import cn.weitee.erp.module.erp.controller.admin.sale.vo.order.ErpSaleOrderCancelApprovalReqVO;
import cn.weitee.erp.module.erp.controller.admin.sale.vo.order.ErpSaleOrderClosurePageReqVO;
import cn.weitee.erp.module.erp.controller.admin.sale.vo.order.ErpSaleOrderClosurePageRespVO;
import cn.weitee.erp.module.erp.controller.admin.sale.vo.order.ErpSaleOrderClosureSummaryRespVO;
import cn.weitee.erp.module.erp.controller.admin.sale.vo.order.ErpSaleOrderPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.sale.vo.order.ErpSaleOrderRespVO;
import cn.weitee.erp.module.erp.controller.admin.sale.vo.order.ErpSaleOrderSaveReqVO;
import cn.weitee.erp.module.erp.controller.admin.sale.vo.order.ErpSaleOrderSubmitReqVO;
import cn.weitee.erp.module.erp.controller.admin.sale.vo.order.ErpSaleOrderUpdateStatusReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.project.ErpProjectDO;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpCustomerDO;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpSaleOrderDO;
import cn.weitee.erp.module.erp.service.project.ErpProjectService;
import cn.weitee.erp.module.erp.service.sale.ErpCustomerService;
import cn.weitee.erp.module.erp.service.sale.ErpSaleOrderClosureService;
import cn.weitee.erp.module.erp.service.sale.ErpSaleOrderBpmService;
import cn.weitee.erp.module.erp.service.sale.ErpSaleOrderService;
import cn.weitee.erp.module.system.api.user.AdminUserApi;
import cn.weitee.erp.module.system.api.user.dto.AdminUserRespDTO;
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
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.weitee.erp.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.weitee.erp.framework.common.pojo.CommonResult.success;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertList;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.weitee.erp.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - ERP 销售订单")
@RestController
@RequestMapping("/erp/sale-order")
@Validated
public class ErpSaleOrderController {

    @Resource
    private ErpSaleOrderService saleOrderService;
    @Resource
    private ErpSaleOrderBpmService saleOrderBpmService;
    @Resource
    private ErpSaleOrderDisplayService saleOrderDisplayService;
    @Resource
    private ErpSaleOrderClosureService saleOrderClosureService;
    @Resource
    private ErpCustomerService customerService;
    @Resource
    private ErpProjectService projectService;
    @Resource
    private AdminUserApi adminUserApi;

    @PostMapping("/create")
    @Operation(summary = "创建销售订单")
    @PreAuthorize("@ss.hasPermission('erp:sale-order:create')")
    public CommonResult<Long> createSaleOrder(@Valid @RequestBody ErpSaleOrderSaveReqVO createReqVO) {
        return success(saleOrderService.createSaleOrder(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新销售订单")
    @PreAuthorize("@ss.hasPermission('erp:sale-order:update')")
    public CommonResult<Boolean> updateSaleOrder(@Valid @RequestBody ErpSaleOrderSaveReqVO updateReqVO) {
        saleOrderService.updateSaleOrder(updateReqVO);
        return success(true);
    }

    @PutMapping("/batch-update")
    @Operation(summary = "批量修改销售订单")
    @PreAuthorize("@ss.hasPermission('erp:sale-order:update')")
    public CommonResult<ErpSaleOrderBatchUpdateResultVO> updateSaleOrderBatch(
            @Valid @RequestBody ErpSaleOrderBatchUpdateReqVO reqVO) {
        return success(saleOrderService.updateSaleOrderBatch(reqVO));
    }

    @PutMapping("/update-status")
    @Operation(summary = "更新销售订单的状态")
    @PreAuthorize("@ss.hasPermission('erp:sale-order:update-status')")
    public CommonResult<Boolean> updateSaleOrderStatus(@Valid @RequestBody ErpSaleOrderUpdateStatusReqVO reqVO) {
        saleOrderService.updateSaleOrderStatus(reqVO);
        return success(true);
    }

    @PostMapping("/submit")
    @Operation(summary = "提交销售订单审批")
    @PreAuthorize("@ss.hasPermission('erp:sale-order:update-status')")
    public CommonResult<String> submitSaleOrder(@Valid @RequestBody ErpSaleOrderSubmitReqVO reqVO) {
        return success(saleOrderBpmService.submitSaleOrder(getLoginUserId(), reqVO));
    }

    @DeleteMapping("/cancel-approval")
    @Operation(summary = "撤回销售订单审批")
    @PreAuthorize("@ss.hasPermission('erp:sale-order:update-status')")
    public CommonResult<Boolean> cancelSaleOrderApproval(@Valid @RequestBody ErpSaleOrderCancelApprovalReqVO reqVO) {
        saleOrderBpmService.cancelSaleOrderApproval(getLoginUserId(), reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除销售订单")
    @Parameter(name = "ids", description = "编号数组", required = true)
    @PreAuthorize("@ss.hasPermission('erp:sale-order:delete')")
    public CommonResult<Boolean> deleteSaleOrder(@RequestParam("ids") List<Long> ids) {
        saleOrderService.deleteSaleOrder(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得销售订单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:sale-order:query')")
    public CommonResult<ErpSaleOrderRespVO> getSaleOrder(@RequestParam("id") Long id) {
        return success(saleOrderDisplayService.getDetail(id));
    }

    @GetMapping("/get-closure-summary")
    @Operation(summary = "获得销售订单闭环摘要")
    @Parameter(name = "id", description = "销售单编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:sale-order:query')")
    public CommonResult<ErpSaleOrderClosureSummaryRespVO> getSaleOrderClosureSummary(@RequestParam("id") Long id) {
        return success(BeanUtils.toBean(
                saleOrderClosureService.getClosureSummary(id), ErpSaleOrderClosureSummaryRespVO.class));
    }

    @GetMapping("/get-closure-summary-page")
    @Operation(summary = "获得销售闭环工作台分页")
    @PreAuthorize("@ss.hasPermission('erp:sale-order:query')")
    public CommonResult<PageResult<ErpSaleOrderClosurePageRespVO>> getSaleOrderClosureSummaryPage(
            @Valid ErpSaleOrderClosurePageReqVO pageReqVO) {
        PageResult<ErpSaleOrderDO> pageResult = saleOrderService.getSaleOrderPage(pageReqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty(pageResult.getTotal()));
        }
        Set<Long> saleOrderIds = convertSet(pageResult.getList(), ErpSaleOrderDO::getId);
        Map<Long, ErpSaleOrderClosureSummaryRespVO> closureSummaryMap = saleOrderClosureService
                .getClosureSummaryMap(saleOrderIds).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> BeanUtils.toBean(entry.getValue(), ErpSaleOrderClosureSummaryRespVO.class)));
        Map<Long, ErpCustomerDO> customerMap = customerService.getCustomerMap(
                convertSet(pageResult.getList(), ErpSaleOrderDO::getCustomerId));
        Map<Long, ErpProjectDO> projectMap = projectService.getProjectMap(
                extractProjectIds(pageResult.getList()));
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
                convertSet(pageResult.getList(), ErpSaleOrderDO::getSaleUserId));
        List<ErpSaleOrderClosurePageRespVO> respList = convertList(pageResult.getList(), saleOrder -> {
            ErpSaleOrderClosurePageRespVO respVO = BeanUtils.toBean(saleOrder, ErpSaleOrderClosurePageRespVO.class);
            MapUtils.findAndThen(customerMap, saleOrder.getCustomerId(),
                    customer -> respVO.setCustomerName(customer.getName()));
            MapUtils.findAndThen(projectMap, saleOrder.getProjectId(),
                    project -> respVO.setProjectName(project.getName()));
            MapUtils.findAndThen(userMap, saleOrder.getSaleUserId(),
                    user -> respVO.setSaleUserName(user.getNickname()));
            respVO.setClosureSummary(closureSummaryMap.get(saleOrder.getId()));
            return respVO;
        });
        return success(new PageResult<>(respList, pageResult.getTotal()));
    }

    @GetMapping("/page")
    @Operation(summary = "获得销售订单分页")
    @PreAuthorize("@ss.hasPermission('erp:sale-order:query')")
    public CommonResult<PageResult<ErpSaleOrderRespVO>> getSaleOrderPage(@Valid ErpSaleOrderPageReqVO pageReqVO) {
        PageResult<ErpSaleOrderDO> pageResult = saleOrderService.getSaleOrderPage(pageReqVO);
        return success(saleOrderDisplayService.buildPageResult(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出销售订单 Excel")
    @PreAuthorize("@ss.hasPermission('erp:sale-order:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportSaleOrderExcel(@Valid ErpSaleOrderPageReqVO pageReqVO,
                                     HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ErpSaleOrderRespVO> list = saleOrderDisplayService.buildPageResult(
                saleOrderService.getSaleOrderPage(pageReqVO)).getList();
        ExcelUtils.write(response, "销售订单.xls", "数据", ErpSaleOrderRespVO.class, list);
    }

    private Set<Long> extractProjectIds(List<ErpSaleOrderDO> saleOrders) {
        return saleOrders.stream()
                .map(ErpSaleOrderDO::getProjectId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
}
