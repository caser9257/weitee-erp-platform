package cn.weitee.erp.module.infra.controller.admin.file;

import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.infra.controller.admin.file.vo.version.FileVersionRespVO;
import cn.weitee.erp.module.infra.controller.admin.file.vo.version.FileVersionRollbackReqVO;
import cn.weitee.erp.module.infra.dal.dataobject.file.FileVersionDO;
import cn.weitee.erp.module.infra.service.file.FileVersionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;

import static cn.weitee.erp.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 文件版本")
@RestController
@RequestMapping("/infra/file-version")
@Validated
public class FileVersionController {

    @Resource
    private FileVersionService fileVersionService;

    @GetMapping("/list")
    @Operation(summary = "获得文件版本列表")
    @Parameter(name = "fileId", description = "文件ID", required = true)
    @PreAuthorize("@ss.hasPermission('infra:file:query')")
    public CommonResult<List<FileVersionRespVO>> getFileVersionList(@RequestParam("fileId") Long fileId) {
        List<FileVersionDO> list = fileVersionService.getFileVersions(fileId);
        return success(BeanUtils.toBean(list, FileVersionRespVO.class));
    }

    @GetMapping("/latest")
    @Operation(summary = "获得文件最新版本号")
    @Parameter(name = "fileId", description = "文件ID", required = true)
    @PreAuthorize("@ss.hasPermission('infra:file:query')")
    public CommonResult<Integer> getLatestVersion(@RequestParam("fileId") Long fileId) {
        return success(fileVersionService.getLatestVersion(fileId));
    }

    @PostMapping("/rollback")
    @Operation(summary = "回溯文件到指定版本")
    @PreAuthorize("@ss.hasPermission('infra:file:update')")
    public CommonResult<Boolean> rollbackFileVersion(@Valid @RequestBody FileVersionRollbackReqVO reqVO) {
        fileVersionService.rollbackToVersion(reqVO.getFileId(), reqVO.getVersion());
        return success(true);
    }

}