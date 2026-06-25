package cn.weitee.erp.module.project.service.statistics;

import cn.weitee.erp.module.project.controller.admin.vo.statistics.ProjectStatisticsOverviewRespVO;

public interface ProjectStatisticsService {

    /**
     * 获取项目统计概览
     */
    ProjectStatisticsOverviewRespVO getOverview(Long projectId);

}
