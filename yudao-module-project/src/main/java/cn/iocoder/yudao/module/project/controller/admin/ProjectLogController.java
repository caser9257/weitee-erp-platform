package cn.iocoder.yudao.module.project.controller.admin;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.project.controller.admin.vo.log.ProjectLogPageReqVO;
import cn.iocoder.yudao.module.project.controller.admin.vo.log.ProjectLogRespVO;
import cn.iocoder.yudao.module.project.service.log.ProjectLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 项目操作日志")
@RestController
@RequestMapping("/project/log")
@Validated
public class ProjectLogController {

    @Resource
    private ProjectLogService projectLogService;

    @GetMapping("/page")
    @Operation(summary = "获得操作日志分页")
    @PreAuthorize("@ss.hasPermission('project:log:query')")
    public CommonResult<PageResult<ProjectLogRespVO>> getLogPage(@Valid ProjectLogPageReqVO pageReqVO) {
        return success(projectLogService.getLogPage(pageReqVO));
    }

}
