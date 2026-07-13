package cn.weitee.erp.module.erp.controller.admin.stock;

import cn.hutool.core.collection.CollUtil;
import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.weitee.erp.module.erp.controller.admin.stock.vo.assemble.ErpStockAssemblePageReqVO;
import cn.weitee.erp.module.erp.controller.admin.stock.vo.assemble.ErpStockAssembleRespVO;
import cn.weitee.erp.module.erp.controller.admin.stock.vo.assemble.ErpStockAssembleSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockAssembleDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockAssembleItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpWarehouseDO;
import cn.weitee.erp.module.erp.enums.stock.ErpStockAssembleActionTypeEnum;
import cn.weitee.erp.module.erp.service.product.ErpProductService;
import cn.weitee.erp.module.erp.service.stock.ErpStockAssembleService;
import cn.weitee.erp.module.erp.service.stock.ErpWarehouseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
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

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.weitee.erp.framework.common.pojo.CommonResult.success;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertMultiMap;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - ERP 组装拆卸单")
@RestController
@RequestMapping("/erp/stock-assemble")
@Validated
public class ErpStockAssembleController {

    @Resource
    private ErpStockAssembleService assembleService;
    @Resource
    private ErpProductService productService;
    @Resource
    private ErpWarehouseService warehouseService;

    @PostMapping("/create")
    @Operation(summary = "创建组装拆卸单")
    @PreAuthorize("@ss.hasPermission('erp:stock-assemble:create')")
    public CommonResult<Long> create(@Valid @RequestBody ErpStockAssembleSaveReqVO reqVO) {
        return success(assembleService.createStockAssemble(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新组装拆卸单")
    @PreAuthorize("@ss.hasPermission('erp:stock-assemble:update')")
    public CommonResult<Boolean> update(@Valid @RequestBody ErpStockAssembleSaveReqVO reqVO) {
        assembleService.updateStockAssemble(reqVO);
        return success(true);
    }

    @PutMapping("/update-status")
    @Operation(summary = "审核组装拆卸单")
    @PreAuthorize("@ss.hasPermission('erp:stock-assemble:update-status')")
    public CommonResult<Boolean> updateStatus(@RequestParam("id") Long id, @RequestParam("status") Integer status) {
        assembleService.updateStockAssembleStatus(id, status);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除组装拆卸单")
    @PreAuthorize("@ss.hasPermission('erp:stock-assemble:delete')")
    public CommonResult<Boolean> delete(@RequestParam("ids") List<Long> ids) {
        assembleService.deleteStockAssemble(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得组装拆卸单")
    @PreAuthorize("@ss.hasPermission('erp:stock-assemble:query')")
    public CommonResult<ErpStockAssembleRespVO> get(@RequestParam("id") Long id) {
        ErpStockAssembleDO assemble = assembleService.getStockAssemble(id);
        return success(assemble == null ? null : buildVO(assemble,
                assembleService.getStockAssembleItemListByAssembleId(id)));
    }

    @GetMapping("/page")
    @Operation(summary = "获得组装拆卸单分页")
    @PreAuthorize("@ss.hasPermission('erp:stock-assemble:query')")
    public CommonResult<PageResult<ErpStockAssembleRespVO>> page(@Valid ErpStockAssemblePageReqVO reqVO) {
        PageResult<ErpStockAssembleDO> pageResult = assembleService.getStockAssemblePage(reqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty(pageResult.getTotal()));
        }
        List<ErpStockAssembleItemDO> itemList = assembleService.getStockAssembleItemListByAssembleIds(
                convertSet(pageResult.getList(), ErpStockAssembleDO::getId));
        Map<Long, List<ErpStockAssembleItemDO>> itemMap = convertMultiMap(itemList, ErpStockAssembleItemDO::getAssembleId);
        Set<Long> productIds = new LinkedHashSet<>(convertSet(pageResult.getList(), ErpStockAssembleDO::getProductId));
        productIds.addAll(convertSet(itemList, ErpStockAssembleItemDO::getProductId));
        Map<Long, ErpProductRespVO> productMap = productIds.isEmpty()
                ? Collections.emptyMap() : productService.getProductVOMap(productIds);
        Map<Long, ErpWarehouseDO> warehouseMap = convertSet(pageResult.getList(), ErpStockAssembleDO::getWarehouseId).isEmpty()
                ? Collections.emptyMap() : warehouseService.getWarehouseMap(convertSet(pageResult.getList(), ErpStockAssembleDO::getWarehouseId));
        return success(BeanUtils.toBean(pageResult, ErpStockAssembleRespVO.class, vo -> {
            vo.setActionTypeName(resolveActionTypeName(vo.getActionType()));
            ErpProductRespVO product = productMap.get(vo.getProductId());
            if (product != null) {
                vo.setProductName(product.getName());
            }
            ErpWarehouseDO warehouse = warehouseMap.get(vo.getWarehouseId());
            if (warehouse != null) {
                vo.setWarehouseName(warehouse.getName());
            }
            vo.setItems(BeanUtils.toBean(itemMap.getOrDefault(vo.getId(), Collections.emptyList()),
                    ErpStockAssembleRespVO.Item.class, item -> {
                        ErpProductRespVO itemProduct = productMap.get(item.getProductId());
                        if (itemProduct != null) {
                            item.setProductName(itemProduct.getName());
                        }
                    }));
        }));
    }

    private ErpStockAssembleRespVO buildVO(ErpStockAssembleDO assemble, List<ErpStockAssembleItemDO> items) {
        ErpStockAssembleRespVO vo = BeanUtils.toBean(assemble, ErpStockAssembleRespVO.class);
        vo.setActionTypeName(resolveActionTypeName(assemble.getActionType()));
        ErpProductRespVO product = productService.getProductVOList(List.of(assemble.getProductId())).stream()
                .findFirst().orElse(null);
        if (product != null) {
            vo.setProductName(product.getName());
        }
        ErpWarehouseDO warehouse = warehouseService.getWarehouse(assemble.getWarehouseId());
        if (warehouse != null) {
            vo.setWarehouseName(warehouse.getName());
        }
        Set<Long> itemProductIds = convertSet(items, ErpStockAssembleItemDO::getProductId);
        Map<Long, ErpProductRespVO> productMap = itemProductIds.isEmpty()
                ? Collections.emptyMap() : productService.getProductVOMap(itemProductIds);
        vo.setItems(BeanUtils.toBean(items, ErpStockAssembleRespVO.Item.class, item -> {
            ErpProductRespVO itemProduct = productMap.get(item.getProductId());
            if (itemProduct != null) {
                item.setProductName(itemProduct.getName());
            }
        }));
        return vo;
    }

    private String resolveActionTypeName(String type) {
        for (ErpStockAssembleActionTypeEnum item : ErpStockAssembleActionTypeEnum.values()) {
            if (item.getType().equals(type)) {
                return item.getName();
            }
        }
        return type;
    }
}
