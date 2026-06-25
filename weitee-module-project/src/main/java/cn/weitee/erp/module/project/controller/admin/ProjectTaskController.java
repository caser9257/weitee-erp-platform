package cn.weitee.erp.module.project.controller.admin;

import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.project.controller.admin.vo.task.ProjectTaskGanttRespVO;
import cn.weitee.erp.module.project.controller.admin.vo.task.ProjectTaskPageReqVO;
import cn.weitee.erp.module.project.controller.admin.vo.task.ProjectTaskRespVO;
import cn.weitee.erp.module.project.controller.admin.vo.task.ProjectTaskSaveReqVO;
import cn.weitee.erp.module.project.dal.dataobject.task.ProjectTaskDO;
import cn.weitee.erp.module.project.dal.dataobject.task.ProjectTaskTagDO;
import cn.weitee.erp.module.project.dal.dataobject.task.ProjectTaskUserDO;
import cn.weitee.erp.module.project.dal.mysql.task.ProjectTaskTagMapper;
import cn.weitee.erp.module.project.dal.mysql.task.ProjectTaskUserMapper;
import cn.weitee.erp.module.project.service.task.ProjectTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static cn.weitee.erp.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 任务管理")
@RestController
@RequestMapping("/project/task")
@Validated
public class ProjectTaskController {

    @Resource
    private ProjectTaskService taskService;
    @Resource
    private ProjectTaskUserMapper projectTaskUserMapper;
    @Resource
    private ProjectTaskTagMapper projectTaskTagMapper;

    @PostMapping("/create")
    @Operation(summary = "创建任务")
    @PreAuthorize("@ss.hasPermission('project:task:create')")
    public CommonResult<Long> createTask(@Valid @RequestBody ProjectTaskSaveReqVO createReqVO) {
        return success(taskService.createTask(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新任务")
    @PreAuthorize("@ss.hasPermission('project:task:update')")
    public CommonResult<Boolean> updateTask(@Valid @RequestBody ProjectTaskSaveReqVO updateReqVO) {
        taskService.updateTask(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除任务")
    @Parameter(name = "id", description = "任务编号", required = true)
    @PreAuthorize("@ss.hasPermission('project:task:delete')")
    public CommonResult<Boolean> deleteTask(@RequestParam("id") Long id) {
        taskService.deleteTask(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得任务详情")
    @Parameter(name = "id", description = "任务编号", required = true)
    @PreAuthorize("@ss.hasPermission('project:task:query')")
    public CommonResult<ProjectTaskRespVO> getTask(@RequestParam("id") Long id) {
        ProjectTaskDO task = taskService.getTask(id);
        if (task == null) {
            return success(null);
        }
        ProjectTaskRespVO resp = convertToRespVO(task);
        return success(resp);
    }

    @GetMapping("/page")
    @Operation(summary = "获得任务分页")
    @PreAuthorize("@ss.hasPermission('project:task:query')")
    public CommonResult<PageResult<ProjectTaskRespVO>> getTaskPage(@Valid ProjectTaskPageReqVO pageReqVO) {
        PageResult<ProjectTaskDO> pageResult = taskService.getTaskPage(pageReqVO);
        List<ProjectTaskRespVO> respList = pageResult.getList().stream()
                .map(this::convertToRespVO)
                .collect(Collectors.toList());
        return success(new PageResult<>(respList, pageResult.getTotal()));
    }

    @PostMapping("/complete")
    @Operation(summary = "完成/取消完成任务")
    @PreAuthorize("@ss.hasPermission('project:task:update')")
    public CommonResult<Boolean> completeTask(@RequestParam("id") Long id,
                                              @RequestParam("complete") boolean complete) {
        taskService.completeTask(id, complete);
        return success(true);
    }

    @PostMapping("/sort")
    @Operation(summary = "任务排序")
    @PreAuthorize("@ss.hasPermission('project:task:update')")
    public CommonResult<Boolean> sortTask(@RequestParam("columnId") Long columnId,
                                          @RequestBody List<Long> taskIds) {
        taskService.sortTask(columnId, taskIds);
        return success(true);
    }

    @PostMapping("/archive")
    @Operation(summary = "归档/取消归档任务")
    @PreAuthorize("@ss.hasPermission('project:task:update')")
    public CommonResult<Boolean> archiveTask(@RequestParam("id") Long id,
                                             @RequestParam("archive") boolean archive) {
        taskService.archiveTask(id, archive);
        return success(true);
    }

    @PostMapping("/move")
    @Operation(summary = "移动任务")
    @PreAuthorize("@ss.hasPermission('project:task:update')")
    public CommonResult<Boolean> moveTask(@RequestParam("taskId") Long taskId,
                                          @RequestParam("targetColumnId") Long targetColumnId) {
        taskService.moveTask(taskId, targetColumnId);
        return success(true);
    }

    private ProjectTaskRespVO convertToRespVO(ProjectTaskDO task) {
        ProjectTaskRespVO resp = new ProjectTaskRespVO();
        resp.setId(task.getId());
        resp.setParentId(task.getParentId());
        resp.setProjectId(task.getProjectId());
        resp.setColumnId(task.getColumnId());
        resp.setFlowItemId(task.getFlowItemId());
        resp.setFlowItemName(task.getFlowItemName());
        resp.setName(task.getName());
        resp.setDescription(task.getDescription());
        resp.setColor(task.getColor());
        resp.setStartAt(task.getStartAt());
        resp.setEndAt(task.getEndAt());
        resp.setCompleteAt(task.getCompleteAt());
        resp.setArchivedAt(task.getArchivedAt());
        resp.setVisibility(task.getVisibility());
        resp.setPriorityLevel(task.getPriorityLevel());
        resp.setPriorityName(task.getPriorityName());
        resp.setPriorityColor(task.getPriorityColor());
        resp.setSort(task.getSort());
        resp.setCreateTime(task.getCreateTime());
        resp.setCreator(task.getCreator());

        // 负责人
        List<ProjectTaskUserDO> users = projectTaskUserMapper.selectList(
                ProjectTaskUserDO::getTaskId, task.getId());
        resp.setOwners(users.stream()
                .filter(ProjectTaskUserDO::getOwner)
                .map(u -> {
                    ProjectTaskRespVO.TaskUserVO vo = new ProjectTaskRespVO.TaskUserVO();
                    vo.setUserId(u.getUserId());
                    vo.setOwner(true);
                    return vo;
                }).collect(Collectors.toList()));
        resp.setAssistants(users.stream()
                .filter(u -> !u.getOwner())
                .map(u -> {
                    ProjectTaskRespVO.TaskUserVO vo = new ProjectTaskRespVO.TaskUserVO();
                    vo.setUserId(u.getUserId());
                    vo.setOwner(false);
                    return vo;
                }).collect(Collectors.toList()));

        // 标签
        List<ProjectTaskTagDO> tags = projectTaskTagMapper.selectList(
                ProjectTaskTagDO::getTaskId, task.getId());
        resp.setTaskTags(tags.stream().map(t -> {
            ProjectTaskRespVO.TaskTagVO tagVO = new ProjectTaskRespVO.TaskTagVO();
            tagVO.setName(t.getName());
            tagVO.setColor(t.getColor());
            return tagVO;
        }).collect(Collectors.toList()));

        return resp;
    }

    @GetMapping("/list-by-date")
    @Operation(summary = "按日期范围查询任务（日历视图）")
    @PreAuthorize("@ss.hasPermission('project:task:query')")
    public CommonResult<List<ProjectTaskRespVO>> getTaskListByDate(
            @RequestParam("projectId") Long projectId,
            @RequestParam("startTime") LocalDateTime startTime,
            @RequestParam("endTime") LocalDateTime endTime) {
        List<ProjectTaskDO> tasks = taskService.getTaskListByDate(projectId, startTime, endTime);
        List<ProjectTaskRespVO> respList = tasks.stream()
                .map(this::convertToRespVO)
                .collect(Collectors.toList());
        return success(respList);
    }

    @GetMapping("/gantt")
    @Operation(summary = "获取甘特图数据")
    @PreAuthorize("@ss.hasPermission('project:task:query')")
    public CommonResult<List<ProjectTaskGanttRespVO>> getTaskGanttData(
            @RequestParam("projectId") Long projectId) {
        List<ProjectTaskDO> tasks = taskService.getTaskListByProjectId(projectId);
        List<ProjectTaskGanttRespVO> respList = tasks.stream()
                .map(this::convertToGanttRespVO)
                .collect(Collectors.toList());
        return success(respList);
    }

    private ProjectTaskGanttRespVO convertToGanttRespVO(ProjectTaskDO task) {
        ProjectTaskGanttRespVO resp = new ProjectTaskGanttRespVO();
        resp.setId(task.getId());
        resp.setParentId(task.getParentId());
        resp.setProjectId(task.getProjectId());
        resp.setName(task.getName());
        resp.setStartAt(task.getStartAt());
        resp.setEndAt(task.getEndAt());
        resp.setCompleteAt(task.getCompleteAt());
        resp.setPriorityColor(task.getPriorityColor());

        // 计算进度
        if (task.getCompleteAt() != null) {
            resp.setProgress(100);
        } else if (task.getStartAt() != null && task.getEndAt() != null) {
            long total = task.getEndAt().toLocalDate().toEpochDay() - task.getStartAt().toLocalDate().toEpochDay();
            long elapsed = java.time.LocalDate.now().toEpochDay() - task.getStartAt().toLocalDate().toEpochDay();
            if (total > 0) {
                resp.setProgress(Math.min(100, Math.max(0, (int) (elapsed * 100 / total))));
            } else {
                resp.setProgress(0);
            }
        } else {
            resp.setProgress(0);
        }

        return resp;
    }
}
