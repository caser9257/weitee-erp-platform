package cn.iocoder.yudao.module.project.service.report;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.project.controller.admin.vo.report.ProjectReportPageReqVO;
import cn.iocoder.yudao.module.project.controller.admin.vo.report.ProjectReportSaveReqVO;
import cn.iocoder.yudao.module.project.dal.dataobject.report.ProjectReportDO;

import jakarta.validation.Valid;

public interface ProjectReportService {
    Long createReport(@Valid ProjectReportSaveReqVO createReqVO);
    void updateReport(@Valid ProjectReportSaveReqVO updateReqVO);
    void deleteReport(Long id);
    ProjectReportDO getReport(Long id);
    PageResult<ProjectReportDO> getReportPage(ProjectReportPageReqVO pageReqVO);
    PageResult<ProjectReportDO> getReceiveReportPage(ProjectReportPageReqVO pageReqVO);
}
