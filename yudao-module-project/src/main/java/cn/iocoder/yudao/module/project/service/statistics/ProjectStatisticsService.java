package cn.iocoder.yudao.module.project.service.statistics;

import cn.iocoder.yudao.module.project.controller.admin.vo.statistics.ProjectStatisticsOverviewRespVO;

public interface ProjectStatisticsService {

    /**
     * 获取项目统计概览
     */
    ProjectStatisticsOverviewRespVO getOverview(Long projectId);

}
