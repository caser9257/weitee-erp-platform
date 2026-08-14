package cn.weitee.erp.module.mes.controller.admin;

import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.mes.controller.admin.vo.worktask.MesWorkTaskPageReqVO;
import cn.weitee.erp.module.mes.controller.admin.vo.worktask.MesWorkTaskUpdatePlanTimeReqVO;
import cn.weitee.erp.module.mes.dal.dataobject.MesWorkTaskDO;
import cn.weitee.erp.module.mes.service.MesWorkTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;

import static cn.weitee.erp.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - MES 工序任务")
@RestController
@RequestMapping("/mes/work-task")
@Validated
public class MesWorkTaskController {

    @Resource
    private MesWorkTaskService mesWorkTaskService;

    @GetMapping("/page")
    @Operation(summary = "获得工序任务分页")
    @PreAuthorize("@ss.hasPermission('mes:work-task:query')")
    public CommonResult<PageResult<MesWorkTaskDO>> getTaskPage(@Valid MesWorkTaskPageReqVO pageReqVO) {
        return success(mesWorkTaskService.getTaskPage(pageReqVO));
    }

    @GetMapping("/list-by-order")
    @Operation(summary = "按工单获得工序任务列表")
    @PreAuthorize("@ss.hasPermission('mes:work-task:query')")
    public CommonResult<List<MesWorkTaskDO>> getTaskListByOrderId(@RequestParam("productionOrderId") Long productionOrderId) {
        return success(mesWorkTaskService.getTaskListByOrderId(productionOrderId));
    }

    @PutMapping("/update-plan-time")
    @Operation(summary = "调整任务计划时间")
    @PreAuthorize("@ss.hasPermission('mes:work-task:update')")
    public CommonResult<Boolean> updatePlanTime(@Valid @RequestBody MesWorkTaskUpdatePlanTimeReqVO reqVO) {
        mesWorkTaskService.updatePlanTime(reqVO);
        return success(true);
    }

    @PutMapping("/cancel")
    @Operation(summary = "取消任务")
    @PreAuthorize("@ss.hasPermission('mes:work-task:update')")
    public CommonResult<Boolean> cancelTask(@RequestParam("id") Long id) {
        mesWorkTaskService.cancelTask(id);
        return success(true);
    }

    @PutMapping("/regenerate")
    @Operation(summary = "按工单重新生成缺失任务")
    @PreAuthorize("@ss.hasPermission('mes:work-task:update')")
    public CommonResult<Boolean> regenerateTasks(@RequestParam("productionOrderId") Long productionOrderId) {
        mesWorkTaskService.regenerateTasks(productionOrderId);
        return success(true);
    }

    @PutMapping("/update-priority")
    @Operation(summary = "更新任务优先级")
    @PreAuthorize("@ss.hasPermission('mes:work-task:update')")
    public CommonResult<Boolean> updatePriority(@RequestParam("id") Long id,
                                               @RequestParam("priority") Integer priority) {
        mesWorkTaskService.updatePriority(id, priority);
        return success(true);
    }

    @PutMapping("/schedule")
    @Operation(summary = "自动排程（不传工单则排全部待排程任务）")
    @PreAuthorize("@ss.hasPermission('mes:work-task:update')")
    public CommonResult<Integer> schedule(@RequestParam(value = "productionOrderId", required = false) Long productionOrderId) {
        return success(mesWorkTaskService.schedule(productionOrderId));
    }

    @PutMapping("/clear-and-reschedule")
    @Operation(summary = "清空并重排某工单任务")
    @PreAuthorize("@ss.hasPermission('mes:work-task:update')")
    public CommonResult<Integer> clearAndReschedule(@RequestParam("productionOrderId") Long productionOrderId) {
        return success(mesWorkTaskService.clearAndReschedule(productionOrderId));
    }

    @GetMapping("/conflicts")
    @Operation(summary = "工作中心任务冲突检测")
    @PreAuthorize("@ss.hasPermission('mes:work-task:query')")
    public CommonResult<List<cn.weitee.erp.module.mes.service.scheduling.MesSchedulingService.ConflictPair>> detectConflicts(
            @RequestParam("workCenterId") Long workCenterId) {
        return success(mesWorkTaskService.detectConflicts(workCenterId));
    }

    @GetMapping("/gantt")
    @Operation(summary = "获得甘特任务块")
    @PreAuthorize("@ss.hasPermission('mes:work-task:query')")
    public CommonResult<List<MesWorkTaskDO>> getGanttList(
            @RequestParam("workCenterId") Long workCenterId,
            @RequestParam("startTime") @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") java.time.LocalDateTime startTime,
            @RequestParam("endTime") @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") java.time.LocalDateTime endTime) {
        return success(mesWorkTaskService.getGanttList(workCenterId, startTime, endTime));
    }

}
