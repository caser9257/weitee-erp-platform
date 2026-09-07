package cn.weitee.erp.module.erp.controller.admin.product;

import cn.weitee.erp.framework.apilog.core.annotation.ApiAccessLog;
import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.framework.common.pojo.PageParam;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.framework.excel.core.util.ExcelUtils;
import cn.weitee.erp.module.erp.controller.admin.product.vo.unit.ErpProductUnitPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.product.vo.unit.ErpProductUnitRespVO;
import cn.weitee.erp.module.erp.controller.admin.product.vo.unit.ErpProductUnitSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductUnitDO;
import cn.weitee.erp.module.erp.service.product.ErpProductUnitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.weitee.erp.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.weitee.erp.framework.common.pojo.CommonResult.success;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertList;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - ERP 产品单位")
@RestController
@RequestMapping("/erp/product-unit")
@Validated
public class ErpProductUnitController {

    @Resource
    private ErpProductUnitService productUnitService;

    @PostMapping("/create")
    @Operation(summary = "创建产品单位")
    @PreAuthorize("@ss.hasPermission('erp:product-unit:create')")
    public CommonResult<Long> createProductUnit(@Valid @RequestBody ErpProductUnitSaveReqVO createReqVO) {
        return success(productUnitService.createProductUnit(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新产品单位")
    @PreAuthorize("@ss.hasPermission('erp:product-unit:update')")
    public CommonResult<Boolean> updateProductUnit(@Valid @RequestBody ErpProductUnitSaveReqVO updateReqVO) {
        productUnitService.updateProductUnit(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除产品单位")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('erp:product-unit:delete')")
    public CommonResult<Boolean> deleteProductUnit(@RequestParam("id") Long id) {
        productUnitService.deleteProductUnit(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得产品单位")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:product-unit:query')")
    public CommonResult<ErpProductUnitRespVO> getProductUnit(@RequestParam("id") Long id) {
        ErpProductUnitDO productUnit = productUnitService.getProductUnit(id);
        ErpProductUnitRespVO respVO = BeanUtils.toBean(productUnit, ErpProductUnitRespVO.class);
        normalizeUnitType(java.util.Collections.singletonList(respVO));
        fillBaseUnitName(java.util.Collections.singletonList(respVO));
        return success(respVO);
    }

    @GetMapping("/page")
    @Operation(summary = "获得产品单位分页")
    @PreAuthorize("@ss.hasPermission('erp:product-unit:query')")
    public CommonResult<PageResult<ErpProductUnitRespVO>> getProductUnitPage(@Valid ErpProductUnitPageReqVO pageReqVO) {
        PageResult<ErpProductUnitDO> pageResult = productUnitService.getProductUnitPage(pageReqVO);
        PageResult<ErpProductUnitRespVO> result = BeanUtils.toBean(pageResult, ErpProductUnitRespVO.class);
        normalizeUnitType(result.getList());
        fillBaseUnitName(result.getList());
        return success(result);
    }

    /**
     * 存量数据或未指定单位类型的数据兜底归一化为基本单位（0）
     */
    private void normalizeUnitType(List<ErpProductUnitRespVO> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        list.forEach(vo -> {
            if (vo.getUnitType() == null) {
                vo.setUnitType(cn.weitee.erp.module.erp.enums.product.ErpProductUnitTypeEnum.BASE.getType());
            }
        });
    }

    /**
     * 批量填充辅助单位的基本单位名称，避免 N+1
     */
    private void fillBaseUnitName(List<ErpProductUnitRespVO> list) {
        Set<Long> baseUnitIds = convertSet(list, ErpProductUnitRespVO::getBaseUnitId);
        if (baseUnitIds.isEmpty()) {
            return;
        }
        Map<Long, ErpProductUnitDO> baseUnitMap = productUnitService.getProductUnitMap(baseUnitIds);
        list.forEach(vo -> {
            ErpProductUnitDO baseUnit = vo.getBaseUnitId() != null ? baseUnitMap.get(vo.getBaseUnitId()) : null;
            if (baseUnit != null) {
                vo.setBaseUnitName(baseUnit.getName());
            }
        });
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得产品单位精简列表", description = "只包含被开启的单位，主要用于前端的下拉选项")
    public CommonResult<List<ErpProductUnitRespVO>> getProductUnitSimpleList() {
        List<ErpProductUnitDO> list = productUnitService.getProductUnitListByStatus(CommonStatusEnum.ENABLE.getStatus());
        List<ErpProductUnitRespVO> result = convertList(list, unit -> new ErpProductUnitRespVO().setId(unit.getId())
                .setName(unit.getName()).setQuantityPrecision(unit.getQuantityPrecision())
                .setUnitType(unit.getUnitType()).setBaseUnitId(unit.getBaseUnitId())
                .setConversionRate(unit.getConversionRate()));
        normalizeUnitType(result);
        return success(result);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出产品单位 Excel")
    @PreAuthorize("@ss.hasPermission('erp:product-unit:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportProductUnitExcel(@Valid ErpProductUnitPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ErpProductUnitDO> list = productUnitService.getProductUnitPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "产品单位.xls", "数据", ErpProductUnitRespVO.class,
                        BeanUtils.toBean(list, ErpProductUnitRespVO.class));
    }

}
