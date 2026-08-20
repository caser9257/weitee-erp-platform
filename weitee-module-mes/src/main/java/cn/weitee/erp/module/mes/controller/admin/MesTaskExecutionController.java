package cn.weitee.erp.module.mes.controller.admin;

import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.module.mes.controller.admin.vo.taskexecution.MesTaskExecutionReportReqVO;
import cn.weitee.erp.module.mes.controller.admin.vo.taskexecution.MesTaskExecutionRespVO;
import cn.weitee.erp.module.mes.service.MesTaskExecutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static cn.weitee.erp.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - MES 现场执行")
@RestController
@RequestMapping("/mes/task-execution")
@Validated
public class MesTaskExecutionController {

    @Resource
    private MesTaskExecutionService mesTaskExecutionService;

    @GetMapping("/task")
    @Operation(summary = "查询现场执行任务")
    @PreAuthorize("@ss.hasPermission('mes:task-execution:query')")
    public CommonResult<MesTaskExecutionRespVO> getTask(@RequestParam("taskNo") String taskNo) {
        return success(mesTaskExecutionService.getTask(taskNo));
    }

    @PutMapping("/start")
    @Operation(summary = "现场开工")
    @PreAuthorize("@ss.hasPermission('mes:task-execution:update')")
    public CommonResult<Boolean> start(@RequestParam("taskNo") String taskNo) {
        mesTaskExecutionService.start(taskNo);
        return success(true);
    }

    @PutMapping("/pause")
    @Operation(summary = "现场暂停")
    @PreAuthorize("@ss.hasPermission('mes:task-execution:update')")
    public CommonResult<Boolean> pause(@RequestParam("taskNo") String taskNo) {
        mesTaskExecutionService.pause(taskNo);
        return success(true);
    }

    @PutMapping("/resume")
    @Operation(summary = "现场恢复")
    @PreAuthorize("@ss.hasPermission('mes:task-execution:update')")
    public CommonResult<Boolean> resume(@RequestParam("taskNo") String taskNo) {
        mesTaskExecutionService.resume(taskNo);
        return success(true);
    }

    @PostMapping("/report")
    @Operation(summary = "现场报工")
    @PreAuthorize("@ss.hasPermission('mes:task-execution:report')")
    public CommonResult<Long> report(@Valid @RequestBody MesTaskExecutionReportReqVO reqVO) {
        return success(mesTaskExecutionService.report(reqVO));
    }

    @PutMapping("/finish")
    @Operation(summary = "现场完工")
    @PreAuthorize("@ss.hasPermission('mes:task-execution:update')")
    public CommonResult<Boolean> finish(@RequestParam("taskNo") String taskNo) {
        mesTaskExecutionService.finish(taskNo);
        return success(true);
    }
}
