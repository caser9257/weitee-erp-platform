package cn.iocoder.yudao.module.erp.controller.admin.sale;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.ContractImportResultVO;
import cn.iocoder.yudao.module.erp.service.sale.ErpContractImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 合同导入 Controller
 *
 * @author system
 */
@Tag(name = "ERP - 合同导入")
@RestController
@RequestMapping("/erp/contract-import")
@Validated
@Slf4j
public class ErpContractImportController {

    @Resource
    private ErpContractImportService erpContractImportService;

    @GetMapping("/template")
    @PreAuthorize("@ss.hasPermission('erp:contract-import:template')")
    @Operation(summary = "下载导入模板")
    public void downloadTemplate(HttpServletResponse response) {
        try {
            byte[] template = erpContractImportService.downloadTemplate();
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=contract-import-template.xlsx");
            response.getOutputStream().write(template);
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("下载模板失败", e);
        }
    }

    @PostMapping("/import")
    @PreAuthorize("@ss.hasPermission('erp:contract-import:import')")
    @Operation(summary = "导入合同")
    public CommonResult<ContractImportResultVO> importContracts(@RequestParam("file") MultipartFile file) {
        ContractImportResultVO result = erpContractImportService.importContracts(file);
        return success(result);
    }

}
