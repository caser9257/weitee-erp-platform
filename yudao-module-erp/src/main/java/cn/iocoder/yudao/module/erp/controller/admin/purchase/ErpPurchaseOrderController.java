package cn.iocoder.yudao.module.erp.controller.admin.purchase;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.number.MoneyUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderAuditLogRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderBatchUpdateReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderBatchUpdateResultVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderCancelApprovalReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderExportItemRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderRejectLogRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderSaveReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderSubmitReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpPurchaseSuggestDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpApInvoiceMatchItemDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpApStatementDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinancePaymentAllocateDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinancePrepaymentAllocateDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseOrderAuditLogDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInItemDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseOrderDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseOrderItemDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseOrderRejectLogDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpSupplierDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.project.ErpProjectDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOrderDO;
import cn.iocoder.yudao.module.erp.enums.ErpPurchaseOrderBpmConstants;
import cn.iocoder.yudao.module.erp.enums.common.ErpBizTypeEnum;
import cn.iocoder.yudao.module.erp.service.finance.ErpApInvoiceService;
import cn.iocoder.yudao.module.erp.service.finance.ErpApStatementService;
import cn.iocoder.yudao.module.erp.service.finance.ErpFinancePaymentService;
import cn.iocoder.yudao.module.erp.service.finance.ErpFinancePrepaymentService;
import cn.iocoder.yudao.module.erp.service.mrp.ErpMrpSuggestService;
import cn.iocoder.yudao.module.erp.service.product.ErpProductService;
import cn.iocoder.yudao.module.erp.service.project.ErpProjectService;
import cn.iocoder.yudao.module.erp.service.purchase.ErpPurchaseInService;
import cn.iocoder.yudao.module.erp.service.purchase.ErpPurchaseOrderBpmService;
import cn.iocoder.yudao.module.erp.service.purchase.ErpPurchaseOrderService;
import cn.iocoder.yudao.module.erp.service.purchase.ErpSupplierService;
import cn.iocoder.yudao.module.erp.service.sale.ErpSaleOrderService;
import cn.iocoder.yudao.module.erp.service.stock.ErpStockService;
import cn.iocoder.yudao.module.bpm.service.task.BpmTaskService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.flowable.task.api.history.HistoricTaskInstance;
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

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMultiMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnModelConstants.START_USER_NODE_ID;
import static cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnVariableConstants.TASK_VARIABLE_REASON;
import static cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnVariableConstants.TASK_VARIABLE_STATUS;

@Tag(name = "管理后台 - ERP 采购订单")
@RestController
@RequestMapping("/erp/purchase-order")
@Validated
public class ErpPurchaseOrderController {

    private static final String PRODUCT_NAME_DELIMITER = "，";

    @Resource
    private ErpPurchaseOrderService purchaseOrderService;
    @Resource
    private ErpPurchaseOrderBpmService purchaseOrderBpmService;
    @Resource
    private ErpPurchaseInService purchaseInService;
    @Resource
    private ErpApStatementService apStatementService;
    @Resource
    private ErpFinancePaymentService financePaymentService;
    @Resource
    private ErpFinancePrepaymentService financePrepaymentService;
    @Resource
    private ErpApInvoiceService apInvoiceService;
    @Resource
    private ErpStockService stockService;
    @Resource
    private ErpProductService productService;
    @Resource
    private ErpProjectService projectService;
    @Resource
    private ErpSupplierService supplierService;
    @Resource
    private ErpSaleOrderService saleOrderService;
    @Resource
    private ErpMrpSuggestService mrpSuggestService;
    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private BpmTaskService bpmTaskService;

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
        ErpPurchaseOrderDO purchaseOrder = purchaseOrderService.getPurchaseOrder(id);
        if (purchaseOrder == null) {
            return success(null);
        }
        List<ErpPurchaseOrderItemDO> purchaseOrderItemList = purchaseOrderService.getPurchaseOrderItemListByOrderId(id);
        List<ErpPurchaseOrderAuditLogDO> auditLogs = purchaseOrderService.getPurchaseOrderAuditLogListByOrderId(id);
        List<ErpPurchaseOrderRejectLogDO> rejectLogs = purchaseOrderService.getPurchaseOrderRejectLogListByOrderId(id);
        List<ErpPurchaseOrderAuditLogRespVO> bpmAuditLogs = buildBpmAuditLogRespVOs(purchaseOrder.getProcessInstanceId());
        Map<Long, ErpProductRespVO> productMap = productService.getProductVOMap(
                convertSet(purchaseOrderItemList, ErpPurchaseOrderItemDO::getProductId));
        Map<Long, ErpSupplierDO> supplierMap = purchaseOrder.getSupplierId() == null
                ? Map.of()
                : supplierService.getSupplierMap(Set.of(purchaseOrder.getSupplierId()));
        Map<Long, AdminUserRespDTO> userMap = getAuditUserMap(auditLogs, rejectLogs, bpmAuditLogs);
        PurchaseOrderDisplayContext displayContext = buildPurchaseOrderDisplayContext(List.of(purchaseOrder));
        return success(buildPurchaseOrderRespVO(
                purchaseOrder, purchaseOrderItemList, auditLogs, bpmAuditLogs, rejectLogs,
                productMap, supplierMap, userMap, displayContext));
    }

    @GetMapping("/page")
    @Operation(summary = "获得采购订单分页")
    @PreAuthorize("@ss.hasPermission('erp:purchase-order:query')")
    public CommonResult<PageResult<ErpPurchaseOrderRespVO>> getPurchaseOrderPage(@Valid ErpPurchaseOrderPageReqVO pageReqVO) {
        PageResult<ErpPurchaseOrderDO> pageResult = purchaseOrderService.getPurchaseOrderPage(pageReqVO);
        return success(buildPurchaseOrderVOPageResult(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出采购订单 Excel")
    @PreAuthorize("@ss.hasPermission('erp:purchase-order:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportPurchaseOrderExcel(@Valid ErpPurchaseOrderPageReqVO pageReqVO,
                                         HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ErpPurchaseOrderRespVO> list = buildPurchaseOrderVOPageResult(
                purchaseOrderService.getPurchaseOrderPage(pageReqVO)).getList();
        ExcelUtils.write(response, "采购订单.xls", "数据", ErpPurchaseOrderExportItemRespVO.class,
                buildPurchaseOrderExportItemList(list));
    }

    private List<ErpPurchaseOrderExportItemRespVO> buildPurchaseOrderExportItemList(List<ErpPurchaseOrderRespVO> list) {
        if (CollUtil.isEmpty(list)) {
            return List.of();
        }
        List<ErpPurchaseOrderExportItemRespVO> exportList = new ArrayList<>();
        for (ErpPurchaseOrderRespVO order : list) {
            if (CollUtil.isEmpty(order.getItems())) {
                exportList.add(buildPurchaseOrderExportItem(order, null));
                continue;
            }
            for (ErpPurchaseOrderRespVO.Item item : order.getItems()) {
                exportList.add(buildPurchaseOrderExportItem(order, item));
            }
        }
        return exportList;
    }

    private ErpPurchaseOrderExportItemRespVO buildPurchaseOrderExportItem(ErpPurchaseOrderRespVO order,
                                                                          ErpPurchaseOrderRespVO.Item item) {
        ErpPurchaseOrderExportItemRespVO exportItem = new ErpPurchaseOrderExportItemRespVO();
        exportItem.setNo(order.getNo());
        exportItem.setSupplierName(order.getSupplierName());
        exportItem.setSourceOrderNos(order.getSourceOrderNos());
        exportItem.setSourceType(order.getSourceType());
        exportItem.setCreatorName(order.getCreatorName());
        exportItem.setBusinessOwnerName(order.getBusinessOwnerName());
        exportItem.setOrderTime(order.getOrderTime());
        exportItem.setCreateTime(order.getCreateTime());
        exportItem.setRemark(item != null ? item.getRemark() : order.getRemark());
        if (item != null) {
            exportItem.setProductName(item.getProductName());
            exportItem.setProductBarCode(item.getProductBarCode());
            exportItem.setProductStandard(item.getProductStandard());
            exportItem.setProductUnitName(item.getProductUnitName());
            exportItem.setProjectName(item.getProjectName());
            exportItem.setCount(item.getCount());
            exportItem.setProductPrice(item.getProductPrice());
            exportItem.setTaxIncludedPrice(item.getTaxIncludedPrice());
            exportItem.setEngineeringFee(item.getEngineeringFee());
            exportItem.setTaxPercent(item.getTaxPercent());
            exportItem.setTaxPrice(item.getTaxPrice());
            exportItem.setTotalPrice(item.getTotalPrice());
            exportItem.setInCount(item.getInCount());
            exportItem.setDeliveryDate(item.getDeliveryDate());
            exportItem.setPaymentAllocatedAmount(item.getPaymentAllocatedAmount());
            exportItem.setRelatedCount(item.getRelatedCount());
            exportItem.setInvoicedCount(item.getInvoicedCount());
            exportItem.setAuditorName(item.getAuditorName());
            exportItem.setReturnCount(item.getReturnCount());
        }
        return exportItem;
    }

    private PageResult<ErpPurchaseOrderRespVO> buildPurchaseOrderVOPageResult(PageResult<ErpPurchaseOrderDO> pageResult) {
        if (CollUtil.isEmpty(pageResult.getList())) {
            return PageResult.empty(pageResult.getTotal());
        }
        List<ErpPurchaseOrderItemDO> purchaseOrderItemList = purchaseOrderService.getPurchaseOrderItemListByOrderIds(
                convertSet(pageResult.getList(), ErpPurchaseOrderDO::getId));
        Map<Long, List<ErpPurchaseOrderItemDO>> purchaseOrderItemMap = convertMultiMap(
                purchaseOrderItemList, ErpPurchaseOrderItemDO::getOrderId);
        Map<Long, ErpProductRespVO> productMap = productService.getProductVOMap(
                convertSet(purchaseOrderItemList, ErpPurchaseOrderItemDO::getProductId));
        PurchaseOrderItemDisplayContext itemDisplayContext = buildPurchaseOrderItemDisplayContext(purchaseOrderItemList);
        Map<Long, ErpSupplierDO> supplierMap = supplierService.getSupplierMap(
                convertSet(pageResult.getList(), ErpPurchaseOrderDO::getSupplierId));
        PurchaseOrderDisplayContext displayContext = buildPurchaseOrderDisplayContext(pageResult.getList());
        return BeanUtils.toBean(pageResult, ErpPurchaseOrderRespVO.class, purchaseOrderVO -> {
            List<ErpPurchaseOrderItemDO> orderItems = purchaseOrderItemMap.getOrDefault(purchaseOrderVO.getId(), List.of());
            purchaseOrderVO.setItems(buildPurchaseOrderItems(orderItems, productMap, itemDisplayContext));
            purchaseOrderVO.setProductNames(CollUtil.join(
                    purchaseOrderVO.getItems(), PRODUCT_NAME_DELIMITER, ErpPurchaseOrderRespVO.Item::getProductName));
            MapUtils.findAndThen(supplierMap, purchaseOrderVO.getSupplierId(),
                    supplier -> purchaseOrderVO.setSupplierName(supplier.getName()));
            purchaseOrderVO.setCreatorName(displayContext.getCreatorName(purchaseOrderVO.getId()));
            purchaseOrderVO.setBusinessOwnerName(displayContext.getBusinessOwnerName(purchaseOrderVO.getId()));
            purchaseOrderVO.setSourceOrderNos(displayContext.getSourceOrderNos(purchaseOrderVO.getId()));
            purchaseOrderVO.setSourceType(displayContext.getSourceType(purchaseOrderVO.getId()));
            purchaseOrderVO.setHasPendingPurchaseIn(displayContext.hasPendingPurchaseIn(purchaseOrderVO.getId()));
            purchaseOrderVO.setPendingPurchaseInId(displayContext.getPendingPurchaseInId(purchaseOrderVO.getId()));
        });
    }

    private ErpPurchaseOrderRespVO buildPurchaseOrderRespVO(ErpPurchaseOrderDO purchaseOrder,
                                                            List<ErpPurchaseOrderItemDO> purchaseOrderItemList,
                                                            List<ErpPurchaseOrderAuditLogDO> auditLogs,
                                                            List<ErpPurchaseOrderAuditLogRespVO> bpmAuditLogs,
                                                            List<ErpPurchaseOrderRejectLogDO> rejectLogs,
                                                            Map<Long, ErpProductRespVO> productMap,
                                                            Map<Long, ErpSupplierDO> supplierMap,
                                                            Map<Long, AdminUserRespDTO> userMap,
                                                            PurchaseOrderDisplayContext displayContext) {
        PurchaseOrderItemDisplayContext itemDisplayContext = buildPurchaseOrderItemDisplayContext(purchaseOrderItemList);
        return BeanUtils.toBean(purchaseOrder, ErpPurchaseOrderRespVO.class, purchaseOrderVO -> {
            purchaseOrderVO.setItems(buildPurchaseOrderItems(purchaseOrderItemList, productMap, itemDisplayContext));
            purchaseOrderVO.setProductNames(CollUtil.join(
                    purchaseOrderVO.getItems(), PRODUCT_NAME_DELIMITER, ErpPurchaseOrderRespVO.Item::getProductName));
            MapUtils.findAndThen(supplierMap, purchaseOrderVO.getSupplierId(),
                    supplier -> purchaseOrderVO.setSupplierName(supplier.getName()));
            purchaseOrderVO.setCreatorName(displayContext.getCreatorName(purchaseOrderVO.getId()));
            purchaseOrderVO.setBusinessOwnerName(displayContext.getBusinessOwnerName(purchaseOrderVO.getId()));
            purchaseOrderVO.setSourceOrderNos(displayContext.getSourceOrderNos(purchaseOrderVO.getId()));
            purchaseOrderVO.setSourceType(displayContext.getSourceType(purchaseOrderVO.getId()));
            purchaseOrderVO.setHasPendingPurchaseIn(displayContext.hasPendingPurchaseIn(purchaseOrderVO.getId()));
            purchaseOrderVO.setPendingPurchaseInId(displayContext.getPendingPurchaseInId(purchaseOrderVO.getId()));
            if (CollUtil.isNotEmpty(auditLogs) || CollUtil.isNotEmpty(bpmAuditLogs)) {
                purchaseOrderVO.setAuditLogs(ErpPurchaseOrderDisplaySupport.mergeAuditLogs(
                        new ArrayList<>(buildAuditLogRespVOs(auditLogs, userMap)),
                        buildBpmAuditLogRespVOs(bpmAuditLogs, userMap)));
            }
            if (CollUtil.isNotEmpty(rejectLogs)) {
                purchaseOrderVO.setRejectLogs(buildRejectLogRespVOs(rejectLogs, userMap));
            }
        });
    }

    private List<ErpPurchaseOrderRespVO.Item> buildPurchaseOrderItems(
            List<ErpPurchaseOrderItemDO> orderItems,
            Map<Long, ErpProductRespVO> productMap,
            PurchaseOrderItemDisplayContext itemDisplayContext) {
        if (CollUtil.isEmpty(orderItems)) {
            return List.of();
        }
        Map<Long, BigDecimal> stockCountMap = stockService.getStockCountMap(
                convertSet(orderItems, ErpPurchaseOrderItemDO::getProductId));
        List<ErpPurchaseOrderRespVO.Item> items = new ArrayList<>(orderItems.size());
        for (ErpPurchaseOrderItemDO orderItem : orderItems) {
            ErpPurchaseOrderRespVO.Item item = BeanUtils.toBean(orderItem, ErpPurchaseOrderRespVO.Item.class);
            ErpProductRespVO product = productMap.get(orderItem.getProductId());
            if (product != null) {
                item.setProductName(product.getName());
                item.setProductBarCode(product.getBarCode());
                item.setProductUnitName(product.getUnitName());
                item.setProductStandard(product.getStandard());
            }
            item.setStockCount(stockCountMap.getOrDefault(orderItem.getProductId(), BigDecimal.ZERO));
            item.setProjectName(itemDisplayContext.getProjectName(orderItem.getProjectId()));
            item.setDeliveryDate(itemDisplayContext.getDeliveryDate(orderItem.getProjectId()));
            item.setTaxIncludedPrice(calculateTaxIncludedPrice(orderItem.getProductPrice(), orderItem.getTaxPercent()));
            item.setTotalPrice(calculateDisplayTotalPrice(item.getTotalProductPrice(), orderItem.getTaxPrice()));
            item.setPaymentAllocatedAmount(itemDisplayContext.getPaymentAllocatedAmount(orderItem.getId()));
            item.setRelatedCount(itemDisplayContext.getRelatedCount(orderItem.getId()));
            item.setInvoicedCount(itemDisplayContext.getInvoicedCount(orderItem.getId()));
            item.setAuditorName(itemDisplayContext.getAuditorName(orderItem.getOrderId()));
            items.add(item);
        }
        return items;
    }

    private PurchaseOrderItemDisplayContext buildPurchaseOrderItemDisplayContext(List<ErpPurchaseOrderItemDO> purchaseOrderItems) {
        if (CollUtil.isEmpty(purchaseOrderItems)) {
            return PurchaseOrderItemDisplayContext.empty();
        }
        Set<Long> projectIds = convertSet(purchaseOrderItems, ErpPurchaseOrderItemDO::getProjectId);
        Map<Long, ErpProjectDO> projectMap = projectService.getProjectMap(projectIds);

        Set<Long> orderIds = convertSet(purchaseOrderItems, ErpPurchaseOrderItemDO::getOrderId);
        List<ErpPurchaseInDO> purchaseIns = purchaseInService.getPurchaseInListByOrderIds(orderIds);
        Set<Long> purchaseInIds = convertSet(purchaseIns, ErpPurchaseInDO::getId);
        List<ErpPurchaseInItemDO> purchaseInItems = purchaseInService.getPurchaseInItemListByInIds(purchaseInIds);
        Map<Long, List<ErpPurchaseInItemDO>> purchaseInItemMapByOrderItemId = convertMultiMap(
                purchaseInItems, ErpPurchaseInItemDO::getOrderItemId);

        Map<Long, ErpApStatementDO> statementMapByPurchaseInId = new HashMap<>();
        List<ErpApStatementDO> statements = apStatementService.getApStatementListByBizTypeAndBizIds(
                ErpBizTypeEnum.PURCHASE_IN.getType(), purchaseInIds);
        for (ErpApStatementDO statement : statements) {
            statementMapByPurchaseInId.put(statement.getBizId(), statement);
        }

        Set<Long> statementIds = convertSet(statements, ErpApStatementDO::getId);
        List<ErpFinancePaymentAllocateDO> paymentAllocates =
                financePaymentService.getFinancePaymentAllocateListByStatementIds(statementIds);
        List<ErpFinancePrepaymentAllocateDO> prepaymentAllocates =
                financePrepaymentService.getApprovedFinancePrepaymentAllocateListByStatementIds(statementIds);
        Map<Long, BigDecimal> statementPaymentAmountMap = new HashMap<>();
        paymentAllocates.forEach(item -> statementPaymentAmountMap.merge(item.getApStatementId(),
                defaultAmount(item.getAllocateAmount()), BigDecimal::add));
        prepaymentAllocates.forEach(item -> statementPaymentAmountMap.merge(item.getApStatementId(),
                defaultAmount(item.getAllocateAmount()), BigDecimal::add));

        List<ErpApInvoiceMatchItemDO> invoiceMatchItems = apInvoiceService.getActiveMatchItemListByPurchaseInItemIds(
                convertSet(purchaseInItems, ErpPurchaseInItemDO::getId));
        Map<Long, BigDecimal> invoicedCountByPurchaseInItemId = new HashMap<>();
        invoiceMatchItems.forEach(item -> invoicedCountByPurchaseInItemId.merge(item.getSourcePurchaseInItemId(),
                defaultAmount(item.getMatchCount()), BigDecimal::add));

        Map<Long, BigDecimal> paymentAllocatedAmountMap = new HashMap<>();
        Map<Long, BigDecimal> invoicedCountMap = new HashMap<>();
        Map<Long, BigDecimal> relatedCountMap = new HashMap<>();
        for (ErpPurchaseOrderItemDO purchaseOrderItem : purchaseOrderItems) {
            List<ErpPurchaseInItemDO> relatedPurchaseInItems =
                    purchaseInItemMapByOrderItemId.getOrDefault(purchaseOrderItem.getId(), List.of());
            BigDecimal allocatedAmount = BigDecimal.ZERO;
            BigDecimal invoicedCount = BigDecimal.ZERO;
            for (ErpPurchaseInItemDO purchaseInItem : relatedPurchaseInItems) {
                BigDecimal statementAllocatedAmount = resolveStatementAllocatedAmount(
                        purchaseInItem, statementMapByPurchaseInId, statementPaymentAmountMap);
                allocatedAmount = allocatedAmount.add(statementAllocatedAmount);
                invoicedCount = invoicedCount.add(invoicedCountByPurchaseInItemId.getOrDefault(
                        purchaseInItem.getId(), BigDecimal.ZERO));
            }
            paymentAllocatedAmountMap.put(purchaseOrderItem.getId(), allocatedAmount);
            invoicedCountMap.put(purchaseOrderItem.getId(), invoicedCount);
            relatedCountMap.put(purchaseOrderItem.getId(), invoicedCount);
        }

        Map<Long, String> projectNameMap = new HashMap<>();
        Map<Long, LocalDateTime> deliveryDateMap = new HashMap<>();
        projectMap.forEach((projectId, project) -> {
            projectNameMap.put(projectId, project.getName());
            deliveryDateMap.put(projectId, convertDeliveryDate(project.getDeliveryDate()));
        });

        Map<Long, String> auditorNameMap = buildAuditorNameMap(orderIds);
        return new PurchaseOrderItemDisplayContext(
                projectNameMap, deliveryDateMap, paymentAllocatedAmountMap, relatedCountMap,
                invoicedCountMap, auditorNameMap);
    }

    private Map<Long, String> buildAuditorNameMap(Set<Long> orderIds) {
        if (CollUtil.isEmpty(orderIds)) {
            return Map.of();
        }
        List<ErpPurchaseOrderDO> purchaseOrders = purchaseOrderService.getPurchaseOrderList(orderIds);
        Map<Long, Long> auditorIdByOrderId = new HashMap<>();
        Set<Long> userIds = new LinkedHashSet<>();
        for (ErpPurchaseOrderDO purchaseOrder : purchaseOrders) {
            List<ErpPurchaseOrderAuditLogDO> auditLogs =
                    purchaseOrderService.getPurchaseOrderAuditLogListByOrderId(purchaseOrder.getId());
            for (ErpPurchaseOrderAuditLogDO auditLog : auditLogs) {
                if (!"APPROVE".equals(auditLog.getActionType())) {
                    continue;
                }
                Long userId = parseUserId(auditLog.getCreator());
                if (userId != null) {
                    auditorIdByOrderId.put(purchaseOrder.getId(), userId);
                    userIds.add(userId);
                }
                break;
            }
        }
        Map<Long, AdminUserRespDTO> userMap = userIds.isEmpty() ? Map.of() : adminUserApi.getUserMap(userIds);
        Map<Long, String> auditorNameMap = new HashMap<>();
        auditorIdByOrderId.forEach((orderId, userId) -> auditorNameMap.put(orderId, getUserNickname(userMap, userId)));
        return auditorNameMap;
    }

    private BigDecimal resolveStatementAllocatedAmount(ErpPurchaseInItemDO purchaseInItem,
                                                       Map<Long, ErpApStatementDO> statementMapByPurchaseInId,
                                                       Map<Long, BigDecimal> statementPaymentAmountMap) {
        ErpApStatementDO statement = statementMapByPurchaseInId.get(purchaseInItem.getInId());
        if (statement == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal statementAllocatedAmount = statementPaymentAmountMap.getOrDefault(statement.getId(), BigDecimal.ZERO);
        BigDecimal statementAmount = defaultAmount(statement.getAmount()).abs();
        if (statementAllocatedAmount.compareTo(BigDecimal.ZERO) == 0 || statementAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal itemAmount = calculateDisplayTotalPrice(purchaseInItem.getTotalPrice(), purchaseInItem.getTaxPrice()).abs();
        if (itemAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return statementAllocatedAmount.abs()
                .multiply(itemAmount)
                .divide(statementAmount, 2, java.math.RoundingMode.HALF_UP);
    }

    private BigDecimal calculateTaxIncludedPrice(BigDecimal productPrice, BigDecimal taxPercent) {
        if (productPrice == null) {
            return null;
        }
        if (taxPercent == null) {
            return productPrice;
        }
        return MoneyUtils.priceMultiplyPercent(productPrice, BigDecimal.valueOf(100).add(taxPercent));
    }

    private BigDecimal calculateDisplayTotalPrice(BigDecimal totalProductPrice, BigDecimal taxPrice) {
        BigDecimal amount = defaultAmount(totalProductPrice);
        if (taxPrice != null) {
            amount = amount.add(taxPrice);
        }
        return amount;
    }

    private BigDecimal defaultAmount(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    private LocalDateTime convertDeliveryDate(LocalDate deliveryDate) {
        return deliveryDate == null ? null : deliveryDate.atStartOfDay();
    }

    private Map<Long, AdminUserRespDTO> getAuditUserMap(List<ErpPurchaseOrderAuditLogDO> auditLogs,
                                                        List<ErpPurchaseOrderRejectLogDO> rejectLogs,
                                                        List<ErpPurchaseOrderAuditLogRespVO> bpmAuditLogs) {
        Set<Long> userIds = new LinkedHashSet<>();
        if (CollUtil.isNotEmpty(auditLogs)) {
            auditLogs.forEach(log -> addUserId(userIds, parseUserId(log.getCreator())));
        }
        if (CollUtil.isNotEmpty(rejectLogs)) {
            rejectLogs.forEach(log -> addUserId(userIds, parseUserId(log.getCreator())));
        }
        if (CollUtil.isNotEmpty(bpmAuditLogs)) {
            bpmAuditLogs.forEach(log -> addUserId(userIds, log.getOperatorId()));
        }
        return CollUtil.isEmpty(userIds) ? Map.of() : adminUserApi.getUserMap(userIds);
    }

    private List<ErpPurchaseOrderAuditLogRespVO> buildAuditLogRespVOs(List<ErpPurchaseOrderAuditLogDO> auditLogs,
                                                                      Map<Long, AdminUserRespDTO> userMap) {
        return convertList(auditLogs, auditLog -> {
            ErpPurchaseOrderAuditLogRespVO respVO = BeanUtils.toBean(auditLog, ErpPurchaseOrderAuditLogRespVO.class);
            Long userId = parseUserId(auditLog.getCreator());
            respVO.setOperatorId(userId);
            MapUtils.findAndThen(userMap, userId, user -> {
                respVO.setOperatorName(user.getNickname());
                respVO.setOperatorNickname(user.getNickname());
            });
            return respVO;
        });
    }

    private List<ErpPurchaseOrderAuditLogRespVO> buildBpmAuditLogRespVOs(String processInstanceId) {
        if (StrUtil.isBlank(processInstanceId)) {
            return List.of();
        }
        return convertList(bpmTaskService.getTaskListByProcessInstanceId(processInstanceId, true), task -> {
            if (START_USER_NODE_ID.equals(task.getTaskDefinitionKey()) || task.getEndTime() == null) {
                return null;
            }
            Integer taskStatus = (Integer) task.getTaskLocalVariables().get(TASK_VARIABLE_STATUS);
            String actionType = ErpPurchaseOrderDisplaySupport.resolveActionTypeByTaskStatus(taskStatus);
            if (actionType == null) {
                return null;
            }
            ErpPurchaseOrderAuditLogRespVO respVO = new ErpPurchaseOrderAuditLogRespVO();
            respVO.setActionType(actionType);
            respVO.setReason((String) task.getTaskLocalVariables().get(TASK_VARIABLE_REASON));
            respVO.setOperatorId(parseUserId(task.getAssignee()));
            respVO.setTaskName(task.getName());
            respVO.setCreateTime(task.getEndTime() == null ? null
                    : task.getEndTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            return respVO;
        }).stream().filter(java.util.Objects::nonNull).toList();
    }

    private List<ErpPurchaseOrderAuditLogRespVO> buildBpmAuditLogRespVOs(List<ErpPurchaseOrderAuditLogRespVO> auditLogs,
                                                                         Map<Long, AdminUserRespDTO> userMap) {
        return convertList(auditLogs, auditLog -> {
            Long userId = auditLog.getOperatorId();
            MapUtils.findAndThen(userMap, userId, user -> {
                auditLog.setOperatorName(user.getNickname());
                auditLog.setOperatorNickname(user.getNickname());
            });
            return auditLog;
        });
    }

    private List<ErpPurchaseOrderRejectLogRespVO> buildRejectLogRespVOs(List<ErpPurchaseOrderRejectLogDO> rejectLogs,
                                                                        Map<Long, AdminUserRespDTO> userMap) {
        return convertList(rejectLogs, rejectLog -> {
            ErpPurchaseOrderRejectLogRespVO respVO = BeanUtils.toBean(rejectLog, ErpPurchaseOrderRejectLogRespVO.class);
            Long userId = parseUserId(rejectLog.getCreator());
            respVO.setRejectUserId(userId);
            MapUtils.findAndThen(userMap, userId, user -> {
                respVO.setRejectUserName(user.getNickname());
                respVO.setRejectUserNickname(user.getNickname());
                respVO.setCreatorName(user.getNickname());
            });
            return respVO;
        });
    }

    private PurchaseOrderDisplayContext buildPurchaseOrderDisplayContext(List<ErpPurchaseOrderDO> purchaseOrders) {
        if (CollUtil.isEmpty(purchaseOrders)) {
            return PurchaseOrderDisplayContext.empty();
        }
        Set<Long> purchaseOrderIds = convertSet(purchaseOrders, ErpPurchaseOrderDO::getId);
        Map<Long, List<ErpPurchaseInDO>> purchaseInMap = convertMultiMap(
                purchaseInService.getPurchaseInListByOrderIds(purchaseOrderIds), ErpPurchaseInDO::getOrderId);
        List<ErpPurchaseSuggestDO> purchaseSuggests =
                mrpSuggestService.getPurchaseSuggestListByConvertPurchaseOrderIds(purchaseOrderIds);
        Map<Long, List<ErpPurchaseSuggestDO>> purchaseSuggestMap = new HashMap<>();
        Set<Long> sourceOrderIds = new LinkedHashSet<>();
        for (ErpPurchaseSuggestDO purchaseSuggest : purchaseSuggests) {
            if (purchaseSuggest.getConvertPurchaseOrderId() != null) {
                purchaseSuggestMap.computeIfAbsent(purchaseSuggest.getConvertPurchaseOrderId(), key -> new ArrayList<>())
                        .add(purchaseSuggest);
            }
            if (purchaseSuggest.getSourceOrderId() != null) {
                sourceOrderIds.add(purchaseSuggest.getSourceOrderId());
            }
        }

        List<ErpSaleOrderDO> saleOrders = sourceOrderIds.isEmpty()
                ? List.of()
                : saleOrderService.getSaleOrderDisplayListByIds(sourceOrderIds);
        Map<Long, ErpSaleOrderDO> saleOrderMap = new HashMap<>();
        for (ErpSaleOrderDO saleOrder : saleOrders) {
            saleOrderMap.put(saleOrder.getId(), saleOrder);
        }

        Set<Long> userIds = new LinkedHashSet<>();
        for (ErpPurchaseOrderDO purchaseOrder : purchaseOrders) {
            addUserId(userIds, parseUserId(purchaseOrder.getCreator()));
        }
        for (ErpSaleOrderDO saleOrder : saleOrders) {
            addUserId(userIds, saleOrder.getSaleUserId());
            addUserId(userIds, parseUserId(saleOrder.getCreator()));
        }
        Map<Long, AdminUserRespDTO> userMap = userIds.isEmpty() ? Map.of() : adminUserApi.getUserMap(userIds);

        Map<Long, String> creatorNameMap = new HashMap<>();
        Map<Long, String> businessOwnerNameMap = new HashMap<>();
        Map<Long, String> sourceOrderNosMap = new HashMap<>();
        Map<Long, String> sourceTypeMap = new HashMap<>();
        Map<Long, Boolean> pendingPurchaseInMap = new HashMap<>();
        Map<Long, Long> pendingPurchaseInIdMap = new HashMap<>();
        for (ErpPurchaseOrderDO purchaseOrder : purchaseOrders) {
            Long purchaseOrderId = purchaseOrder.getId();
            String creatorName = getUserNickname(userMap, parseUserId(purchaseOrder.getCreator()));
            creatorNameMap.put(purchaseOrderId, creatorName);

            List<String> saleUserNames = new ArrayList<>();
            List<String> sourceCreatorNames = new ArrayList<>();
            List<String> sourceOrderNos = new ArrayList<>();
            List<ErpPurchaseSuggestDO> relatedSuggests = purchaseSuggestMap.getOrDefault(purchaseOrderId, List.of());
            for (ErpPurchaseSuggestDO purchaseSuggest : relatedSuggests) {
                ErpSaleOrderDO saleOrder = saleOrderMap.get(purchaseSuggest.getSourceOrderId());
                if (saleOrder == null) {
                    continue;
                }
                saleUserNames.add(getUserNickname(userMap, saleOrder.getSaleUserId()));
                sourceCreatorNames.add(getUserNickname(userMap, parseUserId(saleOrder.getCreator())));
                sourceOrderNos.add(saleOrder.getNo());
            }
            businessOwnerNameMap.put(purchaseOrderId,
                    ErpPurchaseOrderDisplaySupport.resolveBusinessOwnerName(
                            saleUserNames, sourceCreatorNames, creatorName));
            sourceOrderNosMap.put(purchaseOrderId,
                    ErpPurchaseOrderDisplaySupport.joinDistinctDisplay(sourceOrderNos));
            sourceTypeMap.put(purchaseOrderId, CollUtil.isEmpty(relatedSuggests)
                    ? ErpPurchaseOrderBpmConstants.SOURCE_TYPE_MANUAL
                    : ErpPurchaseOrderBpmConstants.SOURCE_TYPE_MRP);
            List<ErpPurchaseInDO> relatedPurchaseIns = purchaseInMap.getOrDefault(purchaseOrderId, List.of());
            pendingPurchaseInMap.put(purchaseOrderId,
                    ErpPurchaseOrderDisplaySupport.shouldDisplayPendingPurchaseIn(
                            purchaseOrder.getTotalCount(), purchaseOrder.getInCount(), relatedPurchaseIns));
            pendingPurchaseInIdMap.put(purchaseOrderId,
                    ErpPurchaseOrderDisplaySupport.resolveDisplayPendingPurchaseInId(
                            purchaseOrder.getTotalCount(), purchaseOrder.getInCount(), relatedPurchaseIns));
        }
        return new PurchaseOrderDisplayContext(
                creatorNameMap, businessOwnerNameMap, sourceOrderNosMap, sourceTypeMap,
                pendingPurchaseInMap, pendingPurchaseInIdMap);
    }

    private void addUserId(Set<Long> userIds, Long userId) {
        if (userId != null) {
            userIds.add(userId);
        }
    }

    private Long parseUserId(String value) {
        return StrUtil.isNumeric(value) ? Long.valueOf(value) : null;
    }

    private String getUserNickname(Map<Long, AdminUserRespDTO> userMap, Long userId) {
        AdminUserRespDTO user = userId == null ? null : userMap.get(userId);
        return user == null ? null : user.getNickname();
    }

    private static final class PurchaseOrderDisplayContext {

        private final Map<Long, String> creatorNameMap;
        private final Map<Long, String> businessOwnerNameMap;
        private final Map<Long, String> sourceOrderNosMap;
        private final Map<Long, String> sourceTypeMap;
        private final Map<Long, Boolean> pendingPurchaseInMap;
        private final Map<Long, Long> pendingPurchaseInIdMap;

        private PurchaseOrderDisplayContext(Map<Long, String> creatorNameMap,
                                            Map<Long, String> businessOwnerNameMap,
                                            Map<Long, String> sourceOrderNosMap,
                                            Map<Long, String> sourceTypeMap,
                                            Map<Long, Boolean> pendingPurchaseInMap,
                                            Map<Long, Long> pendingPurchaseInIdMap) {
            this.creatorNameMap = creatorNameMap;
            this.businessOwnerNameMap = businessOwnerNameMap;
            this.sourceOrderNosMap = sourceOrderNosMap;
            this.sourceTypeMap = sourceTypeMap;
            this.pendingPurchaseInMap = pendingPurchaseInMap;
            this.pendingPurchaseInIdMap = pendingPurchaseInIdMap;
        }

        private static PurchaseOrderDisplayContext empty() {
            return new PurchaseOrderDisplayContext(Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of());
        }

        private String getCreatorName(Long purchaseOrderId) {
            return creatorNameMap.get(purchaseOrderId);
        }

        private String getBusinessOwnerName(Long purchaseOrderId) {
            return businessOwnerNameMap.get(purchaseOrderId);
        }

        private String getSourceOrderNos(Long purchaseOrderId) {
            return sourceOrderNosMap.get(purchaseOrderId);
        }

        private String getSourceType(Long purchaseOrderId) {
            return sourceTypeMap.get(purchaseOrderId);
        }

        private Boolean hasPendingPurchaseIn(Long purchaseOrderId) {
            return pendingPurchaseInMap.getOrDefault(purchaseOrderId, Boolean.FALSE);
        }

        private Long getPendingPurchaseInId(Long purchaseOrderId) {
            return pendingPurchaseInIdMap.get(purchaseOrderId);
        }
    }

    private static final class PurchaseOrderItemDisplayContext {

        private final Map<Long, String> projectNameMap;
        private final Map<Long, LocalDateTime> deliveryDateMap;
        private final Map<Long, BigDecimal> paymentAllocatedAmountMap;
        private final Map<Long, BigDecimal> relatedCountMap;
        private final Map<Long, BigDecimal> invoicedCountMap;
        private final Map<Long, String> auditorNameMap;

        private PurchaseOrderItemDisplayContext(Map<Long, String> projectNameMap,
                                                Map<Long, LocalDateTime> deliveryDateMap,
                                                Map<Long, BigDecimal> paymentAllocatedAmountMap,
                                                Map<Long, BigDecimal> relatedCountMap,
                                                Map<Long, BigDecimal> invoicedCountMap,
                                                Map<Long, String> auditorNameMap) {
            this.projectNameMap = projectNameMap;
            this.deliveryDateMap = deliveryDateMap;
            this.paymentAllocatedAmountMap = paymentAllocatedAmountMap;
            this.relatedCountMap = relatedCountMap;
            this.invoicedCountMap = invoicedCountMap;
            this.auditorNameMap = auditorNameMap;
        }

        private static PurchaseOrderItemDisplayContext empty() {
            return new PurchaseOrderItemDisplayContext(
                    Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of());
        }

        private String getProjectName(Long projectId) {
            return projectId == null ? null : projectNameMap.get(projectId);
        }

        private LocalDateTime getDeliveryDate(Long projectId) {
            return projectId == null ? null : deliveryDateMap.get(projectId);
        }

        private BigDecimal getPaymentAllocatedAmount(Long orderItemId) {
            return paymentAllocatedAmountMap.getOrDefault(orderItemId, BigDecimal.ZERO);
        }

        private BigDecimal getRelatedCount(Long orderItemId) {
            return relatedCountMap.getOrDefault(orderItemId, BigDecimal.ZERO);
        }

        private BigDecimal getInvoicedCount(Long orderItemId) {
            return invoicedCountMap.getOrDefault(orderItemId, BigDecimal.ZERO);
        }

        private String getAuditorName(Long orderId) {
            return auditorNameMap.get(orderId);
        }
    }
}
