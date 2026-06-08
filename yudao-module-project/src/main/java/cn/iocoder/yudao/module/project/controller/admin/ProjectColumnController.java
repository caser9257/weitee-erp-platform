package cn.iocoder.yudao.module.project.controller.admin;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.project.controller.admin.vo.column.ProjectColumnRespVO;
import cn.iocoder.yudao.module.project.controller.admin.vo.column.ProjectColumnSaveReqVO;
import cn.iocoder.yudao.module.project.convert.column.ProjectColumnConvert;
import cn.iocoder.yudao.module.project.dal.dataobject.column.ProjectColumnDO;
import cn.iocoder.yudao.module.project.service.column.ProjectColumnService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

@Tag(name = "管理后台 - 项目列表")
@RestController
@RequestMapping("/project/column")
@Validated
public class ProjectColumnController {

    @Resource
    private ProjectColumnService columnService;

    @PostMapping("/create")
    @Operation(summary = "创建列表")
    @PreAuthorize("@ss.hasPermission('project:column:create')")
    public CommonResult<Long> createColumn(@Valid @RequestBody ProjectColumnSaveReqVO createReqVO) {
        return success(columnService.createColumn(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新列表")
    @PreAuthorize("@ss.hasPermission('project:column:update')")
    public CommonResult<Boolean> updateColumn(@Valid @RequestBody ProjectColumnSaveReqVO updateReqVO) {
        columnService.updateColumn(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除列表")
    @Parameter(name = "id", description = "列表编号", required = true)
    @PreAuthorize("@ss.hasPermission('project:column:delete')")
    public CommonResult<Boolean> deleteColumn(@RequestParam("id") Long id) {
        columnService.deleteColumn(id);
        return success(true);
    }

    @GetMapping("/list")
    @Operation(summary = "获得列表")
    @Parameter(name = "projectId", description = "项目编号", required = true)
    @PreAuthorize("@ss.hasPermission('project:column:query')")
    public CommonResult<List<ProjectColumnRespVO>> getColumnList(@RequestParam("projectId") Long projectId) {
        List<ProjectColumnDO> list = columnService.getColumnList(projectId);
        return success(convertList(list, ProjectColumnConvert.INSTANCE::convert));
    }

    @PostMapping("/sort")
    @Operation(summary = "列表排序")
    @PreAuthorize("@ss.hasPermission('project:column:update')")
    public CommonResult<Boolean> sortColumns(@RequestParam("projectId") Long projectId,
                                             @RequestBody List<Long> columnIds) {
        columnService.sortColumns(projectId, columnIds);
        return success(true);
    }
}
