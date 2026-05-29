package cn.iocoder.yudao.module.erp.controller.admin.stock;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.stock.vo.move.ErpStockMovePageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.stock.vo.move.ErpStockMovePrintDataRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.stock.vo.move.ErpStockMoveRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.stock.vo.move.ErpStockMoveSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpStockDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpStockMoveDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpStockMoveItemDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpWarehouseDO;
import cn.iocoder.yudao.module.erp.service.product.ErpProductService;
import cn.iocoder.yudao.module.erp.service.stock.ErpStockMoveService;
import cn.iocoder.yudao.module.erp.service.stock.ErpStockService;
import cn.iocoder.yudao.module.erp.service.stock.ErpWarehouseService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMultiMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.erp.util.ErpUserIdUtils.parseUserId;

@Tag(name = "管理后台 - ERP 库存调拨单")
@RestController
@RequestMapping("/erp/stock-move")
@Validated
public class ErpStockMoveController {

    @Resource
    private ErpStockMoveService stockMoveService;
    @Resource
    private ErpStockService stockService;
    @Resource
    private ErpWarehouseService warehouseService;
    @Resource
    private ErpProductService productService;

    @Resource
    private AdminUserApi adminUserApi;

    @PostMapping("/create")
    @Operation(summary = "创建库存调拨单")
    @PreAuthorize("@ss.hasPermission('erp:stock-move:create')")
    public CommonResult<Long> createStockMove(@Valid @RequestBody ErpStockMoveSaveReqVO createReqVO) {
        return success(stockMoveService.createStockMove(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新库存调拨单")
    @PreAuthorize("@ss.hasPermission('erp:stock-move:update')")
    public CommonResult<Boolean> updateStockMove(@Valid @RequestBody ErpStockMoveSaveReqVO updateReqVO) {
        stockMoveService.updateStockMove(updateReqVO);
        return success(true);
    }

    @PutMapping("/update-status")
    @Operation(summary = "更新库存调拨单的状态")
    @PreAuthorize("@ss.hasPermission('erp:stock-move:update-status')")
    public CommonResult<Boolean> updateStockMoveStatus(@RequestParam("id") Long id,
                                                     @RequestParam("status") Integer status) {
        stockMoveService.updateStockMoveStatus(id, status);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除库存调拨单")
    @Parameter(name = "ids", description = "编号数组", required = true)
    @PreAuthorize("@ss.hasPermission('erp:stock-move:delete')")
    public CommonResult<Boolean> deleteStockMove(@RequestParam("ids") List<Long> ids) {
        stockMoveService.deleteStockMove(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得库存调拨单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:stock-move:query')")
    public CommonResult<ErpStockMoveRespVO> getStockMove(@RequestParam("id") Long id) {
        ErpStockMoveDO stockMove = stockMoveService.getStockMove(id);
        if (stockMove == null) {
            return success(null);
        }
        List<ErpStockMoveItemDO> stockMoveItemList = stockMoveService.getStockMoveItemListByMoveId(id);
        List<ErpStockMoveItemDO> safeStockMoveItemList = stockMoveItemList == null ? Collections.emptyList() : stockMoveItemList;
        Map<Long, ErpProductRespVO> productMap = productService.getProductVOMap(
                convertSet(safeStockMoveItemList, ErpStockMoveItemDO::getProductId));
        Set<Long> warehouseIds = new LinkedHashSet<>(convertSet(safeStockMoveItemList, ErpStockMoveItemDO::getFromWarehouseId));
        warehouseIds.addAll(convertSet(safeStockMoveItemList, ErpStockMoveItemDO::getToWarehouseId));
        warehouseIds.removeIf(java.util.Objects::isNull);
        Map<Long, ErpWarehouseDO> warehouseMap = warehouseIds.isEmpty()
                ? Collections.emptyMap()
                : warehouseService.getWarehouseMap(warehouseIds);
        return success(BeanUtils.toBean(stockMove, ErpStockMoveRespVO.class, stockMoveVO ->
                stockMoveVO.setItems(BeanUtils.toBean(stockMoveItemList, ErpStockMoveRespVO.Item.class, item -> {
                    ErpStockDO stock = stockService.getStock(item.getProductId(), item.getFromWarehouseId());
                    item.setStockCount(stock != null ? stock.getCount() : BigDecimal.ZERO);
                    MapUtils.findAndThen(productMap, item.getProductId(), product -> item.setProductName(product.getName())
                            .setProductBarCode(product.getBarCode()).setProductUnitName(product.getUnitName()));
                    MapUtils.findAndThen(warehouseMap, item.getFromWarehouseId(),
                            warehouse -> item.setFromWarehouseName(warehouse.getName()));
                    MapUtils.findAndThen(warehouseMap, item.getToWarehouseId(),
                            warehouse -> item.setToWarehouseName(warehouse.getName()));
                }))));
    }

    @GetMapping("/get-print-data")
    @Operation(summary = "获得库存调拨打印数据")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:stock-move:query')")
    public CommonResult<ErpStockMovePrintDataRespVO> getStockMovePrintData(@RequestParam("id") Long id) {
        ErpStockMoveRespVO stockMove = getStockMove(id).getData();
        if (stockMove == null) {
            return success(null);
        }
        ErpStockMovePrintDataRespVO printData = new ErpStockMovePrintDataRespVO();
        printData.setStockMove(stockMove);
        printData.setSourceAttachments(buildSourceAttachments(stockMove.getFileUrl()));
        return success(printData);
    }

    @GetMapping("/page")
    @Operation(summary = "获得库存调拨单分页")
    @PreAuthorize("@ss.hasPermission('erp:stock-move:query')")
    public CommonResult<PageResult<ErpStockMoveRespVO>> getStockMovePage(@Valid ErpStockMovePageReqVO pageReqVO) {
        PageResult<ErpStockMoveDO> pageResult = stockMoveService.getStockMovePage(pageReqVO);
        return success(buildStockMoveVOPageResult(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出库存调拨单 Excel")
    @PreAuthorize("@ss.hasPermission('erp:stock-move:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportStockMoveExcel(@Valid ErpStockMovePageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ErpStockMoveRespVO> list = buildStockMoveVOPageResult(stockMoveService.getStockMovePage(pageReqVO)).getList();
        // 导出 Excel
        ExcelUtils.write(response, "库存调拨单.xls", "数据", ErpStockMoveRespVO.class, list);
    }

    private List<ErpStockMovePrintDataRespVO.SourceAttachment> buildSourceAttachments(String fileUrl) {
        if (StrUtil.isBlank(fileUrl)) {
            return Collections.emptyList();
        }
        ErpStockMovePrintDataRespVO.SourceAttachment attachment = new ErpStockMovePrintDataRespVO.SourceAttachment();
        attachment.setUrl(fileUrl);
        String cleanUrl = fileUrl.split("\\?")[0].split("#")[0];
        String[] segments = cleanUrl.split("/");
        attachment.setName(segments.length == 0 ? "附件" : segments[segments.length - 1]);
        return Collections.singletonList(attachment);
    }

    private PageResult<ErpStockMoveRespVO> buildStockMoveVOPageResult(PageResult<ErpStockMoveDO> pageResult) {
        if (CollUtil.isEmpty(pageResult.getList())) {
            return PageResult.empty(pageResult.getTotal());
        }
        // 1.1 调拨项
        List<ErpStockMoveItemDO> stockMoveItemList = stockMoveService.getStockMoveItemListByMoveIds(
                convertSet(pageResult.getList(), ErpStockMoveDO::getId));
        List<ErpStockMoveItemDO> safeStockMoveItemList = stockMoveItemList == null ? Collections.emptyList() : stockMoveItemList;
        Map<Long, List<ErpStockMoveItemDO>> stockMoveItemMap = convertMultiMap(safeStockMoveItemList, ErpStockMoveItemDO::getMoveId);
        // 1.2 产品信息
        Map<Long, ErpProductRespVO> productMap = productService.getProductVOMap(
                convertSet(safeStockMoveItemList, ErpStockMoveItemDO::getProductId));
        // 1.3 TODO 芋艿：搞仓库信息
        // 1.4 管理员信息
        Set<Long> creatorIds = convertSet(pageResult.getList(), stockMove -> parseUserId(stockMove.getCreator()));
        Map<Long, AdminUserRespDTO> userMap = creatorIds.isEmpty() ? Collections.emptyMap()
                : adminUserApi.getUserMap(creatorIds);
        // 2. 开始拼接
        return BeanUtils.toBean(pageResult, ErpStockMoveRespVO.class, stockMove -> {
            List<ErpStockMoveRespVO.Item> items = BeanUtils.toBean(
                    stockMoveItemMap.get(stockMove.getId()),
                    ErpStockMoveRespVO.Item.class,
                    item -> MapUtils.findAndThen(productMap, item.getProductId(),
                            product -> item.setProductName(product.getName())
                                    .setProductBarCode(product.getBarCode())
                                    .setProductUnitName(product.getUnitName())));
            stockMove.setItems(items == null ? Collections.emptyList() : items);
            stockMove.setProductNames(CollUtil.join(stockMove.getItems(), "，", ErpStockMoveRespVO.Item::getProductName));
            MapUtils.findAndThen(userMap, parseUserId(stockMove.getCreator()), user -> stockMove.setCreatorName(user.getNickname()));
        });
    }

}
