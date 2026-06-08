package cn.iocoder.yudao.module.erp.controller.admin.stock;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.erp.controller.admin.stock.vo.warehouse.category.ErpWarehouseCategoryListReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.stock.vo.warehouse.category.ErpWarehouseCategoryRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.stock.vo.warehouse.category.ErpWarehouseCategorySaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpWarehouseCategoryDO;
import cn.iocoder.yudao.module.erp.service.stock.ErpWarehouseCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

@Tag(name = "管理后台 - ERP 仓库分类")
@RestController
@RequestMapping("/erp/warehouse-category")
@Validated
public class ErpWarehouseCategoryController {

    @Resource
    private ErpWarehouseCategoryService warehouseCategoryService;

    @PostMapping("/create")
    @Operation(summary = "创建仓库分类")
    @PreAuthorize("@ss.hasPermission('erp:warehouse-category:create')")
    public CommonResult<Long> createWarehouseCategory(@Valid @RequestBody ErpWarehouseCategorySaveReqVO createReqVO) {
        return success(warehouseCategoryService.createWarehouseCategory(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新仓库分类")
    @PreAuthorize("@ss.hasPermission('erp:warehouse-category:update')")
    public CommonResult<Boolean> updateWarehouseCategory(@Valid @RequestBody ErpWarehouseCategorySaveReqVO updateReqVO) {
        warehouseCategoryService.updateWarehouseCategory(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除仓库分类")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('erp:warehouse-category:delete')")
    public CommonResult<Boolean> deleteWarehouseCategory(@RequestParam("id") Long id) {
        warehouseCategoryService.deleteWarehouseCategory(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得仓库分类")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:warehouse-category:query')")
    public CommonResult<ErpWarehouseCategoryRespVO> getWarehouseCategory(@RequestParam("id") Long id) {
        ErpWarehouseCategoryDO category = warehouseCategoryService.getWarehouseCategory(id);
        return success(BeanUtils.toBean(category, ErpWarehouseCategoryRespVO.class));
    }

    @GetMapping("/list")
    @Operation(summary = "获得仓库分类列表")
    @PreAuthorize("@ss.hasPermission('erp:warehouse-category:query')")
    public CommonResult<List<ErpWarehouseCategoryRespVO>> getWarehouseCategoryList(@Valid ErpWarehouseCategoryListReqVO listReqVO) {
        List<ErpWarehouseCategoryDO> list = warehouseCategoryService.getWarehouseCategoryList(listReqVO);
        return success(BeanUtils.toBean(list, ErpWarehouseCategoryRespVO.class));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得仓库分类精简列表", description = "只包含被开启的分类，主要用于前端的下拉选项")
    public CommonResult<List<ErpWarehouseCategoryRespVO>> getWarehouseCategorySimpleList() {
        List<ErpWarehouseCategoryDO> list = warehouseCategoryService.getWarehouseCategoryList(
                new ErpWarehouseCategoryListReqVO().setStatus(CommonStatusEnum.ENABLE.getStatus()));
        return success(convertList(list, category -> new ErpWarehouseCategoryRespVO()
                .setId(category.getId())
                .setName(category.getName())
                .setParentId(category.getParentId())));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出仓库分类 Excel")
    @PreAuthorize("@ss.hasPermission('erp:warehouse-category:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportWarehouseCategoryExcel(@Valid ErpWarehouseCategoryListReqVO listReqVO,
                                             HttpServletResponse response) throws IOException {
        List<ErpWarehouseCategoryDO> list = warehouseCategoryService.getWarehouseCategoryList(listReqVO);
        ExcelUtils.write(response, "仓库分类.xls", "数据", ErpWarehouseCategoryRespVO.class,
                BeanUtils.toBean(list, ErpWarehouseCategoryRespVO.class));
    }

}
