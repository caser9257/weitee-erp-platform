package cn.iocoder.yudao.module.erp.controller.admin.purchase;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.sourcebatch.ErpPurchaseSourceBatchPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.sourcebatch.ErpPurchaseSourceBatchRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.sourcebatch.ErpPurchaseSourceBatchTraceRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.sourcebatch.ErpPurchaseSourceBatchSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInStockExecuteDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInStockExecuteItemBatchDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInStockExecuteItemDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseOrderDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseSourceBatchDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpSupplierDO;
import cn.iocoder.yudao.module.erp.service.product.ErpProductService;
import cn.iocoder.yudao.module.erp.service.purchase.ErpPurchaseOrderService;
import cn.iocoder.yudao.module.erp.service.purchase.ErpPurchaseInService;
import cn.iocoder.yudao.module.erp.service.purchase.ErpPurchaseSourceBatchService;
import cn.iocoder.yudao.module.erp.service.purchase.ErpSupplierService;
import cn.iocoder.yudao.module.erp.service.stock.ErpWarehouseService;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpWarehouseDO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - ERP 采购来源批次")
@RestController
@RequestMapping("/erp/purchase-source-batch")
@Validated
public class ErpPurchaseSourceBatchController {

    @Resource
    private ErpPurchaseSourceBatchService purchaseSourceBatchService;
    @Resource
    private ErpProductService productService;
    @Resource
    private ErpSupplierService supplierService;
    @Resource
    private ErpPurchaseOrderService purchaseOrderService;
    @Resource
    private ErpPurchaseInService purchaseInService;
    @Resource
    private ErpWarehouseService warehouseService;

    @PostMapping("/create")
    @Operation(summary = "创建采购来源批次")
    @PreAuthorize("@ss.hasPermission('erp:purchase-source-batch:create')")
    public CommonResult<Long> createPurchaseSourceBatch(@Valid @RequestBody ErpPurchaseSourceBatchSaveReqVO createReqVO) {
        return success(purchaseSourceBatchService.createPurchaseSourceBatch(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新采购来源批次")
    @PreAuthorize("@ss.hasPermission('erp:purchase-source-batch:update')")
    public CommonResult<Boolean> updatePurchaseSourceBatch(@Valid @RequestBody ErpPurchaseSourceBatchSaveReqVO updateReqVO) {
        purchaseSourceBatchService.updatePurchaseSourceBatch(updateReqVO);
        return success(true);
    }

    @PutMapping("/close")
    @Operation(summary = "关闭采购来源批次")
    @PreAuthorize("@ss.hasPermission('erp:purchase-source-batch:close')")
    public CommonResult<Boolean> closePurchaseSourceBatch(@RequestParam("id") Long id) {
        purchaseSourceBatchService.closePurchaseSourceBatch(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得采购来源批次")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('erp:purchase-source-batch:query')")
    public CommonResult<ErpPurchaseSourceBatchRespVO> getPurchaseSourceBatch(@RequestParam("id") Long id) {
        ErpPurchaseSourceBatchDO batch = purchaseSourceBatchService.getPurchaseSourceBatch(id);
        if (batch == null) {
            return success(null);
        }
        return success(buildRespVOList(List.of(batch)).get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得采购来源批次分页")
    @PreAuthorize("@ss.hasPermission('erp:purchase-source-batch:query')")
    public CommonResult<PageResult<ErpPurchaseSourceBatchRespVO>> getPurchaseSourceBatchPage(
            @Valid ErpPurchaseSourceBatchPageReqVO pageReqVO) {
        PageResult<ErpPurchaseSourceBatchDO> pageResult = purchaseSourceBatchService.getPurchaseSourceBatchPage(pageReqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty(pageResult.getTotal()));
        }
        return success(new PageResult<>(buildRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/get-trace")
    @Operation(summary = "获得采购来源批次追溯信息")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('erp:purchase-source-batch:query')")
    public CommonResult<ErpPurchaseSourceBatchTraceRespVO> getPurchaseSourceBatchTrace(@RequestParam("id") Long id) {
        ErpPurchaseSourceBatchDO batch = purchaseSourceBatchService.getPurchaseSourceBatch(id);
        if (batch == null) {
            return success(null);
        }
        List<ErpPurchaseInStockExecuteItemBatchDO> executeItemBatchList =
                purchaseInService.getPurchaseInStockExecuteItemBatchListByPurchaseSourceBatchId(id);
        Map<Long, ErpPurchaseInStockExecuteItemDO> executeItemMap = convertMap(
                purchaseInService.getPurchaseInStockExecuteItemListByIds(
                        convertSet(executeItemBatchList, ErpPurchaseInStockExecuteItemBatchDO::getExecuteItemId)),
                ErpPurchaseInStockExecuteItemDO::getId);
        Map<Long, ErpPurchaseInStockExecuteDO> executeMap = convertMap(
                purchaseInService.getPurchaseInStockExecuteListByIds(
                        convertSet(executeItemMap.values(), ErpPurchaseInStockExecuteItemDO::getExecuteId)),
                ErpPurchaseInStockExecuteDO::getId);
        Map<Long, ErpProductRespVO> productMap = productService.getProductVOMap(
                convertSet(executeItemBatchList, ErpPurchaseInStockExecuteItemBatchDO::getProductId));
        Map<Long, ErpWarehouseDO> warehouseMap = warehouseService.getWarehouseMap(
                convertSet(executeItemBatchList, ErpPurchaseInStockExecuteItemBatchDO::getWarehouseId));

        ErpPurchaseSourceBatchTraceRespVO traceRespVO = new ErpPurchaseSourceBatchTraceRespVO();
        traceRespVO.setSourceBatch(buildRespVOList(List.of(batch)).get(0));
        traceRespVO.setTraceItems(BeanUtils.toBean(executeItemBatchList, ErpPurchaseSourceBatchTraceRespVO.TraceItem.class, item -> {
            ErpPurchaseInStockExecuteItemDO executeItem = executeItemMap.get(item.getExecuteItemId());
            if (executeItem != null) {
                item.setExecuteId(executeItem.getExecuteId());
                MapUtils.findAndThen(executeMap, executeItem.getExecuteId(), execute ->
                        item.setExecuteNo(execute.getNo()).setExecuteStatus(execute.getStatus()).setExecuteRemark(execute.getRemark()));
            }
            MapUtils.findAndThen(productMap, item.getProductId(), product -> item.setProductName(product.getName()));
            MapUtils.findAndThen(warehouseMap, item.getWarehouseId(), warehouse -> item.setWarehouseName(warehouse.getName()));
        }));
        if (traceRespVO.getTraceItems() == null) {
            traceRespVO.setTraceItems(Collections.emptyList());
        }
        return success(traceRespVO);
    }

    private List<ErpPurchaseSourceBatchRespVO> buildRespVOList(List<ErpPurchaseSourceBatchDO> list) {
        Map<Long, ErpProductRespVO> productMap = productService.getProductVOMap(convertSet(list, ErpPurchaseSourceBatchDO::getProductId));
        Map<Long, ErpSupplierDO> supplierMap = supplierService.getSupplierMap(convertSet(list, ErpPurchaseSourceBatchDO::getSupplierId));
        Map<Long, String> orderNoMap = buildPurchaseOrderNoMap(convertSet(list, ErpPurchaseSourceBatchDO::getPurchaseOrderId));
        return BeanUtils.toBean(list, ErpPurchaseSourceBatchRespVO.class, item -> {
            ErpProductRespVO product = productMap.get(item.getProductId());
            if (product != null) {
                item.setProductName(product.getName());
            }
            ErpSupplierDO supplier = supplierMap.get(item.getSupplierId());
            if (supplier != null) {
                item.setSupplierName(supplier.getName());
            }
            item.setPurchaseOrderNo(orderNoMap.get(item.getPurchaseOrderId()));
        });
    }

    private Map<Long, String> buildPurchaseOrderNoMap(Collection<Long> orderIds) {
        return convertMap(purchaseOrderService.getPurchaseOrderList(orderIds),
                ErpPurchaseOrderDO::getId, ErpPurchaseOrderDO::getNo);
    }
}
