package cn.weitee.erp.module.project.service.task;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.project.controller.admin.vo.task.ProjectTaskPageReqVO;
import cn.weitee.erp.module.project.controller.admin.vo.task.ProjectTaskSaveReqVO;
import cn.weitee.erp.module.project.dal.dataobject.task.ProjectTaskDO;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

public interface ProjectTaskService {
    Long createTask(@Valid ProjectTaskSaveReqVO createReqVO);
    void updateTask(@Valid ProjectTaskSaveReqVO updateReqVO);
    void deleteTask(Long id);
    ProjectTaskDO getTask(Long id);
    PageResult<ProjectTaskDO> getTaskPage(ProjectTaskPageReqVO pageReqVO);
    void completeTask(Long id, boolean complete);
    void sortTask(Long columnId, List<Long> taskIds);
    void archiveTask(Long id, boolean archive);
    void moveTask(Long taskId, Long targetColumnId);

    /**
     * 按日期范围查询任务（用于日历视图）
     */
    List<ProjectTaskDO> getTaskListByDate(Long projectId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查询项目下所有任务（用于甘特图）
     */
    List<ProjectTaskDO> getTaskListByProjectId(Long projectId);
}
