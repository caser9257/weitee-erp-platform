package cn.weitee.erp.module.project.controller.admin;

import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.project.controller.admin.vo.report.ProjectReportPageReqVO;
import cn.weitee.erp.module.project.controller.admin.vo.report.ProjectReportRespVO;
import cn.weitee.erp.module.project.controller.admin.vo.report.ProjectReportSaveReqVO;
import cn.weitee.erp.module.project.dal.dataobject.report.ProjectReportDO;
import cn.weitee.erp.module.project.service.report.ProjectReportService;
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

import static cn.weitee.erp.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 日报管理")
@RestController
@RequestMapping("/project/report")
@Validated
public class ProjectReportController {

    @Resource
    private ProjectReportService reportService;

    @PostMapping("/create")
    @Operation(summary = "创建日报")
    @PreAuthorize("@ss.hasPermission('project:report:create')")
    public CommonResult<Long> createReport(@Valid @RequestBody ProjectReportSaveReqVO createReqVO) {
        return success(reportService.createReport(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新日报")
    @PreAuthorize("@ss.hasPermission('project:report:update')")
    public CommonResult<Boolean> updateReport(@Valid @RequestBody ProjectReportSaveReqVO updateReqVO) {
        reportService.updateReport(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除日报")
    @Parameter(name = "id", description = "日报编号", required = true)
    @PreAuthorize("@ss.hasPermission('project:report:delete')")
    public CommonResult<Boolean> deleteReport(@RequestParam("id") Long id) {
        reportService.deleteReport(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得日报详情")
    @Parameter(name = "id", description = "日报编号", required = true)
    @PreAuthorize("@ss.hasPermission('project:report:query')")
    public CommonResult<ProjectReportRespVO> getReport(@RequestParam("id") Long id) {
        ProjectReportDO report = reportService.getReport(id);
        if (report == null) {
            return success(null);
        }
        return success(convertToRespVO(report));
    }

    @GetMapping("/page")
    @Operation(summary = "获得日报分页")
    @PreAuthorize("@ss.hasPermission('project:report:query')")
    public CommonResult<PageResult<ProjectReportRespVO>> getReportPage(@Valid ProjectReportPageReqVO pageReqVO) {
        PageResult<ProjectReportDO> pageResult = reportService.getReportPage(pageReqVO);
        List<ProjectReportRespVO> respList = pageResult.getList().stream()
                .map(this::convertToRespVO)
                .collect(Collectors.toList());
        return success(new PageResult<>(respList, pageResult.getTotal()));
    }

    @GetMapping("/receive-page")
    @Operation(summary = "获得接收日报分页")
    @PreAuthorize("@ss.hasPermission('project:report:query')")
    public CommonResult<PageResult<ProjectReportRespVO>> getReceiveReportPage(@Valid ProjectReportPageReqVO pageReqVO) {
        PageResult<ProjectReportDO> pageResult = reportService.getReceiveReportPage(pageReqVO);
        List<ProjectReportRespVO> respList = pageResult.getList().stream()
                .map(this::convertToRespVO)
                .collect(Collectors.toList());
        return success(new PageResult<>(respList, pageResult.getTotal()));
    }

    private ProjectReportRespVO convertToRespVO(ProjectReportDO report) {
        ProjectReportRespVO resp = new ProjectReportRespVO();
        resp.setId(report.getId());
        resp.setUserId(report.getUserId());
        resp.setType(report.getType());
        resp.setContent(report.getContent());
        resp.setSign(report.getSign());
        resp.setCreateTime(report.getCreateTime());
        return resp;
    }
}
