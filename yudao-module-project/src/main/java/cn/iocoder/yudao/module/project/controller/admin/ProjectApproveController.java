package cn.iocoder.yudao.module.project.controller.admin;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.project.controller.admin.vo.approve.ProjectApproveActionReqVO;
import cn.iocoder.yudao.module.project.controller.admin.vo.approve.ProjectApprovePageReqVO;
import cn.iocoder.yudao.module.project.controller.admin.vo.approve.ProjectApproveRespVO;
import cn.iocoder.yudao.module.project.controller.admin.vo.approve.ProjectApproveSaveReqVO;
import cn.iocoder.yudao.module.project.dal.dataobject.approve.ProjectApproveProcDO;
import cn.iocoder.yudao.module.project.service.approve.ProjectApproveService;
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

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 审批管理")
@RestController
@RequestMapping("/project/approve")
@Validated
public class ProjectApproveController {

    @Resource
    private ProjectApproveService approveService;

    @PostMapping("/create")
    @Operation(summary = "创建审批")
    @PreAuthorize("@ss.hasPermission('project:approve:create')")
    public CommonResult<Long> createApprove(@Valid @RequestBody ProjectApproveSaveReqVO createReqVO) {
        return success(approveService.createApprove(createReqVO));
    }

    @PostMapping("/approve")
    @Operation(summary = "审批操作")
    @PreAuthorize("@ss.hasPermission('project:approve:update')")
    public CommonResult<Boolean> approveAction(@Valid @RequestBody ProjectApproveActionReqVO actionReqVO) {
        approveService.approveAction(actionReqVO);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得审批详情")
    @Parameter(name = "id", description = "审批编号", required = true)
    @PreAuthorize("@ss.hasPermission('project:approve:query')")
    public CommonResult<ProjectApproveRespVO> getApprove(@RequestParam("id") Long id) {
        ProjectApproveProcDO approve = approveService.getApprove(id);
        if (approve == null) {
            return success(null);
        }
        return success(convertToRespVO(approve));
    }

    @GetMapping("/page")
    @Operation(summary = "获得审批分页")
    @PreAuthorize("@ss.hasPermission('project:approve:query')")
    public CommonResult<PageResult<ProjectApproveRespVO>> getApprovePage(@Valid ProjectApprovePageReqVO pageReqVO) {
        PageResult<ProjectApproveProcDO> pageResult = approveService.getApprovePage(pageReqVO);
        List<ProjectApproveRespVO> respList = pageResult.getList().stream()
                .map(this::convertToRespVO)
                .collect(Collectors.toList());
        return success(new PageResult<>(respList, pageResult.getTotal()));
    }

    private ProjectApproveRespVO convertToRespVO(ProjectApproveProcDO approve) {
        ProjectApproveRespVO resp = new ProjectApproveRespVO();
        resp.setId(approve.getId());
        resp.setName(approve.getName());
        resp.setStatus(approve.getStatus());
        resp.setCreateTime(approve.getCreateTime());
        return resp;
    }
}
