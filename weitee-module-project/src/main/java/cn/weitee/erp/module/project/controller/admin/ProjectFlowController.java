package cn.weitee.erp.module.project.controller.admin;

import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.module.project.controller.admin.vo.flow.ProjectFlowRespVO;
import cn.weitee.erp.module.project.controller.admin.vo.flow.ProjectFlowSaveReqVO;
import cn.weitee.erp.module.project.convert.flow.ProjectFlowConvert;
import cn.weitee.erp.module.project.dal.dataobject.flow.ProjectFlowDO;
import cn.weitee.erp.module.project.dal.dataobject.flow.ProjectFlowItemDO;
import cn.weitee.erp.module.project.service.flow.ProjectFlowService;
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

@Tag(name = "管理后台 - 工作流管理")
@RestController
@RequestMapping("/project/flow")
@Validated
public class ProjectFlowController {

    @Resource
    private ProjectFlowService flowService;

    @PostMapping("/save")
    @Operation(summary = "保存工作流")
    @PreAuthorize("@ss.hasPermission('project:flow:update')")
    public CommonResult<Long> saveFlow(@Valid @RequestBody ProjectFlowSaveReqVO saveReqVO) {
        return success(flowService.saveFlow(saveReqVO));
    }

    @GetMapping("/get")
    @Operation(summary = "获得工作流")
    @Parameter(name = "projectId", description = "项目编号", required = true)
    @PreAuthorize("@ss.hasPermission('project:flow:query')")
    public CommonResult<ProjectFlowRespVO> getFlow(@RequestParam("projectId") Long projectId) {
        ProjectFlowDO flow = flowService.getFlow(projectId);
        if (flow == null) {
            return success(null);
        }

        List<ProjectFlowItemDO> items = flowService.getFlowItems(flow.getId());
        ProjectFlowRespVO resp = ProjectFlowConvert.INSTANCE.convert(flow);
        resp.setItems(ProjectFlowConvert.INSTANCE.convertItemList(items));
        return success(resp);
    }
}
