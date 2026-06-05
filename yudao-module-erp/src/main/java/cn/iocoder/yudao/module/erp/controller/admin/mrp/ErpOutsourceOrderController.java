package cn.iocoder.yudao.module.erp.controller.admin.mrp;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpApStatementDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinancePaymentAllocateDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinancePaymentDO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.outsource.*;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.outsource.ErpOutsourceCostDetailRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.outsource.ErpOutsourceReconciliationRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.*;
import cn.iocoder.yudao.module.erp.enums.mrp.ErpOutsourceIssueTypeEnum;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpSupplierDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpWarehouseDO;
import cn.iocoder.yudao.module.erp.enums.ErpApStatementStatusEnum;
import cn.iocoder.yudao.module.erp.enums.ErpFinancePaymentAllocateStatusEnum;
import cn.iocoder.yudao.module.erp.enums.common.ErpBizTypeEnum;
import cn.iocoder.yudao.module.erp.enums.mrp.ErpOutsourceOrderStatusEnum;
import cn.iocoder.yudao.module.erp.enums.mrp.ErpOutsourceOrderTypeEnum;
import cn.iocoder.yudao.module.erp.service.finance.ErpApStatementService;
import cn.iocoder.yudao.module.erp.service.finance.ErpFinancePaymentService;
import cn.iocoder.yudao.module.erp.service.mrp.ErpOutsourceOrderService;
import cn.iocoder.yudao.module.erp.service.product.ErpProductService;
import cn.iocoder.yudao.module.erp.service.purchase.ErpSupplierService;
import cn.iocoder.yudao.module.erp.service.stock.ErpWarehouseService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMultiMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.erp.util.ErpUserIdUtils.parseUserId;

@Tag(name = "管理后台 - ERP 委外订单")
@RestController
@RequestMapping("/erp/outsource-order")
@Validated
public class ErpOutsourceOrderController {

    @Resource
    private ErpOutsourceOrderService outsourceOrderService;
    @Resource
    private ErpSupplierService supplierService;
    @Resource
    private ErpProductService productService;
    @Resource
    private ErpWarehouseService warehouseService;
    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private ErpApStatementService apStatementService;
    @Resource
    private ErpFinancePaymentService financePaymentService;

    @PostMapping("/create")
    @Operation(summary = "创建委外订单")
    @PreAuthorize("@ss.hasPermission('erp:production-order:update')")
    public CommonResult<Long> createOutsourceOrder(@Valid @RequestBody ErpOutsourceOrderSaveReqVO createReqVO) {
        return success(outsourceOrderService.createOutsourceOrder(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新委外订单")
    @PreAuthorize("@ss.hasPermission('erp:production-order:update')")
    public CommonResult<Boolean> updateOutsourceOrder(@Valid @RequestBody ErpOutsourceOrderSaveReqVO updateReqVO) {
        outsourceOrderService.updateOutsourceOrder(updateReqVO);
        return success(true);
    }

    @PostMapping("/close")
    @Operation(summary = "委外订单损耗结案")
    @PreAuthorize("@ss.hasPermission('erp:production-order:update')")
    public CommonResult<Boolean> closeOutsourceOrder(@Valid @RequestBody ErpOutsourceOrderCloseReqVO reqVO) {
        outsourceOrderService.closeOutsourceOrder(reqVO);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得委外订单")
    @Parameter(name = "id", required = true)
    @PreAuthorize("@ss.hasPermission('erp:production-order:query')")
    public CommonResult<ErpOutsourceOrderRespVO> getOutsourceOrder(@RequestParam("id") Long id) {
        ErpOutsourceOrderDO order = outsourceOrderService.getOutsourceOrder(id);
        return success(order == null ? null : buildOrderResp(order,
                supplierService.getSupplierMap(List.of(order.getSupplierId())),
                productService.getProductVOMap(List.of(order.getProductId())),
                buildUserMapByCreator(order.getCreator())));
    }

    @GetMapping("/page")
    @Operation(summary = "获得委外订单分页")
    @PreAuthorize("@ss.hasPermission('erp:production-order:query')")
    public CommonResult<PageResult<ErpOutsourceOrderRespVO>> getOutsourceOrderPage(@Valid ErpOutsourceOrderPageReqVO pageReqVO) {
        PageResult<ErpOutsourceOrderDO> pageResult = outsourceOrderService.getOutsourceOrderPage(pageReqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty(pageResult.getTotal()));
        }
        Map<Long, ErpSupplierDO> supplierMap = supplierService.getSupplierMap(convertSet(pageResult.getList(), ErpOutsourceOrderDO::getSupplierId));
        Map<Long, ErpProductRespVO> productMap = productService.getProductVOMap(convertSet(pageResult.getList(), ErpOutsourceOrderDO::getProductId));
        List<Long> creatorIds = new ArrayList<>(convertList(pageResult.getList(), item -> parseUserId(item.getCreator())));
        creatorIds.removeIf(Objects::isNull);
        Map<Long, AdminUserRespDTO> userMap = creatorIds.isEmpty() ? Collections.emptyMap() : adminUserApi.getUserMap(creatorIds);
        return success(new PageResult<>(convertList(pageResult.getList(), item -> buildOrderResp(item, supplierMap, productMap, userMap)),
                pageResult.getTotal()));
    }

    @PostMapping("/issue/create")
    @Operation(summary = "创建委外发料单")
    @PreAuthorize("@ss.hasPermission('erp:production-order:update')")
    public CommonResult<Long> createOutsourceIssue(@Valid @RequestBody ErpOutsourceIssueCreateReqVO reqVO) {
        return success(outsourceOrderService.createOutsourceIssue(reqVO));
    }

    @GetMapping("/issue/get")
    @Operation(summary = "获得委外发料单")
    @PreAuthorize("@ss.hasPermission('erp:production-order:query')")
    public CommonResult<ErpOutsourceIssueRespVO> getOutsourceIssue(@RequestParam("id") Long id) {
        ErpOutsourceIssueDO issue = outsourceOrderService.getOutsourceIssue(id);
        if (issue == null) {
            return success(null);
        }
        List<ErpOutsourceIssueItemDO> items = outsourceOrderService.getOutsourceIssueItemListByIssueId(id);
        return success(buildIssueResp(issue, items));
    }

    @GetMapping("/issue/page")
    @Operation(summary = "获得委外发料单分页")
    @PreAuthorize("@ss.hasPermission('erp:production-order:query')")
    public CommonResult<PageResult<ErpOutsourceIssueRespVO>> getOutsourceIssuePage(@Valid ErpOutsourceIssuePageReqVO pageReqVO) {
        PageResult<ErpOutsourceIssueDO> pageResult = outsourceOrderService.getOutsourceIssuePage(pageReqVO);
        List<Long> creatorIds = new ArrayList<>(convertList(pageResult.getList(), item -> parseUserId(item.getCreator())));
        creatorIds.removeIf(Objects::isNull);
        Map<Long, AdminUserRespDTO> userMap = creatorIds.isEmpty() ? Collections.emptyMap() : adminUserApi.getUserMap(creatorIds);
        return success(new PageResult<>(convertList(pageResult.getList(), issue -> {
            ErpOutsourceIssueRespVO vo = BeanUtils.toBean(issue, ErpOutsourceIssueRespVO.class);
            ErpOutsourceOrderDO order = outsourceOrderService.getOutsourceOrder(issue.getOrderId());
            if (order != null) {
                vo.setOrderNo(order.getNo());
            }
            vo.setIssueType(ErpOutsourceIssueTypeEnum.defaultType(issue.getIssueType()));
            vo.setIssueTypeName(ErpOutsourceIssueTypeEnum.resolveName(vo.getIssueType()));
            vo.setStatusName(resolveDoneStatusName(issue.getStatus()));
            MapUtils.findAndThen(userMap, parseUserId(issue.getCreator()), user -> vo.setCreatorName(user.getNickname()));
            return vo;
        }), pageResult.getTotal()));
    }

    @PostMapping("/return/create")
    @Operation(summary = "创建委外退料单")
    @PreAuthorize("@ss.hasPermission('erp:production-order:update')")
    public CommonResult<Long> createOutsourceReturn(@Valid @RequestBody ErpOutsourceReturnCreateReqVO reqVO) {
        return success(outsourceOrderService.createOutsourceReturn(reqVO));
    }

    @GetMapping("/return/get")
    @Operation(summary = "获得委外退料单")
    @PreAuthorize("@ss.hasPermission('erp:production-order:query')")
    public CommonResult<ErpOutsourceReturnRespVO> getOutsourceReturn(@RequestParam("id") Long id) {
        ErpOutsourceReturnDO returning = outsourceOrderService.getOutsourceReturn(id);
        if (returning == null) {
            return success(null);
        }
        List<ErpOutsourceReturnItemDO> items = outsourceOrderService.getOutsourceReturnItemListByReturnId(id);
        return success(buildReturnResp(returning, items));
    }

    @GetMapping("/return/page")
    @Operation(summary = "获得委外退料单分页")
    @PreAuthorize("@ss.hasPermission('erp:production-order:query')")
    public CommonResult<PageResult<ErpOutsourceReturnRespVO>> getOutsourceReturnPage(@Valid ErpOutsourceReturnPageReqVO pageReqVO) {
        PageResult<ErpOutsourceReturnDO> pageResult = outsourceOrderService.getOutsourceReturnPage(pageReqVO);
        return success(new PageResult<>(convertList(pageResult.getList(), item -> {
            ErpOutsourceReturnRespVO vo = BeanUtils.toBean(item, ErpOutsourceReturnRespVO.class);
            ErpOutsourceOrderDO order = outsourceOrderService.getOutsourceOrder(item.getOrderId());
            if (order != null) {
                vo.setOrderNo(order.getNo());
            }
            vo.setStatusName(resolveDoneStatusName(item.getStatus()));
            return vo;
        }), pageResult.getTotal()));
    }

    @PostMapping("/inbound/create")
    @Operation(summary = "创建委外入库单")
    @PreAuthorize("@ss.hasPermission('erp:production-order:update')")
    public CommonResult<Long> createOutsourceInbound(@Valid @RequestBody ErpOutsourceInboundCreateReqVO reqVO) {
        return success(outsourceOrderService.createOutsourceInbound(reqVO));
    }

    @GetMapping("/inbound/get")
    @Operation(summary = "获得委外入库单")
    @PreAuthorize("@ss.hasPermission('erp:production-order:query')")
    public CommonResult<ErpOutsourceInboundRespVO> getOutsourceInbound(@RequestParam("id") Long id) {
        ErpOutsourceInboundDO inbound = outsourceOrderService.getOutsourceInbound(id);
        if (inbound == null) {
            return success(null);
        }
        return success(buildInboundResp(inbound,
                inbound.getWarehouseId() == null ? Collections.emptyMap() : warehouseService.getWarehouseMap(List.of(inbound.getWarehouseId()))));
    }

    @GetMapping("/inbound/get-print-data")
    @Operation(summary = "获得委外入库打印数据")
    @PreAuthorize("@ss.hasPermission('erp:production-order:query')")
    public CommonResult<ErpOutsourceInboundPrintDataRespVO> getOutsourceInboundPrintData(@RequestParam("id") Long id) {
        ErpOutsourceInboundRespVO inbound = getOutsourceInbound(id).getData();
        if (inbound == null) {
            return success(null);
        }
        ErpOutsourceCostDetailRespVO costDetail = outsourceOrderService.getOutsourceCostDetail(inbound.getOrderId());
        ErpOutsourceReconciliationRespVO reconciliationDetail = outsourceOrderService.getOutsourceReconciliationDetail(inbound.getOrderId());
        ErpOutsourceInboundPrintDataRespVO printData = new ErpOutsourceInboundPrintDataRespVO();
        printData.setOutsourceInbound(inbound);
        printData.setFinancialFacts(buildFinancialFacts(costDetail, reconciliationDetail));
        printData.setSourceAttachments(Collections.emptyList());
        printData.setReconciliationRecords(buildReconciliationRecords(inbound.getOrderId()));
        return success(printData);
    }

    @GetMapping("/inbound/page")
    @Operation(summary = "获得委外入库单分页")
    @PreAuthorize("@ss.hasPermission('erp:production-order:query')")
    public CommonResult<PageResult<ErpOutsourceInboundRespVO>> getOutsourceInboundPage(@Valid ErpOutsourceInboundPageReqVO pageReqVO) {
        PageResult<ErpOutsourceInboundDO> pageResult = outsourceOrderService.getOutsourceInboundPage(pageReqVO);
        Map<Long, ErpWarehouseDO> warehouseMap = warehouseService.getWarehouseMap(convertSet(pageResult.getList(), ErpOutsourceInboundDO::getWarehouseId));
        return success(new PageResult<>(convertList(pageResult.getList(), item -> buildInboundResp(item, warehouseMap)), pageResult.getTotal()));
    }

    @PostMapping("/fee/create")
    @Operation(summary = "创建委外加工费登记")
    @PreAuthorize("@ss.hasPermission('erp:production-order:update')")
    public CommonResult<Long> createOutsourceFee(@Valid @RequestBody ErpOutsourceFeeSaveReqVO reqVO) {
        return success(outsourceOrderService.createOutsourceFee(reqVO));
    }

    @GetMapping("/fee/get")
    @Operation(summary = "获得委外加工费登记")
    @PreAuthorize("@ss.hasPermission('erp:production-order:query')")
    public CommonResult<ErpOutsourceFeeRespVO> getOutsourceFee(@RequestParam("id") Long id) {
        ErpOutsourceFeeDO fee = outsourceOrderService.getOutsourceFee(id);
        if (fee == null) {
            return success(null);
        }
        return success(buildFeeResp(fee));
    }

    @GetMapping("/fee/page")
    @Operation(summary = "获得委外加工费分页")
    @PreAuthorize("@ss.hasPermission('erp:production-order:query')")
    public CommonResult<PageResult<ErpOutsourceFeeRespVO>> getOutsourceFeePage(@Valid ErpOutsourceFeePageReqVO pageReqVO) {
        PageResult<ErpOutsourceFeeDO> pageResult = outsourceOrderService.getOutsourceFeePage(pageReqVO);
        return success(new PageResult<>(convertList(pageResult.getList(), this::buildFeeResp), pageResult.getTotal()));
    }

    @GetMapping("/cost-detail")
    @Operation(summary = "获得委外成本明细")
    @PreAuthorize("@ss.hasPermission('erp:production-order:query')")
    public CommonResult<ErpOutsourceCostDetailRespVO> getOutsourceCostDetail(@RequestParam("orderId") Long orderId) {
        return success(outsourceOrderService.getOutsourceCostDetail(orderId));
    }

    @GetMapping("/reconciliation-detail")
    @Operation(summary = "获得委外核销差异明细")
    @PreAuthorize("@ss.hasPermission('erp:production-order:query')")
    public CommonResult<ErpOutsourceReconciliationRespVO> getOutsourceReconciliationDetail(@RequestParam("orderId") Long orderId) {
        return success(outsourceOrderService.getOutsourceReconciliationDetail(orderId));
    }

    @GetMapping("/loss-detail")
    @Operation(summary = "获得委外损耗明细")
    @PreAuthorize("@ss.hasPermission('erp:production-order:query')")
    public CommonResult<ErpOutsourceLossDetailRespVO> getOutsourceLossDetail(@RequestParam("orderId") Long orderId) {
        return success(outsourceOrderService.getOutsourceLossDetail(orderId));
    }

    @PostMapping("/loss-entry/create")
    @Operation(summary = "创建委外损耗补录")
    @PreAuthorize("@ss.hasPermission('erp:production-order:update')")
    public CommonResult<Boolean> createOutsourceLossEntry(@Valid @RequestBody ErpOutsourceLossEntryCreateReqVO reqVO) {
        outsourceOrderService.createOutsourceLossEntry(reqVO);
        return success(true);
    }

    private ErpOutsourceOrderRespVO buildOrderResp(ErpOutsourceOrderDO order,
                                                   Map<Long, ErpSupplierDO> supplierMap,
                                                   Map<Long, ErpProductRespVO> productMap,
                                                   Map<Long, AdminUserRespDTO> userMap) {
        ErpOutsourceOrderRespVO vo = BeanUtils.toBean(order, ErpOutsourceOrderRespVO.class);
        vo.setOrderTypeName(ErpOutsourceOrderTypeEnum.resolveName(order.getOrderType()));
        vo.setStatusName(ErpOutsourceOrderStatusEnum.resolveName(order.getStatus()));
        MapUtils.findAndThen(supplierMap, order.getSupplierId(), supplier -> vo.setSupplierName(supplier.getName()));
        MapUtils.findAndThen(productMap, order.getProductId(), product -> vo.setProductName(product.getName()));
        if (order.getCreator() != null) {
            MapUtils.findAndThen(userMap, parseUserId(order.getCreator()), user -> vo.setCreatorName(user.getNickname()));
        }
        return vo;
    }

    private ErpOutsourceIssueRespVO buildIssueResp(ErpOutsourceIssueDO issue, List<ErpOutsourceIssueItemDO> items) {
        Map<Long, ErpOutsourceOrderDO> orderMap = new HashMap<>();
        if (issue.getOrderId() != null) {
            ErpOutsourceOrderDO order = outsourceOrderService.getOutsourceOrder(issue.getOrderId());
            if (order != null) {
                orderMap.put(issue.getOrderId(), order);
            }
        }
        Map<Long, ErpProductRespVO> productMap = CollUtil.isEmpty(items) ? Collections.emptyMap()
                : productService.getProductVOMap(convertSet(items, ErpOutsourceIssueItemDO::getMaterialId));
        Map<Long, ErpWarehouseDO> warehouseMap = CollUtil.isEmpty(items) ? Collections.emptyMap()
                : warehouseService.getWarehouseMap(convertSet(items, ErpOutsourceIssueItemDO::getWarehouseId));
        Map<Long, List<ErpOutsourceIssueBatchDO>> batchMap = CollUtil.isEmpty(items) ? Collections.emptyMap()
                : convertMultiMap(outsourceOrderService.getOutsourceIssueBatchListByIssueItemIds(convertSet(items, ErpOutsourceIssueItemDO::getId)),
                ErpOutsourceIssueBatchDO::getIssueItemId);
        Map<Long, AdminUserRespDTO> userMap = buildUserMapByCreator(issue.getCreator());
        ErpOutsourceIssueRespVO vo = BeanUtils.toBean(issue, ErpOutsourceIssueRespVO.class);
        MapUtils.findAndThen(orderMap, issue.getOrderId(), order -> vo.setOrderNo(order.getNo()));
        MapUtils.findAndThen(userMap, parseUserId(issue.getCreator()), user -> vo.setCreatorName(user.getNickname()));
        vo.setIssueType(ErpOutsourceIssueTypeEnum.defaultType(issue.getIssueType()));
        vo.setIssueTypeName(ErpOutsourceIssueTypeEnum.resolveName(vo.getIssueType()));
        vo.setStatusName(resolveDoneStatusName(issue.getStatus()));
        vo.setItems(convertList(items, item -> {
            ErpOutsourceIssueRespVO.Item itemVO = BeanUtils.toBean(item, ErpOutsourceIssueRespVO.Item.class);
            MapUtils.findAndThen(productMap, item.getMaterialId(), product -> {
                itemVO.setMaterialName(product.getName());
                itemVO.setMaterialCode(product.getMaterialCode());
                itemVO.setMaterialBarCode(product.getBarCode());
                itemVO.setProductUnitName(product.getUnitName());
            });
            MapUtils.findAndThen(warehouseMap, item.getWarehouseId(), warehouse -> itemVO.setWarehouseName(warehouse.getName()));
            itemVO.setBatches(convertList(batchMap.getOrDefault(item.getId(), Collections.emptyList()),
                    batch -> BeanUtils.toBean(batch, ErpOutsourceIssueRespVO.Batch.class)));
            return itemVO;
        }));
        return vo;
    }

    private ErpOutsourceReturnRespVO buildReturnResp(ErpOutsourceReturnDO returning, List<ErpOutsourceReturnItemDO> items) {
        Map<Long, ErpOutsourceOrderDO> orderMap = new HashMap<>();
        if (returning.getOrderId() != null) {
            ErpOutsourceOrderDO order = outsourceOrderService.getOutsourceOrder(returning.getOrderId());
            if (order != null) {
                orderMap.put(returning.getOrderId(), order);
            }
        }
        Map<Long, ErpProductRespVO> productMap = CollUtil.isEmpty(items) ? Collections.emptyMap()
                : productService.getProductVOMap(convertSet(items, ErpOutsourceReturnItemDO::getMaterialId));
        Map<Long, ErpWarehouseDO> warehouseMap = CollUtil.isEmpty(items) ? Collections.emptyMap()
                : warehouseService.getWarehouseMap(convertSet(items, ErpOutsourceReturnItemDO::getWarehouseId));
        Map<Long, List<ErpOutsourceReturnBatchDO>> batchMap = CollUtil.isEmpty(items) ? Collections.emptyMap()
                : convertMultiMap(outsourceOrderService.getOutsourceReturnBatchListByReturnItemIds(convertSet(items, ErpOutsourceReturnItemDO::getId)),
                ErpOutsourceReturnBatchDO::getReturnItemId);
        Map<Long, AdminUserRespDTO> userMap = buildUserMapByCreator(returning.getCreator());
        ErpOutsourceReturnRespVO vo = BeanUtils.toBean(returning, ErpOutsourceReturnRespVO.class);
        MapUtils.findAndThen(orderMap, returning.getOrderId(), order -> vo.setOrderNo(order.getNo()));
        MapUtils.findAndThen(userMap, parseUserId(returning.getCreator()), user -> vo.setCreatorName(user.getNickname()));
        vo.setStatusName(resolveDoneStatusName(returning.getStatus()));
        vo.setItems(convertList(items, item -> {
            ErpOutsourceReturnRespVO.Item itemVO = BeanUtils.toBean(item, ErpOutsourceReturnRespVO.Item.class);
            MapUtils.findAndThen(productMap, item.getMaterialId(), product -> {
                itemVO.setMaterialName(product.getName());
                itemVO.setMaterialCode(product.getMaterialCode());
                itemVO.setMaterialBarCode(product.getBarCode());
                itemVO.setProductUnitName(product.getUnitName());
            });
            MapUtils.findAndThen(warehouseMap, item.getWarehouseId(), warehouse -> itemVO.setWarehouseName(warehouse.getName()));
            itemVO.setBatches(convertList(batchMap.getOrDefault(item.getId(), Collections.emptyList()),
                    batch -> BeanUtils.toBean(batch, ErpOutsourceReturnRespVO.Batch.class)));
            return itemVO;
        }));
        return vo;
    }

    private ErpOutsourceInboundRespVO buildInboundResp(ErpOutsourceInboundDO inbound, Map<Long, ErpWarehouseDO> warehouseMap) {
        ErpOutsourceInboundRespVO vo = BeanUtils.toBean(inbound, ErpOutsourceInboundRespVO.class);
        ErpOutsourceOrderDO order = outsourceOrderService.getOutsourceOrder(inbound.getOrderId());
        if (order != null) {
            vo.setOrderNo(order.getNo());
        }
        MapUtils.findAndThen(warehouseMap, inbound.getWarehouseId(), warehouse -> vo.setWarehouseName(warehouse.getName()));
        vo.setStatusName(resolveDoneStatusName(inbound.getStatus()));
        if (inbound.getCreator() != null) {
            Map<Long, AdminUserRespDTO> userMap = buildUserMapByCreator(inbound.getCreator());
            MapUtils.findAndThen(userMap, parseUserId(inbound.getCreator()), user -> vo.setCreatorName(user.getNickname()));
        }

        // 查询关联的台账信息
        fillStatementInfo(vo, inbound.getId());

        return vo;
    }

    private void fillStatementInfo(ErpOutsourceInboundRespVO vo, Long inboundId) {
        try {
            ErpApStatementDO statement = apStatementService.getApStatementByBizTypeAndBizId(
                    ErpBizTypeEnum.OUTSOURCE_INBOUND.getType(), inboundId);
            if (statement != null) {
                vo.setStatementId(statement.getId());
                vo.setStatementNo(statement.getStatementNo());
                vo.setStatementAmount(statement.getAmount());
                vo.setStatementPaidAmount(statement.getPaidAmount());
                vo.setStatementRemainAmount(statement.getRemainAmount());
                vo.setStatementStatus(statement.getStatus());
                vo.setStatementStatusName(resolveStatementStatusName(statement.getStatus()));
            }
        } catch (Exception e) {
            // 查询失败不影响主流程
        }
    }

    private String resolveStatementStatusName(Integer status) {
        if (status == null) {
            return "-";
        }
        return Arrays.stream(ErpApStatementStatusEnum.values())
                .filter(item -> Objects.equals(item.getStatus(), status))
                .map(ErpApStatementStatusEnum::getName)
                .findFirst()
                .orElse("-");
    }

    private ErpOutsourceInboundPrintDataRespVO.FinancialFacts buildFinancialFacts(ErpOutsourceCostDetailRespVO costDetail,
                                                                                ErpOutsourceReconciliationRespVO reconciliationDetail) {
        ErpOutsourceInboundPrintDataRespVO.FinancialFacts financialFacts = new ErpOutsourceInboundPrintDataRespVO.FinancialFacts();
        if (costDetail != null) {
            financialFacts.setPlannedQty(costDetail.getPlannedQty());
            financialFacts.setFinishedQty(costDetail.getFinishedQty());
            financialFacts.setMaterialCost(costDetail.getMaterialCost());
            financialFacts.setReturnMaterialCost(costDetail.getReturnMaterialCost());
            financialFacts.setNetMaterialCost(costDetail.getNetMaterialCost());
            financialFacts.setProcessFee(costDetail.getProcessFee());
            financialFacts.setTotalCost(costDetail.getTotalCost());
            financialFacts.setUnitCost(costDetail.getUnitCost());
            financialFacts.setIssueCount(costDetail.getIssueDetails() == null ? 0 : costDetail.getIssueDetails().size());
            financialFacts.setReturnCount(costDetail.getReturnDetails() == null ? 0 : costDetail.getReturnDetails().size());
            financialFacts.setFeeCount(costDetail.getFeeDetails() == null ? 0 : costDetail.getFeeDetails().size());
            financialFacts.setInboundCount(costDetail.getInboundDetails() == null ? 0 : costDetail.getInboundDetails().size());
        }
        if (reconciliationDetail != null) {
            financialFacts.setLossQty(reconciliationDetail.getLossQty());
            financialFacts.setPendingInboundQty(reconciliationDetail.getPendingInboundQty());
            financialFacts.setUnresolvedQty(reconciliationDetail.getUnresolvedQty());
            financialFacts.setOverInboundQty(reconciliationDetail.getOverInboundQty());
            financialFacts.setHasSupplementIssue(reconciliationDetail.getHasSupplementIssue());
        }
        return financialFacts;
    }

    private ErpOutsourceFeeRespVO buildFeeResp(ErpOutsourceFeeDO fee) {
        ErpOutsourceFeeRespVO vo = BeanUtils.toBean(fee, ErpOutsourceFeeRespVO.class);
        ErpOutsourceOrderDO order = outsourceOrderService.getOutsourceOrder(fee.getOrderId());
        if (order != null) {
            vo.setOrderNo(order.getNo());
        }
        vo.setStatusName(resolveDoneStatusName(fee.getStatus()));
        ErpApStatementDO statement = apStatementService.getApStatementByBizTypeAndBizId(ErpBizTypeEnum.OUTSOURCE_FEE.getType(), fee.getId());
        if (statement != null) {
            vo.setStatementId(statement.getId());
            vo.setStatementNo(statement.getStatementNo());
            vo.setPaidAmount(statement.getPaidAmount());
            vo.setRemainAmount(statement.getRemainAmount());
            vo.setReconciliationStatus(statement.getStatus());
            vo.setReconciliationStatusName(resolveStatementStatusName(statement.getStatus()));
        }
        if (fee.getCreator() != null) {
            Map<Long, AdminUserRespDTO> userMap = buildUserMapByCreator(fee.getCreator());
            MapUtils.findAndThen(userMap, parseUserId(fee.getCreator()), user -> vo.setCreatorName(user.getNickname()));
        }
        return vo;
    }

    private Map<Long, AdminUserRespDTO> buildUserMapByCreator(String creator) {
        Long userId = parseUserId(creator);
        return userId == null ? Collections.emptyMap() : adminUserApi.getUserMap(Collections.singletonList(userId));
    }

    private String resolveDoneStatusName(Integer status) {
        return status != null && status == 20 ? "已完成" : null;
    }

    private List<ErpOutsourceInboundPrintDataRespVO.ReconciliationRecord> buildReconciliationRecords(Long orderId) {
        if (orderId == null) {
            return Collections.emptyList();
        }
        ErpOutsourceFeePageReqVO pageReqVO = new ErpOutsourceFeePageReqVO();
        pageReqVO.setPageNo(1);
        pageReqVO.setPageSize(Integer.MAX_VALUE);
        pageReqVO.setOrderId(orderId);
        List<ErpOutsourceFeeDO> feeList = outsourceOrderService.getOutsourceFeePage(pageReqVO).getList();
        if (CollUtil.isEmpty(feeList)) {
            return Collections.emptyList();
        }
        List<Long> feeIds = convertList(feeList, ErpOutsourceFeeDO::getId);
        List<ErpApStatementDO> statementList = apStatementService.getApStatementListByBizTypeAndBizIds(
                ErpBizTypeEnum.OUTSOURCE_FEE.getType(), feeIds);
        if (CollUtil.isEmpty(statementList)) {
            return Collections.emptyList();
        }
        List<Long> statementIds = convertSet(statementList, ErpApStatementDO::getId).stream().toList();
        List<ErpFinancePaymentAllocateDO> allocateList = financePaymentService.getFinancePaymentAllocateListByStatementIds(statementIds);
        if (CollUtil.isEmpty(allocateList)) {
            return Collections.emptyList();
        }
        List<Long> paymentIds = convertSet(allocateList, ErpFinancePaymentAllocateDO::getPaymentId).stream().toList();
        Map<Long, ErpFinancePaymentDO> paymentMap = paymentIds.isEmpty()
                ? Collections.emptyMap()
                : convertMap(financePaymentService.getFinancePaymentListByIds(paymentIds), ErpFinancePaymentDO::getId);
        List<Long> operatorIds = paymentMap.values().stream()
                .map(ErpFinancePaymentDO::getCreator)
                .map(ErpOutsourceOrderController::parseCreatorId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, AdminUserRespDTO> operatorMap = operatorIds.isEmpty()
                ? Collections.emptyMap()
                : adminUserApi.getUserMap(operatorIds);

        return allocateList.stream()
                .sorted(Comparator.comparing(ErpFinancePaymentAllocateDO::getCreateTime,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .map(allocate -> {
                    ErpOutsourceInboundPrintDataRespVO.ReconciliationRecord record =
                            new ErpOutsourceInboundPrintDataRespVO.ReconciliationRecord();
                    ErpFinancePaymentDO payment = paymentMap.get(allocate.getPaymentId());
                    record.setPaymentId(allocate.getPaymentId());
                    record.setPaymentNo(payment == null ? null : payment.getNo());
                    record.setAllocateAmount(allocate.getAllocateAmount());
                    record.setPaymentTime(payment == null ? null : payment.getPaymentTime());
                    record.setStatus(allocate.getStatus());
                    record.setStatusName(resolveAllocateStatusName(allocate.getStatus()));
                    record.setRemark(allocate.getRemark());
                    if (payment != null) {
                        MapUtils.findAndThen(operatorMap, parseCreatorId(payment.getCreator()),
                                user -> record.setOperatorName(user.getNickname()));
                    }
                    return record;
                })
                .toList();
    }

    private String resolveAllocateStatusName(Integer status) {
        return Arrays.stream(ErpFinancePaymentAllocateStatusEnum.values())
                .filter(item -> Objects.equals(item.getStatus(), status))
                .map(ErpFinancePaymentAllocateStatusEnum::getName)
                .findFirst()
                .orElse(null);
    }

    private static Long parseCreatorId(String creator) {
        return parseUserId(creator);
    }

}
