package cn.weitee.erp.module.erp.controller.admin.product;

import cn.weitee.erp.framework.apilog.core.annotation.ApiAccessLog;
import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.framework.common.pojo.PageParam;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.framework.excel.core.util.ExcelUtils;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpProductApprovalViewRespVO;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpProductBatchApprovalViewRespVO;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpProductImportResultVO;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpProductPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpProductSimpleRespVO;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ProductSaveReqVO;
import cn.weitee.erp.framework.security.core.util.SecurityFrameworkUtils;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductDO;
import cn.weitee.erp.module.erp.service.product.ErpProductBpmService;
import cn.weitee.erp.module.erp.service.product.ErpProductImportService;
import cn.weitee.erp.module.erp.service.product.ErpProductPendingChangeService;
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
import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCT_CADENCE_ACCESS_DENIED;

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
    @Resource
    private ErpProductPendingChangeService productPendingChangeService;

    @PostMapping("/create")
    @Operation(summary = "创建产品")
    @PreAuthorize("@ss.hasPermission('erp:product:create')")
    public CommonResult<Long> createProduct(@Valid @RequestBody ProductSaveReqVO createReqVO) {
        validateCadenceFieldAccess(createReqVO);
        return success(productService.createProduct(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新产品", description = "已审批物料自动进入修改审批（暂存模式），未生效物料直接保存")
    @PreAuthorize("@ss.hasPermission('erp:product:update')")
    public CommonResult<Boolean> updateProduct(@Valid @RequestBody ProductSaveReqVO updateReqVO) {
        validateCadenceFieldAccess(updateReqVO);
        // 统一入口：内部按物料审核状态路由（APPROVE → 修改审批；草稿/驳回/失败 → 直接落库）
        productBpmService.submitProductUpdate(SecurityFrameworkUtils.getLoginUserId(), updateReqVO);
        return success(true);
    }

    @GetMapping("/approval-view")
    @Operation(summary = "获得物料修改审批视图（主表现值 vs 暂存目标值 + 字段级 diff）")
    @Parameter(name = "id", description = "物料编号", required = true, example = "15672")
    @PreAuthorize("@ss.hasPermission('erp:product:approval-view')")
    public CommonResult<ErpProductApprovalViewRespVO> getApprovalView(@RequestParam("id") Long id) {
        ErpProductApprovalViewRespVO view = productPendingChangeService.getApprovalView(id);
        if (!canAccessCadence()) {
            ErpProductCadenceAccessHelper.maskCadenceFields(view);
        }
        return success(view);
    }

    @GetMapping("/batch-approval-view")
    @Operation(summary = "获得物料批量修改审批视图（批次内全部物料的字段级 diff）")
    @Parameter(name = "batchId", description = "批次编号", required = true, example = "1948236571289630720")
    @PreAuthorize("@ss.hasPermission('erp:product:batch-approval-view')")
    public CommonResult<ErpProductBatchApprovalViewRespVO> getBatchApprovalView(
            @RequestParam("batchId") Long batchId) {
        return success(productPendingChangeService.getBatchApprovalView(batchId));
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
        ErpProductRespVO product = productService.getProductVOList(List.of(id)).stream().findFirst().orElse(null);
        if (!canAccessCadence()) {
            ErpProductCadenceAccessHelper.maskCadenceFields(product);
        }
        return success(product);
    }

    @GetMapping("/page")
    @Operation(summary = "获得产品分页")
    @PreAuthorize("@ss.hasPermission('erp:product:query')")
    public CommonResult<PageResult<ErpProductRespVO>> getProductPage(@Valid ErpProductPageReqVO pageReqVO) {
        PageResult<ErpProductRespVO> pageResult = productService.getProductVOPage(pageReqVO);
        maskCadenceFields(pageResult.getList());
        return success(pageResult);
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得产品精简列表", description = "只包含启用状态的产品，主要用于下拉选项")
    public CommonResult<List<ErpProductRespVO>> getProductSimpleList(
            @RequestParam(value = "name", required = false) String name) {
        List<ErpProductRespVO> list = productService.getProductVOListByStatus(CommonStatusEnum.ENABLE.getStatus(), name);
        return success(convertList(list, product -> new ErpProductRespVO().setId(product.getId())
                .setName(product.getName()).setMaterialCode(product.getMaterialCode()).setBarCode(product.getBarCode())
                .setCategoryId(product.getCategoryId()).setCategoryName(product.getCategoryName())
                .setUnitId(product.getUnitId()).setUnitName(product.getUnitName())
                .setQuantityPrecision(product.getQuantityPrecision())
                .setStandard(product.getStandard())
                .setBatchControlFlag(product.getBatchControlFlag())
                .setInspectionRequiredFlag(product.getInspectionRequiredFlag())
                .setPurchasePrice(product.getPurchasePrice()).setSalePrice(product.getSalePrice()).setMinPrice(product.getMinPrice())));
    }

    @GetMapping("/simple-list-approved")
    @Operation(summary = "获得已审核物料精简列表（供 BOM 引用）", description = "白名单字段精简 VO，万级物料下避免超大响应体")
    @PreAuthorize("@ss.hasPermission('erp:product:query')")
    public CommonResult<List<ErpProductSimpleRespVO>> getApprovedProductSimpleList(
            @RequestParam(value = "name", required = false) String name) {
        return success(productService.getApprovedProductSimpleList(name));
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

    @PostMapping("/change-request")
    @Operation(summary = "两段式变更阶段一：发起变更申请", description = "生效物料变更必须先申请审批，通过后解锁编辑权限")
    @PreAuthorize("@ss.hasPermission('erp:product:update')")
    public CommonResult<Boolean> submitChangeRequest(@RequestParam("id") Long id,
                                                     @RequestParam(value = "reason", required = false) String reason) {
        productBpmService.submitChangeRequest(SecurityFrameworkUtils.getLoginUserId(), id, reason);
        return success(true);
    }

    @PostMapping("/change-confirm")
    @Operation(summary = "两段式变更阶段二：提交变更完成确认", description = "编辑完成后提交审批，负责人确认变更正确后生效")
    @PreAuthorize("@ss.hasPermission('erp:product:update')")
    public CommonResult<Boolean> submitChangeConfirm(@RequestParam("id") Long id,
                                                     @RequestParam(value = "reason", required = false) String reason) {
        productBpmService.submitChangeConfirm(SecurityFrameworkUtils.getLoginUserId(), id, reason);
        return success(true);
    }

    @PostMapping("/obsolete-request")
    @Operation(summary = "发起废除申请", description = "废除=销号，编码释放可复用；审批通过即生效")
    @PreAuthorize("@ss.hasPermission('erp:product:update')")
    public CommonResult<Boolean> submitObsoleteRequest(@RequestParam("id") Long id,
                                                       @RequestParam(value = "reason", required = false) String reason) {
        productBpmService.submitObsoleteRequest(SecurityFrameworkUtils.getLoginUserId(), id, reason);
        return success(true);
    }

    @PostMapping("/status-change")
    @Operation(summary = "发起启停审批", description = "审批通过后切换启停状态；审批期间状态保持不变")
    @PreAuthorize("@ss.hasPermission('erp:product:update')")
    public CommonResult<Boolean> submitStatusChange(@RequestParam("id") Long id,
                                                    @RequestParam("targetStatus") Integer targetStatus,
                                                    @RequestParam(value = "reason", required = false) String reason) {
        productBpmService.submitStatusChange(SecurityFrameworkUtils.getLoginUserId(), id, targetStatus, reason);
        return success(true);
    }

    @PostMapping("/cancel-two-stage")
    @Operation(summary = "撤回两段式审批（变更/废除 阶段一/二 通用）")
    @PreAuthorize("@ss.hasPermission('erp:product:cancel')")
    public CommonResult<Boolean> cancelTwoStageApproval(@RequestParam("id") Long id,
                                                        @RequestParam(value = "reason", required = false) String reason) {
        productBpmService.cancelTwoStageApproval(SecurityFrameworkUtils.getLoginUserId(), id, reason);
        return success(true);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出产品 Excel")
    @PreAuthorize("@ss.hasPermission('erp:product:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportProductExcel(@Valid ErpProductPageReqVO pageReqVO, HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        PageResult<ErpProductRespVO> pageResult = productService.getProductVOPage(pageReqVO);
        maskCadenceFields(pageResult.getList());
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

    private boolean canAccessCadence() {
        return ErpProductCadenceAccessHelper.canAccess(SecurityFrameworkUtils.getLoginUserDeptId());
    }

    private void validateCadenceFieldAccess(ProductSaveReqVO reqVO) {
        if (!canAccessCadence() && ErpProductCadenceAccessHelper.hasCadenceFields(reqVO)) {
            throw exception(PRODUCT_CADENCE_ACCESS_DENIED);
        }
    }

    private void maskCadenceFields(List<ErpProductRespVO> products) {
        if (!canAccessCadence()) {
            products.forEach(ErpProductCadenceAccessHelper::maskCadenceFields);
        }
    }

}
