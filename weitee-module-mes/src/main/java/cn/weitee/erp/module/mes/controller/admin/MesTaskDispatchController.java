package cn.weitee.erp.module.mes.controller.admin;

import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.mes.controller.admin.vo.taskdispatch.MesTaskDispatchPageReqVO;
import cn.weitee.erp.module.mes.controller.admin.vo.taskdispatch.MesTaskDispatchRespVO;
import cn.weitee.erp.module.mes.controller.admin.vo.taskdispatch.MesTaskDispatchSaveReqVO;
import cn.weitee.erp.module.mes.service.MesTaskDispatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;

import static cn.weitee.erp.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - MES 派工管理")
@RestController
@RequestMapping("/mes/task-dispatch")
@Validated
public class MesTaskDispatchController {

    @Resource
    private MesTaskDispatchService mesTaskDispatchService;

    @GetMapping("/page")
    @Operation(summary = "获得派工工作台分页")
    @PreAuthorize("@ss.hasPermission('mes:task-dispatch:query')")
    public CommonResult<PageResult<MesTaskDispatchRespVO>> getWorkbenchPage(
            @Valid MesTaskDispatchPageReqVO pageReqVO) {
        return success(mesTaskDispatchService.getWorkbenchPage(pageReqVO));
    }

    @GetMapping("/history")
    @Operation(summary = "获得工序任务派工历史")
    @PreAuthorize("@ss.hasPermission('mes:task-dispatch:query')")
    public CommonResult<List<MesTaskDispatchRespVO>> getHistory(@RequestParam("taskId") Long taskId) {
        return success(mesTaskDispatchService.getHistory(taskId));
    }

    @PostMapping("/assign")
    @Operation(summary = "派工")
    @PreAuthorize("@ss.hasPermission('mes:task-dispatch:update')")
    public CommonResult<Long> assign(@Valid @RequestBody MesTaskDispatchSaveReqVO reqVO) {
        return success(mesTaskDispatchService.assign(reqVO));
    }

    @PutMapping("/reassign")
    @Operation(summary = "改派")
    @PreAuthorize("@ss.hasPermission('mes:task-dispatch:update')")
    public CommonResult<Long> reassign(@Valid @RequestBody MesTaskDispatchSaveReqVO reqVO) {
        return success(mesTaskDispatchService.reassign(reqVO));
    }

    @PutMapping("/revoke")
    @Operation(summary = "撤销派工")
    @PreAuthorize("@ss.hasPermission('mes:task-dispatch:update')")
    public CommonResult<Boolean> revoke(@RequestParam("taskId") Long taskId) {
        mesTaskDispatchService.revoke(taskId);
        return success(true);
    }
}
