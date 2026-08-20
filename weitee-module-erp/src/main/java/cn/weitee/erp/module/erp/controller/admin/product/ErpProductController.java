package cn.weitee.erp.module.erp.controller.admin.product;

import cn.weitee.erp.framework.apilog.core.annotation.ApiAccessLog;
import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.framework.common.pojo.PageParam;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.framework.excel.core.util.ExcelUtils;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpProductImportResultVO;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpProductPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ProductSaveReqVO;
import cn.weitee.erp.framework.security.core.util.SecurityFrameworkUtils;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductDO;
import cn.weitee.erp.module.erp.service.product.ErpProductBpmService;
import cn.weitee.erp.module.erp.service.product.ErpProductImportService;
import cn.weitee.erp.module.erp.service.product.ErpProductService;
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
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;

import static cn.weitee.erp.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.weitee.erp.framework.apilog.core.enums.OperateTypeEnum.IMPORT;
import static cn.weitee.erp.framework.common.pojo.CommonResult.success;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertList;

@Tag(name = "管理后台 - ERP 产品")
@RestController
@RequestMapping("/erp/product")
@Validated
public class ErpProductController {

    @Resource
    private ErpProductService productService;
    @Resource
    private ErpProductImportService productImportService;
    @Resource
    private ErpProductBpmService productBpmService;

    @PostMapping("/create")
    @Operation(summary = "创建产品")
    @PreAuthorize("@ss.hasPermission('erp:product:create')")
    public CommonResult<Long> createProduct(@Valid @RequestBody ProductSaveReqVO createReqVO) {
        return success(productService.createProduct(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新产品")
    @PreAuthorize("@ss.hasPermission('erp:product:update')")
    public CommonResult<Boolean> updateProduct(@Valid @RequestBody ProductSaveReqVO updateReqVO) {
        productService.updateProduct(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除产品")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('erp:product:delete')")
    public CommonResult<Boolean> deleteProduct(@RequestParam("id") Long id) {
        productService.deleteProduct(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得产品")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:product:query')")
    public CommonResult<ErpProductRespVO> getProduct(@RequestParam("id") Long id) {
        return success(productService.getProductVOList(List.of(id)).stream().findFirst().orElse(null));
    }

    @GetMapping("/page")
    @Operation(summary = "获得产品分页")
    @PreAuthorize("@ss.hasPermission('erp:product:query')")
    public CommonResult<PageResult<ErpProductRespVO>> getProductPage(@Valid ErpProductPageReqVO pageReqVO) {
        return success(productService.getProductVOPage(pageReqVO));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得产品精简列表", description = "只包含启用状态的产品，主要用于下拉选项")
    public CommonResult<List<ErpProductRespVO>> getProductSimpleList() {
        List<ErpProductRespVO> list = productService.getProductVOListByStatus(CommonStatusEnum.ENABLE.getStatus());
        return success(convertList(list, product -> new ErpProductRespVO().setId(product.getId())
                .setName(product.getName()).setMaterialCode(product.getMaterialCode()).setBarCode(product.getBarCode())
                .setCategoryId(product.getCategoryId()).setCategoryName(product.getCategoryName())
                .setUnitId(product.getUnitId()).setUnitName(product.getUnitName())
                .setQuantityPrecision(product.getQuantityPrecision())
                .setBatchControlFlag(product.getBatchControlFlag())
                .setInspectionRequiredFlag(product.getInspectionRequiredFlag())
                .setPurchasePrice(product.getPurchasePrice()).setSalePrice(product.getSalePrice()).setMinPrice(product.getMinPrice())));
    }

    @GetMapping("/simple-list-approved")
    @Operation(summary = "获得已审核物料精简列表（供 BOM 引用）")
    @PreAuthorize("@ss.hasPermission('erp:product:query')")
    public CommonResult<List<ErpProductRespVO>> getApprovedProductSimpleList() {
        return success(productService.getApprovedProductSimpleList());
    }

    @PostMapping("/submit")
    @Operation(summary = "提交物料审核")
    @PreAuthorize("@ss.hasPermission('erp:product:submit')")
    public CommonResult<String> submitProduct(@RequestParam("id") Long id) {
        return success(productBpmService.submitProduct(SecurityFrameworkUtils.getLoginUserId(), id));
    }

    @PostMapping("/cancel")
    @Operation(summary = "撤回物料审核")
    @PreAuthorize("@ss.hasPermission('erp:product:cancel')")
    public CommonResult<Boolean> cancelProduct(@RequestParam("id") Long id,
                                               @RequestParam(value = "reason", required = false) String reason) {
        productBpmService.cancelProductApproval(SecurityFrameworkUtils.getLoginUserId(), id, reason);
        return success(true);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出产品 Excel")
    @PreAuthorize("@ss.hasPermission('erp:product:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportProductExcel(@Valid ErpProductPageReqVO pageReqVO, HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        PageResult<ErpProductRespVO> pageResult = productService.getProductVOPage(pageReqVO);
        ExcelUtils.write(response, "产品.xls", "数据", ErpProductRespVO.class, pageResult.getList());
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得产品导入模板")
    @PreAuthorize("@ss.hasPermission('erp:product:import')")
    public void getProductImportTemplate(HttpServletResponse response) throws IOException {
        byte[] template = productImportService.downloadTemplate();
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=product-import-template.xlsx");
        response.getOutputStream().write(template);
        response.getOutputStream().flush();
    }

    @PostMapping("/import")
    @Operation(summary = "导入产品")
    @PreAuthorize("@ss.hasPermission('erp:product:import')")
    @ApiAccessLog(operateType = IMPORT)
    public CommonResult<ErpProductImportResultVO> importProduct(@RequestParam("file") MultipartFile file,
                                                                @RequestParam(value = "updateSupport", required = false) Boolean updateSupport) throws IOException {
        ErpProductImportResultVO result = productImportService.importProducts(file, updateSupport);
        return success(result);
    }

}
