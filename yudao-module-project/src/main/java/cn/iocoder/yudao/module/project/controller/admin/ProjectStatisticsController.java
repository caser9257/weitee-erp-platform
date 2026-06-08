package cn.iocoder.yudao.module.project.controller.admin;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.project.controller.admin.vo.statistics.ProjectStatisticsOverviewRespVO;
import cn.iocoder.yudao.module.project.service.statistics.ProjectStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 项目统计")
@RestController
@RequestMapping("/project/statistics")
@Validated
public class ProjectStatisticsController {

    @Resource
    private ProjectStatisticsService statisticsService;

    @GetMapping("/overview")
    @Operation(summary = "获取项目统计概览")
    @Parameter(name = "projectId", description = "项目编号", required = true)
    @PreAuthorize("@ss.hasPermission('project:statistics:query')")
    public CommonResult<ProjectStatisticsOverviewRespVO> getOverview(@RequestParam("projectId") Long projectId) {
        return success(statisticsService.getOverview(projectId));
    }

}
