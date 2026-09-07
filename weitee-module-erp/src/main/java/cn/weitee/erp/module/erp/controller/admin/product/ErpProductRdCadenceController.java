package cn.weitee.erp.module.erp.controller.admin.product;

import cn.weitee.erp.framework.apilog.core.annotation.ApiAccessLog;
import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.framework.excel.core.util.ExcelUtils;
import cn.weitee.erp.framework.security.core.util.SecurityFrameworkUtils;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpProductImportResultVO;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpRdCadenceExportVO;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpRdCadencePageReqVO;
import cn.weitee.erp.module.erp.service.product.ErpProductRdCadenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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

@Tag(name = "管理后台 - ERP 研发物料(Cadence)")
@RestController
@RequestMapping("/erp/product/rd-cadence")
@Validated
public class ErpProductRdCadenceController {

    @Resource
    private ErpProductRdCadenceService rdCadenceService;

    @GetMapping("/page")
    @Operation(summary = "研发物料(Cadence)分页")
    @PreAuthorize("@ss.hasPermission('erp:product:rd-cadence:query')")
    public CommonResult<PageResult<ErpProductRespVO>> getRdCadencePage(@Valid ErpRdCadencePageReqVO reqVO) {
        return success(rdCadenceService.getRdCadencePage(reqVO, SecurityFrameworkUtils.getLoginUserDeptId()));
    }

    @GetMapping("/export")
    @Operation(summary = "导出研发物料(Cadence)数据")
    @PreAuthorize("@ss.hasPermission('erp:product:rd-cadence:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportRdCadence(@Valid ErpRdCadencePageReqVO reqVO, HttpServletResponse response) throws IOException {
        List<ErpProductRespVO> list = rdCadenceService.exportRdCadence(reqVO, SecurityFrameworkUtils.getLoginUserDeptId());
        List<ErpRdCadenceExportVO> exportList = BeanUtils.toBean(list, ErpRdCadenceExportVO.class);
        ExcelUtils.write(response, "Cadence物料.xls", "数据", ErpRdCadenceExportVO.class, exportList);
    }

    @GetMapping("/template")
    @Operation(summary = "下载 Cadence 导入模板（英文表头）")
    @PreAuthorize("@ss.hasPermission('erp:product:rd-cadence:import')")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        byte[] template = rdCadenceService.downloadTemplate();
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=cadence-import-template.xlsx");
        response.getOutputStream().write(template);
        response.getOutputStream().flush();
    }

    @PostMapping("/import/precheck")
    @Operation(summary = "Cadence 数据导入预检查（不落库）")
    @PreAuthorize("@ss.hasPermission('erp:product:rd-cadence:import')")
    public CommonResult<ErpProductImportResultVO> precheckImport(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "markAllAsPcb", defaultValue = "false") boolean markAllAsPcb) {
        return success(rdCadenceService.precheckImport(file, SecurityFrameworkUtils.getLoginUserDeptId(), markAllAsPcb));
    }

    @PostMapping("/import")
    @Operation(summary = "Cadence 数据导入（已审核物料进入修改审批）")
    @PreAuthorize("@ss.hasPermission('erp:product:rd-cadence:import')")
    @ApiAccessLog(operateType = IMPORT)
    public CommonResult<ErpProductImportResultVO> importCadence(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "markAllAsPcb", defaultValue = "false") boolean markAllAsPcb) {
        return success(rdCadenceService.importCadence(file, SecurityFrameworkUtils.getLoginUserId(),
                SecurityFrameworkUtils.getLoginUserDeptId(), markAllAsPcb));
    }

}
