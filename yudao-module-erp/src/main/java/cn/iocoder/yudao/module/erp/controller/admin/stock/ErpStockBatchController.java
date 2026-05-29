package cn.iocoder.yudao.module.erp.controller.admin.stock;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.stock.vo.batch.ErpStockBatchAdjustReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.stock.vo.batch.ErpStockBatchAdjustmentPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.stock.vo.batch.ErpStockBatchAdjustmentRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.stock.vo.batch.ErpStockBatchAllocationRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.stock.vo.batch.ErpStockBatchPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.stock.vo.batch.ErpStockBatchRecordPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.stock.vo.batch.ErpStockBatchRecordRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.stock.vo.batch.ErpStockBatchReservationActionReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.stock.vo.batch.ErpStockBatchReservationRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.stock.vo.batch.ErpStockBatchRebuildOutboundReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.stock.vo.batch.ErpStockBatchRebuildOutboundRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.stock.vo.batch.ErpStockBatchReserveOutboundReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.stock.vo.batch.ErpStockBatchRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpStockBatchAdjustmentDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpStockBatchAllocationDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpStockBatchDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpStockBatchRecordDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpStockBatchReservationDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpWarehouseDO;
import cn.iocoder.yudao.module.erp.service.product.ErpProductService;
import cn.iocoder.yudao.module.erp.service.stock.ErpStockBatchAdjustmentService;
import cn.iocoder.yudao.module.erp.service.stock.ErpStockBatchAllocationService;
import cn.iocoder.yudao.module.erp.service.stock.ErpStockBatchRecordService;
import cn.iocoder.yudao.module.erp.service.stock.ErpStockBatchRebuildService;
import cn.iocoder.yudao.module.erp.service.stock.ErpStockBatchReservationService;
import cn.iocoder.yudao.module.erp.service.stock.ErpStockBatchService;
import cn.iocoder.yudao.module.erp.service.stock.ErpWarehouseService;
import cn.iocoder.yudao.module.erp.service.stock.bo.ErpStockBatchAllocateOutboundReqBO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - ERP 批次库存")
@RestController
@RequestMapping("/erp/stock-batch")
@Validated
public class ErpStockBatchController {

    @Resource
    private ErpStockBatchService stockBatchService;
    @Resource
    private ErpStockBatchAdjustmentService stockBatchAdjustmentService;
    @Resource
    private ErpStockBatchRecordService stockBatchRecordService;
    @Resource
    private ErpStockBatchAllocationService stockBatchAllocationService;
    @Resource
    private ErpStockBatchRebuildService stockBatchRebuildService;
    @Resource
    private ErpStockBatchReservationService stockBatchReservationService;
    @Resource
    private ErpProductService productService;
    @Resource
    private ErpWarehouseService warehouseService;

    @GetMapping("/page")
    @Operation(summary = "获得批次库存分页")
    @PreAuthorize("@ss.hasPermission('erp:stock-batch:query')")
    public CommonResult<PageResult<ErpStockBatchRespVO>> getStockBatchPage(@Valid ErpStockBatchPageReqVO reqVO) {
        PageResult<ErpStockBatchDO> pageResult = stockBatchService.getStockBatchPage(reqVO);
        return success(buildStockBatchVOPageResult(pageResult));
    }

    @PostMapping("/adjust")
    @Operation(summary = "调整批次库存")
    @PreAuthorize("@ss.hasPermission('erp:stock-batch:update')")
    public CommonResult<ErpStockBatchAdjustmentRespVO> adjustStockBatch(
            @Valid @RequestBody ErpStockBatchAdjustReqVO reqVO) {
        ErpStockBatchAdjustmentDO adjustment = stockBatchAdjustmentService.adjustBatch(reqVO);
        return success(buildStockBatchAdjustmentVO(adjustment));
    }

    @GetMapping("/adjustment-page")
    @Operation(summary = "获得批次调整单分页")
    @PreAuthorize("@ss.hasPermission('erp:stock-batch:query')")
    public CommonResult<PageResult<ErpStockBatchAdjustmentRespVO>> getStockBatchAdjustmentPage(
            @Valid ErpStockBatchAdjustmentPageReqVO reqVO) {
        PageResult<ErpStockBatchAdjustmentDO> pageResult =
                stockBatchAdjustmentService.getStockBatchAdjustmentPage(reqVO);
        return success(buildStockBatchAdjustmentVOPageResult(pageResult));
    }

    @GetMapping("/record-page")
    @Operation(summary = "获得批次库存流水分页")
    @PreAuthorize("@ss.hasPermission('erp:stock-batch:query')")
    public CommonResult<PageResult<ErpStockBatchRecordRespVO>> getStockBatchRecordPage(
            @Valid ErpStockBatchRecordPageReqVO reqVO) {
        PageResult<ErpStockBatchRecordDO> pageResult = stockBatchRecordService.getStockBatchRecordPage(reqVO);
        return success(buildStockBatchRecordVOPageResult(pageResult));
    }

    @GetMapping("/allocation-by-biz")
    @Operation(summary = "按业务单据获得批次出库分配明细")
    @Parameters({
            @Parameter(name = "bizType", description = "业务类型", required = true),
            @Parameter(name = "bizId", description = "业务编号", required = true)
    })
    @PreAuthorize("@ss.hasPermission('erp:stock-batch:query')")
    public CommonResult<List<ErpStockBatchAllocationRespVO>> getAllocationListByBiz(
            @RequestParam("bizType") Integer bizType,
            @RequestParam("bizId") Long bizId) {
        List<ErpStockBatchAllocationDO> allocations = stockBatchAllocationService.getAllocationListByBiz(bizType, bizId);
        return success(BeanUtils.toBean(allocations, ErpStockBatchAllocationRespVO.class));
    }

    @GetMapping("/allocation-by-stock-batch")
    @Operation(summary = "按批次库存获得出库消耗明细")
    @Parameter(name = "stockBatchId", description = "批次库存编号", required = true)
    @PreAuthorize("@ss.hasPermission('erp:stock-batch:query')")
    public CommonResult<List<ErpStockBatchAllocationRespVO>> getAllocationListByStockBatch(
            @RequestParam("stockBatchId") Long stockBatchId) {
        List<ErpStockBatchAllocationDO> allocations = stockBatchAllocationService.getAllocationListByStockBatchId(stockBatchId);
        return success(BeanUtils.toBean(allocations, ErpStockBatchAllocationRespVO.class));
    }

    @PostMapping("/reserve-outbound")
    @Operation(summary = "预占批次出库库存")
    @PreAuthorize("@ss.hasPermission('erp:stock-batch:reserve')")
    public CommonResult<List<ErpStockBatchReservationRespVO>> reserveOutboundBatch(
            @Valid @RequestBody ErpStockBatchReserveOutboundReqVO reqVO) {
        List<ErpStockBatchReservationDO> reservations = stockBatchReservationService.reserveOutbound(
                BeanUtils.toBean(reqVO, ErpStockBatchAllocateOutboundReqBO.class));
        return success(buildStockBatchReservationVOList(reservations));
    }

    @PostMapping("/release-reservation")
    @Operation(summary = "释放批次出库预占")
    @PreAuthorize("@ss.hasPermission('erp:stock-batch:reserve')")
    public CommonResult<Boolean> releaseOutboundReservation(
            @Valid @RequestBody ErpStockBatchReservationActionReqVO reqVO) {
        stockBatchReservationService.releaseReservation(reqVO.getBizType(), reqVO.getBizId(), reqVO.getRemark());
        return success(true);
    }

    @PostMapping("/deduct-reservation")
    @Operation(summary = "实扣批次出库预占")
    @PreAuthorize("@ss.hasPermission('erp:stock-batch:reserve')")
    public CommonResult<List<ErpStockBatchAllocationRespVO>> deductOutboundReservation(
            @Valid @RequestBody ErpStockBatchReservationActionReqVO reqVO) {
        List<ErpStockBatchAllocationDO> allocations =
                stockBatchReservationService.deductReservation(reqVO.getBizType(), reqVO.getBizId(), reqVO.getRemark());
        return success(BeanUtils.toBean(allocations, ErpStockBatchAllocationRespVO.class));
    }

    @GetMapping("/reservation-by-biz")
    @Operation(summary = "按业务单据获得批次出库预占明细")
    @Parameters({
            @Parameter(name = "bizType", description = "业务类型", required = true),
            @Parameter(name = "bizId", description = "业务编号", required = true)
    })
    @PreAuthorize("@ss.hasPermission('erp:stock-batch:query')")
    public CommonResult<List<ErpStockBatchReservationRespVO>> getReservationListByBiz(
            @RequestParam("bizType") Integer bizType,
            @RequestParam("bizId") Long bizId) {
        List<ErpStockBatchReservationDO> reservations =
                stockBatchReservationService.getReservationListByBiz(bizType, bizId);
        return success(buildStockBatchReservationVOList(reservations));
    }

    @GetMapping("/reservation-by-stock-batch")
    @Operation(summary = "按批次库存获得出库预占明细")
    @Parameter(name = "stockBatchId", description = "批次库存编号", required = true)
    @PreAuthorize("@ss.hasPermission('erp:stock-batch:query')")
    public CommonResult<List<ErpStockBatchReservationRespVO>> getReservationListByStockBatch(
            @RequestParam("stockBatchId") Long stockBatchId) {
        List<ErpStockBatchReservationDO> reservations =
                stockBatchReservationService.getReservationListByStockBatchId(stockBatchId);
        return success(buildStockBatchReservationVOList(reservations));
    }

    @PostMapping("/rebuild-outbound-preview")
    @Operation(summary = "预览历史出库批次重建")
    @PreAuthorize("@ss.hasPermission('erp:stock-batch:query')")
    public CommonResult<ErpStockBatchRebuildOutboundRespVO> previewOutboundBatchRebuild(
            @Valid @RequestBody ErpStockBatchRebuildOutboundReqVO reqVO) {
        return success(stockBatchRebuildService.previewOutbound(reqVO));
    }

    @PostMapping("/rebuild-outbound")
    @Operation(summary = "执行历史出库批次重建")
    @PreAuthorize("@ss.hasPermission('erp:stock-batch:rebuild')")
    public CommonResult<ErpStockBatchRebuildOutboundRespVO> rebuildOutboundBatch(
            @Valid @RequestBody ErpStockBatchRebuildOutboundReqVO reqVO) {
        return success(stockBatchRebuildService.rebuildOutbound(reqVO));
    }

    private PageResult<ErpStockBatchRespVO> buildStockBatchVOPageResult(PageResult<ErpStockBatchDO> pageResult) {
        if (CollUtil.isEmpty(pageResult.getList())) {
            return PageResult.empty(pageResult.getTotal());
        }
        Map<Long, ErpProductRespVO> productMap = productService.getProductVOMap(
                convertSet(pageResult.getList(), ErpStockBatchDO::getProductId));
        Map<Long, ErpWarehouseDO> warehouseMap = warehouseService.getWarehouseMap(
                convertSet(pageResult.getList(), ErpStockBatchDO::getWarehouseId));
        return BeanUtils.toBean(pageResult, ErpStockBatchRespVO.class, item -> {
            MapUtils.findAndThen(productMap, item.getProductId(), product -> item.setProductName(product.getName())
                    .setMaterialCode(product.getMaterialCode())
                    .setUnitName(product.getUnitName()));
            MapUtils.findAndThen(warehouseMap, item.getWarehouseId(),
                    warehouse -> item.setWarehouseName(warehouse.getName()));
        });
    }

    private ErpStockBatchRespVO buildStockBatchVO(ErpStockBatchDO stockBatch) {
        if (stockBatch == null) {
            return null;
        }
        ErpStockBatchRespVO respVO = BeanUtils.toBean(stockBatch, ErpStockBatchRespVO.class);
        if (stockBatch.getProductId() != null) {
            MapUtils.findAndThen(productService.getProductVOMap(Collections.singleton(stockBatch.getProductId())),
                    stockBatch.getProductId(), product -> respVO.setProductName(product.getName())
                            .setMaterialCode(product.getMaterialCode())
                            .setUnitName(product.getUnitName()));
        }
        if (stockBatch.getWarehouseId() != null) {
            MapUtils.findAndThen(warehouseService.getWarehouseMap(Collections.singleton(stockBatch.getWarehouseId())),
                    stockBatch.getWarehouseId(), warehouse -> respVO.setWarehouseName(warehouse.getName()));
        }
        return respVO;
    }

    private PageResult<ErpStockBatchAdjustmentRespVO> buildStockBatchAdjustmentVOPageResult(
            PageResult<ErpStockBatchAdjustmentDO> pageResult) {
        if (CollUtil.isEmpty(pageResult.getList())) {
            return PageResult.empty(pageResult.getTotal());
        }
        Map<Long, ErpProductRespVO> productMap = productService.getProductVOMap(
                convertSet(pageResult.getList(), ErpStockBatchAdjustmentDO::getProductId));
        Map<Long, ErpWarehouseDO> warehouseMap = warehouseService.getWarehouseMap(
                convertSet(pageResult.getList(), ErpStockBatchAdjustmentDO::getWarehouseId));
        return BeanUtils.toBean(pageResult, ErpStockBatchAdjustmentRespVO.class, item -> {
            MapUtils.findAndThen(productMap, item.getProductId(), product -> item.setProductName(product.getName())
                    .setMaterialCode(product.getMaterialCode())
                    .setUnitName(product.getUnitName()));
            MapUtils.findAndThen(warehouseMap, item.getWarehouseId(),
                    warehouse -> item.setWarehouseName(warehouse.getName()));
        });
    }

    private ErpStockBatchAdjustmentRespVO buildStockBatchAdjustmentVO(ErpStockBatchAdjustmentDO adjustment) {
        if (adjustment == null) {
            return null;
        }
        ErpStockBatchAdjustmentRespVO respVO = BeanUtils.toBean(adjustment, ErpStockBatchAdjustmentRespVO.class);
        MapUtils.findAndThen(productService.getProductVOMap(Collections.singleton(adjustment.getProductId())),
                adjustment.getProductId(), product -> respVO.setProductName(product.getName())
                        .setMaterialCode(product.getMaterialCode())
                        .setUnitName(product.getUnitName()));
        MapUtils.findAndThen(warehouseService.getWarehouseMap(Collections.singleton(adjustment.getWarehouseId())),
                adjustment.getWarehouseId(), warehouse -> respVO.setWarehouseName(warehouse.getName()));
        return respVO;
    }

    private List<ErpStockBatchReservationRespVO> buildStockBatchReservationVOList(
            List<ErpStockBatchReservationDO> reservations) {
        if (CollUtil.isEmpty(reservations)) {
            return Collections.emptyList();
        }
        Map<Long, ErpProductRespVO> productMap = productService.getProductVOMap(
                convertSet(reservations, ErpStockBatchReservationDO::getProductId));
        Map<Long, ErpWarehouseDO> warehouseMap = warehouseService.getWarehouseMap(
                convertSet(reservations, ErpStockBatchReservationDO::getWarehouseId));
        return BeanUtils.toBean(reservations, ErpStockBatchReservationRespVO.class, item -> {
            MapUtils.findAndThen(productMap, item.getProductId(), product -> item.setProductName(product.getName())
                    .setMaterialCode(product.getMaterialCode())
                    .setUnitName(product.getUnitName()));
            MapUtils.findAndThen(warehouseMap, item.getWarehouseId(),
                    warehouse -> item.setWarehouseName(warehouse.getName()));
        });
    }

    private PageResult<ErpStockBatchRecordRespVO> buildStockBatchRecordVOPageResult(
            PageResult<ErpStockBatchRecordDO> pageResult) {
        if (CollUtil.isEmpty(pageResult.getList())) {
            return PageResult.empty(pageResult.getTotal());
        }
        Map<Long, ErpProductRespVO> productMap = productService.getProductVOMap(
                convertSet(pageResult.getList(), ErpStockBatchRecordDO::getProductId));
        Map<Long, ErpWarehouseDO> warehouseMap = warehouseService.getWarehouseMap(
                convertSet(pageResult.getList(), ErpStockBatchRecordDO::getWarehouseId));
        return BeanUtils.toBean(pageResult, ErpStockBatchRecordRespVO.class, item -> {
            MapUtils.findAndThen(productMap, item.getProductId(), product -> item.setProductName(product.getName())
                    .setMaterialCode(product.getMaterialCode())
                    .setUnitName(product.getUnitName()));
            MapUtils.findAndThen(warehouseMap, item.getWarehouseId(),
                    warehouse -> item.setWarehouseName(warehouse.getName()));
        });
    }

}
