package cn.iocoder.yudao.module.project.service.statistics.impl;

import cn.iocoder.yudao.module.project.controller.admin.vo.statistics.ProjectStatisticsOverviewRespVO;
import cn.iocoder.yudao.module.project.dal.dataobject.task.ProjectTaskDO;
import cn.iocoder.yudao.module.project.dal.dataobject.project.ProjectUserDO;
import cn.iocoder.yudao.module.project.dal.mysql.task.ProjectTaskMapper;
import cn.iocoder.yudao.module.project.dal.mysql.project.ProjectUserMapper;
import cn.iocoder.yudao.module.project.service.statistics.ProjectStatisticsService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class ProjectStatisticsServiceImpl implements ProjectStatisticsService {

    @Resource
    private ProjectTaskMapper projectTaskMapper;
    @Resource
    private ProjectUserMapper projectUserMapper;

    @Override
    public ProjectStatisticsOverviewRespVO getOverview(Long projectId) {
        ProjectStatisticsOverviewRespVO respVO = new ProjectStatisticsOverviewRespVO();

        // 1. 查询总任务数
        Long totalTasks = projectTaskMapper.selectCount(
                new LambdaQueryWrapper<ProjectTaskDO>()
                        .eq(ProjectTaskDO::getProjectId, projectId)
                        .eq(ProjectTaskDO::getDeleted, false));
        respVO.setTotalTasks(totalTasks.intValue());

        // 2. 查询已完成任务数
        Long completedTasks = projectTaskMapper.selectCount(
                new LambdaQueryWrapper<ProjectTaskDO>()
                        .eq(ProjectTaskDO::getProjectId, projectId)
                        .eq(ProjectTaskDO::getDeleted, false)
                        .isNotNull(ProjectTaskDO::getCompleteAt));
        respVO.setCompletedTasks(completedTasks.intValue());

        // 3. 计算进行中任务数
        respVO.setInProgressTasks(totalTasks.intValue() - completedTasks.intValue());

        // 4. 查询逾期任务数
        Long overdueTasks = projectTaskMapper.selectCount(
                new LambdaQueryWrapper<ProjectTaskDO>()
                        .eq(ProjectTaskDO::getProjectId, projectId)
                        .eq(ProjectTaskDO::getDeleted, false)
                        .isNull(ProjectTaskDO::getCompleteAt)
                        .isNotNull(ProjectTaskDO::getEndAt)
                        .lt(ProjectTaskDO::getEndAt, LocalDateTime.now()));
        respVO.setOverdueTasks(overdueTasks.intValue());

        // 5. 计算完成率
        if (totalTasks > 0) {
            BigDecimal completionRate = BigDecimal.valueOf(completedTasks)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalTasks), 2, RoundingMode.HALF_UP);
            respVO.setCompletionRate(completionRate);
        } else {
            respVO.setCompletionRate(BigDecimal.ZERO);
        }

        // 6. 查询成员数
        Long totalMembers = projectUserMapper.selectCount(
                new LambdaQueryWrapper<ProjectUserDO>()
                        .eq(ProjectUserDO::getProjectId, projectId));
        respVO.setTotalMembers(totalMembers.intValue());

        return respVO;
    }

}
