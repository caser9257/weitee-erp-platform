package cn.weitee.erp.module.infra.controller.admin.file;

import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.infra.controller.admin.file.vo.permission.FilePermissionRespVO;
import cn.weitee.erp.module.infra.controller.admin.file.vo.permission.FilePermissionSaveReqVO;
import cn.weitee.erp.module.infra.dal.dataobject.file.FilePermissionDO;
import cn.weitee.erp.module.infra.service.file.FilePermissionService;
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

@Tag(name = "管理后台 - 文件权限管理")
@RestController
@RequestMapping("/infra/file-permission")
@Validated
public class FilePermissionController {

    @Resource
    private FilePermissionService filePermissionService;

    @PostMapping("/grant")
    @Operation(summary = "授权文件权限")
    @PreAuthorize("@ss.hasPermission('infra:file:update')")
    public CommonResult<Long> grantPermission(@Valid @RequestBody FilePermissionSaveReqVO reqVO) {
        return success(filePermissionService.grantPermission(reqVO));
    }

    @DeleteMapping("/revoke")
    @Operation(summary = "撤销文件权限")
    @PreAuthorize("@ss.hasPermission('infra:file:update')")
    public CommonResult<Boolean> revokePermission(
            @RequestParam("fileId") Long fileId,
            @RequestParam("grantType") String grantType,
            @RequestParam("grantTargetId") Long grantTargetId) {
        filePermissionService.revokePermission(fileId, grantType, grantTargetId);
        return success(true);
    }

    @GetMapping("/list")
    @Operation(summary = "获取文件的权限列表")
    @PreAuthorize("@ss.hasPermission('infra:file:query')")
    public CommonResult<List<FilePermissionRespVO>> getFilePermissions(
            @RequestParam("fileId") Long fileId) {
        List<FilePermissionDO> list = filePermissionService.getFilePermissions(fileId);
        return success(BeanUtils.toBean(list, FilePermissionRespVO.class));
    }

    @GetMapping("/check")
    @Operation(summary = "检查用户是否有指定权限")
    @PreAuthorize("@ss.hasPermission('infra:file:query')")
    public CommonResult<Boolean> checkPermission(
            @RequestParam("fileId") Long fileId,
            @RequestParam("permission") String permission) {
        // TODO: 从SecurityContext获取当前用户ID
        Long userId = 1L; // 临时硬编码
        return success(filePermissionService.hasPermission(fileId, userId, permission));
    }

}
