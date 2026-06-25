package cn.weitee.erp.module.project.service.report;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.project.controller.admin.vo.report.ProjectReportPageReqVO;
import cn.weitee.erp.module.project.controller.admin.vo.report.ProjectReportSaveReqVO;
import cn.weitee.erp.module.project.dal.dataobject.report.ProjectReportDO;

import jakarta.validation.Valid;

public interface ProjectReportService {
    Long createReport(@Valid ProjectReportSaveReqVO createReqVO);
    void updateReport(@Valid ProjectReportSaveReqVO updateReqVO);
    void deleteReport(Long id);
    ProjectReportDO getReport(Long id);
    PageResult<ProjectReportDO> getReportPage(ProjectReportPageReqVO pageReqVO);
    PageResult<ProjectReportDO> getReceiveReportPage(ProjectReportPageReqVO pageReqVO);
}
