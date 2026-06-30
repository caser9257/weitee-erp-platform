package cn.weitee.erp.module.erp.controller.admin.sale;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.weitee.erp.framework.apilog.core.annotation.ApiAccessLog;
import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.framework.common.pojo.PageParam;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.collection.MapUtils;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.framework.excel.core.util.ExcelUtils;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.weitee.erp.module.erp.controller.admin.sale.vo.order.ErpSaleOrderAuditLogRespVO;
import cn.weitee.erp.module.erp.controller.admin.sale.vo.order.ErpSaleOrderBatchUpdateReqVO;
import cn.weitee.erp.module.erp.controller.admin.sale.vo.order.ErpSaleOrderBatchUpdateResultVO;
import cn.weitee.erp.module.erp.controller.admin.sale.vo.order.ErpSaleOrderCancelApprovalReqVO;
import cn.weitee.erp.module.erp.controller.admin.sale.vo.order.ErpSaleOrderClosurePageReqVO;
import cn.weitee.erp.module.erp.controller.admin.sale.vo.order.ErpSaleOrderClosurePageRespVO;
import cn.weitee.erp.module.erp.controller.admin.sale.vo.order.ErpSaleOrderClosureSummaryRespVO;
import cn.weitee.erp.module.erp.controller.admin.sale.vo.order.ErpSaleOrderPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.sale.vo.order.ErpSaleOrderRejectLogRespVO;
import cn.weitee.erp.module.erp.controller.admin.sale.vo.order.ErpSaleOrderRespVO;
import cn.weitee.erp.module.erp.controller.admin.sale.vo.order.ErpSaleOrderSaveReqVO;
import cn.weitee.erp.module.erp.controller.admin.sale.vo.order.ErpSaleOrderSubmitReqVO;
import cn.weitee.erp.module.erp.controller.admin.sale.vo.order.ErpSaleOrderUpdateStatusReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.project.ErpProjectDO;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpCustomerDO;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpSaleOrderAuditLogDO;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpSaleOrderDO;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpSaleOrderItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpSaleOrderRejectLogDO;
import cn.weitee.erp.module.erp.service.product.ErpProductService;
import cn.weitee.erp.module.erp.service.project.ErpProjectService;
import cn.weitee.erp.module.erp.service.sale.ErpCustomerService;
import cn.weitee.erp.module.erp.service.sale.ErpSaleOrderClosureService;
import cn.weitee.erp.module.erp.service.sale.ErpSaleOrderBpmService;
import cn.weitee.erp.module.erp.service.sale.ErpSaleOrderService;
import cn.weitee.erp.module.erp.service.stock.ErpStockService;
import cn.weitee.erp.module.erp.util.ErpUserIdUtils;
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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.weitee.erp.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.weitee.erp.framework.common.pojo.CommonResult.success;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertList;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertMultiMap;
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
    private ErpStockService stockService;
    @Resource
    private ErpProductService productService;
    @Resource
    private ErpCustomerService customerService;
    @Resource
    private ErpSaleOrderClosureService saleOrderClosureService;
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
        ErpSaleOrderDO saleOrder = saleOrderService.getSaleOrder(id);
        if (saleOrder == null) {
            return success(null);
        }
        List<ErpSaleOrderItemDO> saleOrderItemList = saleOrderService.getSaleOrderItemListByOrderId(id);
        List<ErpSaleOrderAuditLogDO> auditLogs = saleOrderService.getSaleOrderAuditLogListByOrderId(id);
        List<ErpSaleOrderRejectLogDO> rejectLogs = saleOrderService.getSaleOrderRejectLogListByOrderId(id);
        Map<Long, BigDecimal> stockCountMap = stockService.getStockCountMap(
                convertSet(saleOrderItemList, ErpSaleOrderItemDO::getProductId));
        Map<Long, ErpProductRespVO> productMap = productService.getProductVOMap(
                convertSet(saleOrderItemList, ErpSaleOrderItemDO::getProductId));
        Map<Long, ErpCustomerDO> customerMap = customerService.getCustomerMap(List.of(saleOrder.getCustomerId()));
        Map<Long, ErpProjectDO> projectMap = projectService.getProjectMap(
                saleOrder.getProjectId() == null ? List.of() : List.of(saleOrder.getProjectId()));
        Set<Long> userIds = extractCreatorIds(saleOrder, auditLogs, rejectLogs);
        if (saleOrder.getSaleUserId() != null) {
            userIds.add(saleOrder.getSaleUserId());
        }
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(userIds);
        ErpSaleOrderRespVO respVO = buildSaleOrderRespVO(saleOrder, saleOrderItemList, stockCountMap, auditLogs, rejectLogs,
                productMap, customerMap, projectMap, userMap);
        respVO.setClosureSummary(BeanUtils.toBean(
                saleOrderClosureService.getClosureSummary(id), ErpSaleOrderRespVO.ClosureSummary.class));
        return success(respVO);
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
        return success(buildSaleOrderVOPageResult(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出销售订单 Excel")
    @PreAuthorize("@ss.hasPermission('erp:sale-order:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportSaleOrderExcel(@Valid ErpSaleOrderPageReqVO pageReqVO,
                                     HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ErpSaleOrderRespVO> list = buildSaleOrderVOPageResult(saleOrderService.getSaleOrderPage(pageReqVO)).getList();
        ExcelUtils.write(response, "销售订单.xls", "数据", ErpSaleOrderRespVO.class, list);
    }

    private PageResult<ErpSaleOrderRespVO> buildSaleOrderVOPageResult(PageResult<ErpSaleOrderDO> pageResult) {
        if (CollUtil.isEmpty(pageResult.getList())) {
            return PageResult.empty(pageResult.getTotal());
        }
        List<ErpSaleOrderItemDO> saleOrderItemList = saleOrderService.getSaleOrderItemListByOrderIds(
                convertSet(pageResult.getList(), ErpSaleOrderDO::getId));
        Map<Long, List<ErpSaleOrderItemDO>> saleOrderItemMap = convertMultiMap(saleOrderItemList, ErpSaleOrderItemDO::getOrderId);
        Map<Long, BigDecimal> stockCountMap = stockService.getStockCountMap(
                convertSet(saleOrderItemList, ErpSaleOrderItemDO::getProductId));
        Map<Long, ErpProductRespVO> productMap = productService.getProductVOMap(
                convertSet(saleOrderItemList, ErpSaleOrderItemDO::getProductId));
        Map<Long, ErpCustomerDO> customerMap = customerService.getCustomerMap(
                convertSet(pageResult.getList(), ErpSaleOrderDO::getCustomerId));
        Map<Long, ErpProjectDO> projectMap = projectService.getProjectMap(
                extractProjectIds(pageResult.getList()));
        Set<Long> userIds = extractCreatorIds(pageResult.getList());
        userIds.addAll(pageResult.getList().stream()
                .map(ErpSaleOrderDO::getSaleUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(userIds);
        List<ErpSaleOrderRespVO> respList = convertList(pageResult.getList(), saleOrder ->
                buildSaleOrderRespVO(saleOrder, saleOrderItemMap.get(saleOrder.getId()), stockCountMap, null, null,
                        productMap, customerMap, projectMap, userMap));
        return new PageResult<>(respList, pageResult.getTotal());
    }

    private ErpSaleOrderRespVO buildSaleOrderRespVO(ErpSaleOrderDO saleOrder,
                                                    List<ErpSaleOrderItemDO> saleOrderItemList,
                                                    Map<Long, BigDecimal> stockCountMap,
                                                    List<ErpSaleOrderAuditLogDO> auditLogs,
                                                    List<ErpSaleOrderRejectLogDO> rejectLogs,
                                                    Map<Long, ErpProductRespVO> productMap,
                                                    Map<Long, ErpCustomerDO> customerMap,
                                                    Map<Long, ErpProjectDO> projectMap,
                                                    Map<Long, AdminUserRespDTO> userMap) {
        ErpSaleOrderRespVO saleOrderRespVO = BeanUtils.toBean(saleOrder, ErpSaleOrderRespVO.class);
        saleOrderRespVO.setDeliveryDate(convertDeliveryDate(saleOrder.getDeliveryDate()));
        saleOrderRespVO.setItems(BeanUtils.toBean(saleOrderItemList, ErpSaleOrderRespVO.Item.class, item -> {
            BigDecimal stockCount = stockCountMap != null ? stockCountMap.get(item.getProductId()) : null;
            item.setStockCount(stockCount != null ? stockCount : BigDecimal.ZERO);
            item.setTotalProductPrice(item.getTotalPrice());
            item.setTotalPrice(calculateDisplayTotalPrice(item.getTotalProductPrice(), item.getTaxPrice()));
            MapUtils.findAndThen(productMap, item.getProductId(), product -> item.setProductName(product.getName())
                    .setProductBarCode(product.getBarCode()).setProductUnitName(product.getUnitName()));
        }));
        saleOrderRespVO.setTotalCountUnitName(resolveTotalCountUnitName(saleOrderRespVO.getItems()));
        saleOrderRespVO.setProductNames(CollUtil.join(saleOrderRespVO.getItems(), "，", ErpSaleOrderRespVO.Item::getProductName));
        MapUtils.findAndThen(customerMap, saleOrder.getCustomerId(), customer -> saleOrderRespVO.setCustomerName(customer.getName()));
        MapUtils.findAndThen(projectMap, saleOrder.getProjectId(), project -> saleOrderRespVO.setProjectName(project.getName()));
        if (CollUtil.isNotEmpty(auditLogs)) {
            saleOrderRespVO.setAuditLogs(buildAuditLogRespVOs(auditLogs, userMap));
        }
        if (CollUtil.isNotEmpty(rejectLogs)) {
            saleOrderRespVO.setRejectLogs(buildRejectLogRespVOs(rejectLogs, userMap));
        }
        if (userMap != null) {
            MapUtils.findAndThen(userMap, ErpUserIdUtils.parseUserId(saleOrder.getCreator()),
                    user -> saleOrderRespVO.setCreatorName(user.getNickname()));
            MapUtils.findAndThen(userMap, saleOrder.getSaleUserId(),
                    user -> saleOrderRespVO.setSaleUserName(user.getNickname()));
        }
        return saleOrderRespVO;
    }

    private BigDecimal calculateDisplayTotalPrice(BigDecimal totalProductPrice, BigDecimal taxPrice) {
        BigDecimal amount = totalProductPrice != null ? totalProductPrice : BigDecimal.ZERO;
        if (taxPrice != null) {
            amount = amount.add(taxPrice);
        }
        return amount;
    }

    private String resolveTotalCountUnitName(List<ErpSaleOrderRespVO.Item> itemList) {
        if (CollUtil.isEmpty(itemList)) {
            return null;
        }
        List<String> unitNames = itemList.stream()
                .map(ErpSaleOrderRespVO.Item::getProductUnitName)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        return unitNames.size() == 1 ? unitNames.get(0) : null;
    }

    private Set<Long> extractCreatorIds(List<ErpSaleOrderDO> saleOrders) {
        return saleOrders.stream()
                .map(ErpSaleOrderDO::getCreator)
                .map(ErpUserIdUtils::parseUserId)
                .collect(Collectors.toSet());
    }

    private Set<Long> extractCreatorIds(ErpSaleOrderDO saleOrder, List<ErpSaleOrderAuditLogDO> auditLogs,
                                        List<ErpSaleOrderRejectLogDO> rejectLogs) {
        Set<Long> userIds = extractCreatorIds(List.of(saleOrder));
        if (CollUtil.isNotEmpty(auditLogs)) {
            userIds.addAll(auditLogs.stream()
                    .map(ErpSaleOrderAuditLogDO::getCreator)
                    .filter(StrUtil::isNotBlank)
                    .map(Long::parseLong)
                    .collect(Collectors.toSet()));
        }
        if (CollUtil.isEmpty(rejectLogs)) {
            return userIds;
        }
        userIds.addAll(rejectLogs.stream()
                .map(ErpSaleOrderRejectLogDO::getCreator)
                .filter(StrUtil::isNotBlank)
                .map(Long::parseLong)
                .collect(Collectors.toSet()));
        return userIds;
    }

    private List<ErpSaleOrderAuditLogRespVO> buildAuditLogRespVOs(List<ErpSaleOrderAuditLogDO> auditLogs,
                                                                  Map<Long, AdminUserRespDTO> userMap) {
        return convertList(auditLogs, auditLog -> {
            ErpSaleOrderAuditLogRespVO respVO = BeanUtils.toBean(auditLog, ErpSaleOrderAuditLogRespVO.class);
            Long userId = ErpUserIdUtils.parseUserId(auditLog.getCreator());
            if (userMap != null && userId != null) {
                respVO.setOperatorId(userId);
                MapUtils.findAndThen(userMap, userId, user -> {
                    respVO.setOperatorName(user.getNickname());
                    respVO.setOperatorNickname(user.getNickname());
                });
            }
            return respVO;
        });
    }

    private List<ErpSaleOrderRejectLogRespVO> buildRejectLogRespVOs(List<ErpSaleOrderRejectLogDO> rejectLogs,
                                                                    Map<Long, AdminUserRespDTO> userMap) {
        return convertList(rejectLogs, rejectLog -> {
            ErpSaleOrderRejectLogRespVO respVO = BeanUtils.toBean(rejectLog, ErpSaleOrderRejectLogRespVO.class);
            Long userId = ErpUserIdUtils.parseUserId(rejectLog.getCreator());
            if (userMap != null && userId != null) {
                respVO.setRejectUserId(userId);
                MapUtils.findAndThen(userMap, userId, user -> {
                    respVO.setRejectUserName(user.getNickname());
                    respVO.setRejectUserNickname(user.getNickname());
                    respVO.setCreatorName(user.getNickname());
                });
            }
            return respVO;
        });
    }

    private Set<Long> extractProjectIds(List<ErpSaleOrderDO> saleOrders) {
        return saleOrders.stream()
                .map(ErpSaleOrderDO::getProjectId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private LocalDateTime convertDeliveryDate(LocalDate deliveryDate) {
        return deliveryDate == null ? null : deliveryDate.atStartOfDay();
    }

}
