package cn.weitee.erp.module.mes.controller.admin;

import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.mes.controller.admin.vo.sopimport.MesSopImportConfirmReqVO;
import cn.weitee.erp.module.mes.controller.admin.vo.sopimport.MesSopImportRecordPageReqVO;
import cn.weitee.erp.module.mes.controller.admin.vo.sopimport.MesSopImportRecordRespVO;
import cn.weitee.erp.module.mes.service.MesSopImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.io.IOException;

import static cn.weitee.erp.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - MES SOP OCR 导入")
@RestController
@RequestMapping("/mes/sop-import")
@Validated
public class MesSopImportController {

    @Resource
    private MesSopImportService mesSopImportService;

    @PostMapping("/ocr")
    @Operation(summary = "OCR 识别 SOP 图片并生成草稿")
    @PreAuthorize("@ss.hasPermission('mes:sop:import')")
    public CommonResult<MesSopImportRecordRespVO> ocrImport(@RequestParam("file") MultipartFile file) throws IOException {
        return success(mesSopImportService.ocrImport(file.getOriginalFilename(), file.getBytes()));
    }

    @PutMapping("/confirm")
    @Operation(summary = "校对确认草稿转正式 SOP")
    @PreAuthorize("@ss.hasPermission('mes:sop:import')")
    public CommonResult<Long> confirmImport(@Valid @RequestBody MesSopImportConfirmReqVO reqVO) {
        return success(mesSopImportService.confirmImport(reqVO));
    }

    @GetMapping("/page")
    @Operation(summary = "获得导入记录分页")
    @PreAuthorize("@ss.hasPermission('mes:sop:query')")
    public CommonResult<PageResult<MesSopImportRecordRespVO>> getImportPage(@Valid MesSopImportRecordPageReqVO pageReqVO) {
        return success(mesSopImportService.getImportPage(pageReqVO));
    }

}
