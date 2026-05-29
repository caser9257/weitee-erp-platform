package cn.iocoder.yudao.module.project.service.task.impl;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.project.controller.admin.vo.log.ProjectLogCreateReqVO;
import cn.iocoder.yudao.module.project.controller.admin.vo.task.ProjectTaskPageReqVO;
import cn.iocoder.yudao.module.project.controller.admin.vo.task.ProjectTaskSaveReqVO;
import cn.iocoder.yudao.module.project.dal.dataobject.task.*;
import cn.iocoder.yudao.module.project.dal.mysql.task.*;
import cn.iocoder.yudao.module.project.service.log.ProjectLogService;
import cn.iocoder.yudao.module.project.service.task.ProjectTaskService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.project.enums.ErrorCodeConstants.*;

@Service
@Validated
public class ProjectTaskServiceImpl implements ProjectTaskService {

    @Resource
    private ProjectTaskMapper projectTaskMapper;
    @Resource
    private ProjectTaskUserMapper projectTaskUserMapper;
    @Resource
    private ProjectTaskContentMapper projectTaskContentMapper;
    @Resource
    private ProjectTaskTagMapper projectTaskTagMapper;
    @Resource
    private ProjectLogService projectLogService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTask(@Valid ProjectTaskSaveReqVO createReqVO) {
        // 1. 校验任务数量限制
        Long uncompletedCount = projectTaskMapper.selectCount(new LambdaQueryWrapper<ProjectTaskDO>()
                .eq(ProjectTaskDO::getProjectId, createReqVO.getProjectId())
                .eq(ProjectTaskDO::getParentId, 0L)
                .isNull(ProjectTaskDO::getCompleteAt)
                .isNull(ProjectTaskDO::getArchivedAt));
        if (uncompletedCount >= 2000) {
            throw exception(TASK_PROJECT_MAX_COUNT_ERROR);
        }

        Long columnCount = projectTaskMapper.selectCount(new LambdaQueryWrapper<ProjectTaskDO>()
                .eq(ProjectTaskDO::getColumnId, createReqVO.getColumnId())
                .isNull(ProjectTaskDO::getCompleteAt)
                .isNull(ProjectTaskDO::getArchivedAt));
        if (columnCount >= 500) {
            throw exception(TASK_COLUMN_MAX_COUNT_ERROR);
        }

        if (createReqVO.getParentId() != null && createReqVO.getParentId() > 0) {
            Long subCount = projectTaskMapper.selectCount(new LambdaQueryWrapper<ProjectTaskDO>()
                    .eq(ProjectTaskDO::getParentId, createReqVO.getParentId())
                    .isNull(ProjectTaskDO::getCompleteAt)
                    .isNull(ProjectTaskDO::getArchivedAt));
            if (subCount >= 50) {
                throw exception(TASK_SUBTASK_MAX_COUNT_ERROR);
            }
        }

        // 2. 创建任务
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        ProjectTaskDO task = ProjectTaskDO.builder()
                .parentId(createReqVO.getParentId() != null ? createReqVO.getParentId() : 0L)
                .projectId(createReqVO.getProjectId())
                .columnId(createReqVO.getColumnId())
                .name(createReqVO.getName())
                .description(createReqVO.getDescription())
                .color(createReqVO.getColor())
                .startAt(createReqVO.getStartAt())
                .endAt(createReqVO.getEndAt())
                .visibility(createReqVO.getVisibility() != null ? createReqVO.getVisibility() : 1)
                .priorityLevel(createReqVO.getPriorityLevel())
                .priorityName(createReqVO.getPriorityName())
                .priorityColor(createReqVO.getPriorityColor())
                .sort(getNextSort(createReqVO.getColumnId()))
                .build();
        projectTaskMapper.insert(task);

        // 3. 添加负责人
        if (createReqVO.getOwnerUserIds() != null) {
            for (Long ownerId : createReqVO.getOwnerUserIds()) {
                if (ownerId == null || ownerId == 0) continue;
                ProjectTaskUserDO taskUser = ProjectTaskUserDO.builder()
                        .projectId(task.getProjectId())
                        .taskId(task.getId())
                        .taskPid(task.getParentId() > 0 ? task.getParentId() : task.getId())
                        .userId(ownerId)
                        .owner(true)
                        .build();
                projectTaskUserMapper.insert(taskUser);
            }
        }

        // 4. 添加协助人
        if (createReqVO.getAssistUserIds() != null) {
            for (Long assistId : createReqVO.getAssistUserIds()) {
                if (assistId == null || assistId == 0) continue;
                ProjectTaskUserDO taskUser = ProjectTaskUserDO.builder()
                        .projectId(task.getProjectId())
                        .taskId(task.getId())
                        .taskPid(task.getParentId() > 0 ? task.getParentId() : task.getId())
                        .userId(assistId)
                        .owner(false)
                        .build();
                projectTaskUserMapper.insert(taskUser);
            }
        }

        // 5. 添加标签
        if (createReqVO.getTaskTags() != null) {
            for (ProjectTaskSaveReqVO.TaskTagVO tag : createReqVO.getTaskTags()) {
                ProjectTaskTagDO taskTag = ProjectTaskTagDO.builder()
                        .projectId(task.getProjectId())
                        .taskId(task.getId())
                        .name(tag.getName())
                        .color(tag.getColor())
                        .build();
                projectTaskTagMapper.insert(taskTag);
            }
        }

        // 6. 添加内容
        if (createReqVO.getContent() != null && !createReqVO.getContent().isEmpty()) {
            ProjectTaskContentDO content = ProjectTaskContentDO.builder()
                    .projectId(task.getProjectId())
                    .taskId(task.getId())
                    .userId(userId)
                    .description(createReqVO.getContent().length() > 100 ?
                            createReqVO.getContent().substring(0, 100) : createReqVO.getContent())
                    .content(createReqVO.getContent())
                    .build();
            projectTaskContentMapper.insert(content);
        }

        // 7. 记录日志
        recordLog(task.getProjectId(), task.getColumnId(), task.getId(), "创建任务");

        return task.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTask(@Valid ProjectTaskSaveReqVO updateReqVO) {
        ProjectTaskDO task = projectTaskMapper.selectById(updateReqVO.getId());
        if (task == null) {
            throw exception(TASK_NOT_FOUND);
        }

        // 更新基本字段
        if (updateReqVO.getName() != null) {
            task.setName(updateReqVO.getName());
        }
        if (updateReqVO.getDescription() != null) {
            task.setDescription(updateReqVO.getDescription());
        }
        if (updateReqVO.getColor() != null) {
            task.setColor(updateReqVO.getColor());
        }
        if (updateReqVO.getStartAt() != null) {
            task.setStartAt(updateReqVO.getStartAt());
        }
        if (updateReqVO.getEndAt() != null) {
            task.setEndAt(updateReqVO.getEndAt());
        }
        if (updateReqVO.getVisibility() != null) {
            task.setVisibility(updateReqVO.getVisibility());
        }
        if (updateReqVO.getPriorityLevel() != null) {
            task.setPriorityLevel(updateReqVO.getPriorityLevel());
            task.setPriorityName(updateReqVO.getPriorityName());
            task.setPriorityColor(updateReqVO.getPriorityColor());
        }
        projectTaskMapper.updateById(task);

        // 更新负责人
        if (updateReqVO.getOwnerUserIds() != null) {
            projectTaskUserMapper.delete(new LambdaQueryWrapper<ProjectTaskUserDO>()
                    .eq(ProjectTaskUserDO::getTaskId, task.getId())
                    .eq(ProjectTaskUserDO::getOwner, true));
            for (Long ownerId : updateReqVO.getOwnerUserIds()) {
                if (ownerId == null || ownerId == 0) continue;
                ProjectTaskUserDO taskUser = ProjectTaskUserDO.builder()
                        .projectId(task.getProjectId())
                        .taskId(task.getId())
                        .taskPid(task.getParentId() > 0 ? task.getParentId() : task.getId())
                        .userId(ownerId)
                        .owner(true)
                        .build();
                projectTaskUserMapper.insert(taskUser);
            }
        }

        // 更新标签
        if (updateReqVO.getTaskTags() != null) {
            projectTaskTagMapper.delete(new LambdaQueryWrapper<ProjectTaskTagDO>()
                    .eq(ProjectTaskTagDO::getTaskId, task.getId()));
            for (ProjectTaskSaveReqVO.TaskTagVO tag : updateReqVO.getTaskTags()) {
                ProjectTaskTagDO taskTag = ProjectTaskTagDO.builder()
                        .projectId(task.getProjectId())
                        .taskId(task.getId())
                        .name(tag.getName())
                        .color(tag.getColor())
                        .build();
                projectTaskTagMapper.insert(taskTag);
            }
        }

        recordLog(task.getProjectId(), task.getColumnId(), task.getId(), "更新任务");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTask(Long id) {
        ProjectTaskDO task = projectTaskMapper.selectById(id);
        if (task == null) {
            throw exception(TASK_NOT_FOUND);
        }

        // 删除子任务
        List<ProjectTaskDO> subTasks = projectTaskMapper.selectList(ProjectTaskDO::getParentId, id);
        for (ProjectTaskDO subTask : subTasks) {
            projectTaskMapper.deleteById(subTask.getId());
        }

        // 删除任务
        projectTaskMapper.deleteById(id);

        // 删除关联数据
        projectTaskUserMapper.delete(new LambdaQueryWrapper<ProjectTaskUserDO>()
                .eq(ProjectTaskUserDO::getTaskId, id));
        projectTaskTagMapper.delete(new LambdaQueryWrapper<ProjectTaskTagDO>()
                .eq(ProjectTaskTagDO::getTaskId, id));

        recordLog(task.getProjectId(), task.getColumnId(), id, "删除任务");
    }

    @Override
    public ProjectTaskDO getTask(Long id) {
        return projectTaskMapper.selectById(id);
    }

    @Override
    public PageResult<ProjectTaskDO> getTaskPage(ProjectTaskPageReqVO pageReqVO) {
        LambdaQueryWrapper<ProjectTaskDO> query = new LambdaQueryWrapper<ProjectTaskDO>()
                .eq(pageReqVO.getProjectId() != null, ProjectTaskDO::getProjectId, pageReqVO.getProjectId())
                .eq(pageReqVO.getColumnId() != null, ProjectTaskDO::getColumnId, pageReqVO.getColumnId())
                .eq(pageReqVO.getParentId() != null, ProjectTaskDO::getParentId, pageReqVO.getParentId())
                .like(pageReqVO.getName() != null, ProjectTaskDO::getName, pageReqVO.getName())
                .eq(pageReqVO.getFlowItemId() != null, ProjectTaskDO::getFlowItemId, pageReqVO.getFlowItemId())
                .orderByAsc(ProjectTaskDO::getSort);
        if (pageReqVO.getCompleted() != null) {
            if (pageReqVO.getCompleted()) {
                query.isNotNull(ProjectTaskDO::getCompleteAt);
            } else {
                query.isNull(ProjectTaskDO::getCompleteAt);
            }
        }
        if (pageReqVO.getArchived() != null) {
            if (pageReqVO.getArchived()) {
                query.isNotNull(ProjectTaskDO::getArchivedAt);
            } else {
                query.isNull(ProjectTaskDO::getArchivedAt);
            }
        }
        return projectTaskMapper.selectPage(pageReqVO, query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeTask(Long id, boolean complete) {
        ProjectTaskDO task = projectTaskMapper.selectById(id);
        if (task == null) {
            throw exception(TASK_NOT_FOUND);
        }

        if (complete) {
            if (task.getCompleteAt() != null) {
                throw exception(TASK_ALREADY_COMPLETED);
            }
            task.setCompleteAt(LocalDateTime.now());
        } else {
            if (task.getCompleteAt() == null) {
                throw exception(TASK_NOT_COMPLETED);
            }
            task.setCompleteAt(null);
        }
        projectTaskMapper.updateById(task);

        recordLog(task.getProjectId(), task.getColumnId(), id,
                complete ? "完成任务" : "重新打开任务");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sortTask(Long columnId, List<Long> taskIds) {
        for (int i = 0; i < taskIds.size(); i++) {
            ProjectTaskDO task = new ProjectTaskDO();
            task.setId(taskIds.get(i));
            task.setSort(i);
            projectTaskMapper.updateById(task);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void archiveTask(Long id, boolean archive) {
        ProjectTaskDO task = projectTaskMapper.selectById(id);
        if (task == null) {
            throw exception(TASK_NOT_FOUND);
        }

        Long userId = SecurityFrameworkUtils.getLoginUserId();
        if (archive) {
            task.setArchivedAt(LocalDateTime.now());
            task.setArchivedUserId(userId);
        } else {
            task.setArchivedAt(null);
            task.setArchivedUserId(userId);
        }
        projectTaskMapper.updateById(task);

        recordLog(task.getProjectId(), task.getColumnId(), id,
                archive ? "归档任务" : "取消归档任务");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void moveTask(Long taskId, Long targetColumnId) {
        ProjectTaskDO task = projectTaskMapper.selectById(taskId);
        if (task == null) {
            throw exception(TASK_NOT_FOUND);
        }

        Long oldColumnId = task.getColumnId();
        task.setColumnId(targetColumnId);
        task.setSort(getNextSort(targetColumnId));
        projectTaskMapper.updateById(task);

        recordLog(task.getProjectId(), oldColumnId, taskId, "移动任务");
    }

    private int getNextSort(Long columnId) {
        LambdaQueryWrapper<ProjectTaskDO> query = new LambdaQueryWrapper<ProjectTaskDO>()
                .eq(ProjectTaskDO::getColumnId, columnId)
                .orderByDesc(ProjectTaskDO::getSort)
                .last("LIMIT 1");
        ProjectTaskDO maxSortTask = projectTaskMapper.selectOne(query);
        return maxSortTask != null && maxSortTask.getSort() != null ? maxSortTask.getSort() + 1 : 0;
    }

    private void recordLog(Long projectId, Long columnId, Long taskId, String detail) {
        ProjectLogCreateReqVO reqVO = new ProjectLogCreateReqVO();
        reqVO.setProjectId(projectId);
        reqVO.setColumnId(columnId);
        reqVO.setTaskId(taskId);
        reqVO.setDetail(detail);
        projectLogService.createLog(reqVO);
    }

    @Override
    public List<ProjectTaskDO> getTaskListByDate(Long projectId, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<ProjectTaskDO> queryWrapper = new LambdaQueryWrapper<ProjectTaskDO>()
                .eq(ProjectTaskDO::getProjectId, projectId)
                .eq(ProjectTaskDO::getDeleted, false)
                .and(w -> w
                        .between(ProjectTaskDO::getStartAt, startTime, endTime)
                        .or()
                        .between(ProjectTaskDO::getEndAt, startTime, endTime)
                )
                .orderByAsc(ProjectTaskDO::getStartAt);
        return projectTaskMapper.selectList(queryWrapper);
    }

    @Override
    public List<ProjectTaskDO> getTaskListByProjectId(Long projectId) {
        LambdaQueryWrapper<ProjectTaskDO> queryWrapper = new LambdaQueryWrapper<ProjectTaskDO>()
                .eq(ProjectTaskDO::getProjectId, projectId)
                .eq(ProjectTaskDO::getDeleted, false)
                .isNull(ProjectTaskDO::getArchivedAt)
                .orderByAsc(ProjectTaskDO::getParentId)
                .orderByAsc(ProjectTaskDO::getSort);
        return projectTaskMapper.selectList(queryWrapper);
    }
}
