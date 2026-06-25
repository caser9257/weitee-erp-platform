package cn.weitee.erp.module.project.controller.admin;

import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.module.project.controller.admin.vo.file.ProjectFileRespVO;
import cn.weitee.erp.module.project.controller.admin.vo.file.ProjectFileSaveReqVO;
import cn.weitee.erp.module.project.dal.dataobject.file.ProjectFileDO;
import cn.weitee.erp.module.project.service.file.ProjectFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

import static cn.weitee.erp.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 文件管理")
@RestController
@RequestMapping("/project/file")
@Validated
public class ProjectFileController {

    @Resource
    private ProjectFileService fileService;

    @PostMapping("/upload")
    @Operation(summary = "上传文件")
    @PreAuthorize("@ss.hasPermission('project:file:create')")
    public CommonResult<Long> uploadFile(@Valid @RequestBody ProjectFileSaveReqVO createReqVO) {
        return success(fileService.uploadFile(createReqVO));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除文件")
    @Parameter(name = "id", description = "文件编号", required = true)
    @PreAuthorize("@ss.hasPermission('project:file:delete')")
    public CommonResult<Boolean> deleteFile(@RequestParam("id") Long id) {
        fileService.deleteFile(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得文件详情")
    @Parameter(name = "id", description = "文件编号", required = true)
    @PreAuthorize("@ss.hasPermission('project:file:query')")
    public CommonResult<ProjectFileRespVO> getFile(@RequestParam("id") Long id) {
        ProjectFileDO file = fileService.getFile(id);
        if (file == null) {
            return success(null);
        }
        return success(convertToRespVO(file));
    }

    @GetMapping("/list")
    @Operation(summary = "获得文件列表")
    @PreAuthorize("@ss.hasPermission('project:file:query')")
    public CommonResult<List<ProjectFileRespVO>> getFileList() {
        List<ProjectFileDO> files = fileService.getFileList();
        return success(files.stream().map(this::convertToRespVO).collect(Collectors.toList()));
    }

    private ProjectFileRespVO convertToRespVO(ProjectFileDO file) {
        ProjectFileRespVO resp = new ProjectFileRespVO();
        resp.setId(file.getId());
        resp.setName(file.getName());
        resp.setExt(file.getExt());
        resp.setSize(file.getSize());
        resp.setType(file.getType());
        resp.setUrl(file.getUrl());
        resp.setCreateTime(file.getCreateTime());
        return resp;
    }
}
