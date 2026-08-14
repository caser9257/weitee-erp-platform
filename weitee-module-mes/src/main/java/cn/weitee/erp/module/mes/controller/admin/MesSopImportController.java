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
import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.mes.enums.ErrorCodeConstants.MES_SOP_IMPORT_FILE_EMPTY;
import static cn.weitee.erp.module.mes.enums.ErrorCodeConstants.MES_SOP_IMPORT_FILE_TOO_LARGE;
import static cn.weitee.erp.module.mes.enums.ErrorCodeConstants.MES_SOP_IMPORT_FILE_TYPE_INVALID;

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
        validateImage(file);
        return success(mesSopImportService.ocrImport(file.getOriginalFilename(), file.getBytes()));
    }

    /**
     * 校验上传文件：仅图片类型、大小 ≤ 10MB。
     */
    private void validateImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw exception(MES_SOP_IMPORT_FILE_EMPTY);
        }
        if (file.getSize() > 10 * 1024 * 1024) {
            throw exception(MES_SOP_IMPORT_FILE_TOO_LARGE);
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw exception(MES_SOP_IMPORT_FILE_TYPE_INVALID);
        }
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
