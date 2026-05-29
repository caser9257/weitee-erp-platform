package cn.iocoder.yudao.module.project.controller.admin;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.project.controller.admin.vo.project.ProjectPageReqVO;
import cn.iocoder.yudao.module.project.controller.admin.vo.project.ProjectRespVO;
import cn.iocoder.yudao.module.project.controller.admin.vo.project.ProjectSaveReqVO;
import cn.iocoder.yudao.module.project.convert.project.ProjectConvert;
import cn.iocoder.yudao.module.project.dal.dataobject.project.ProjectDO;
import cn.iocoder.yudao.module.project.dal.dataobject.project.ProjectUserDO;
import cn.iocoder.yudao.module.project.service.project.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

@Tag(name = "管理后台 - 项目管理")
@RestController
@RequestMapping("/project")
@Validated
public class ProjectController {

    @Resource
    private ProjectService projectService;

    @PostMapping("/create")
    @Operation(summary = "创建项目")
    @PreAuthorize("@ss.hasPermission('project:project:create')")
    public CommonResult<Long> createProject(@Valid @RequestBody ProjectSaveReqVO createReqVO) {
        return success(projectService.createProject(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新项目")
    @PreAuthorize("@ss.hasPermission('project:project:update')")
    public CommonResult<Boolean> updateProject(@Valid @RequestBody ProjectSaveReqVO updateReqVO) {
        projectService.updateProject(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除项目")
    @Parameter(name = "id", description = "项目编号", required = true)
    @PreAuthorize("@ss.hasPermission('project:project:delete')")
    public CommonResult<Boolean> deleteProject(@RequestParam("id") Long id) {
        projectService.deleteProject(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得项目详情")
    @Parameter(name = "id", description = "项目编号", required = true)
    @PreAuthorize("@ss.hasPermission('project:project:query')")
    public CommonResult<ProjectRespVO> getProject(@RequestParam("id") Long id) {
        ProjectDO project = projectService.getProject(id);
        return success(ProjectConvert.INSTANCE.convert(project));
    }

    @GetMapping("/page")
    @Operation(summary = "获得项目分页")
    @PreAuthorize("@ss.hasPermission('project:project:query')")
    public CommonResult<PageResult<ProjectRespVO>> getProjectPage(@Valid ProjectPageReqVO pageReqVO) {
        PageResult<ProjectDO> pageResult = projectService.getProjectPage(pageReqVO);
        return success(new PageResult<>(
                convertList(pageResult.getList(), ProjectConvert.INSTANCE::convert),
                pageResult.getTotal()));
    }

    @PostMapping("/archive")
    @Operation(summary = "归档/取消归档项目")
    @PreAuthorize("@ss.hasPermission('project:project:update')")
    public CommonResult<Boolean> archiveProject(@RequestParam("id") Long id,
                                                @RequestParam("archive") boolean archive) {
        projectService.archiveProject(id, archive);
        return success(true);
    }

    @PostMapping("/member/add")
    @Operation(summary = "添加项目成员")
    @PreAuthorize("@ss.hasPermission('project:project:update')")
    public CommonResult<Boolean> addMember(@RequestParam("projectId") Long projectId,
                                           @RequestParam("userId") Long userId,
                                           @RequestParam(value = "owner", defaultValue = "false") boolean owner) {
        projectService.addMember(projectId, userId, owner);
        return success(true);
    }

    @PostMapping("/member/remove")
    @Operation(summary = "移除项目成员")
    @PreAuthorize("@ss.hasPermission('project:project:update')")
    public CommonResult<Boolean> removeMember(@RequestParam("projectId") Long projectId,
                                              @RequestParam("userId") Long userId) {
        projectService.removeMember(projectId, userId);
        return success(true);
    }

    @GetMapping("/member/list")
    @Operation(summary = "获得项目成员列表")
    @PreAuthorize("@ss.hasPermission('project:project:query')")
    public CommonResult<List<ProjectUserDO>> getMembers(@RequestParam("projectId") Long projectId) {
        return success(projectService.getMembers(projectId));
    }
}
