package cn.iocoder.yudao.module.project.service.project.impl;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.project.controller.admin.vo.log.ProjectLogCreateReqVO;
import cn.iocoder.yudao.module.project.controller.admin.vo.project.ProjectPageReqVO;
import cn.iocoder.yudao.module.project.controller.admin.vo.project.ProjectSaveReqVO;
import cn.iocoder.yudao.module.project.convert.project.ProjectConvert;
import cn.iocoder.yudao.module.project.dal.dataobject.project.ProjectDO;
import cn.iocoder.yudao.module.project.dal.dataobject.project.ProjectUserDO;
import cn.iocoder.yudao.module.project.dal.mysql.project.ProjectMapper;
import cn.iocoder.yudao.module.project.dal.mysql.project.ProjectUserMapper;
import cn.iocoder.yudao.module.project.service.log.ProjectLogService;
import cn.iocoder.yudao.module.project.service.project.ProjectService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.project.enums.ErrorCodeConstants.*;

@Service
@Validated
public class ProjectServiceImpl implements ProjectService {

    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private ProjectUserMapper projectUserMapper;
    @Resource
    private ProjectLogService projectLogService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createProject(@Valid ProjectSaveReqVO createReqVO) {
        // 1. 校验名称
        if (createReqVO.getName().length() < 2 || createReqVO.getName().length() > 32) {
            throw exception(PROJECT_NAME_LENGTH_ERROR);
        }

        // 2. 个人项目唯一性校验
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        if (Boolean.TRUE.equals(createReqVO.getPersonal())) {
            Long count = projectMapper.selectCount(new LambdaQueryWrapper<ProjectDO>()
                    .eq(ProjectDO::getOwnerUserId, userId)
                    .eq(ProjectDO::getPersonal, true));
            if (count > 0) {
                throw exception(PROJECT_PERSONAL_ALREADY_EXISTS);
            }
        }

        // 3. 创建项目
        ProjectDO project = ProjectConvert.INSTANCE.convert(createReqVO);
        project.setOwnerUserId(userId);
        projectMapper.insert(project);

        // 4. 添加创建人为负责人
        ProjectUserDO projectUser = ProjectUserDO.builder()
                .projectId(project.getId())
                .userId(userId)
                .owner(true)
                .sort(0)
                .build();
        projectUserMapper.insert(projectUser);

        // 5. 记录日志
        recordLog(project.getId(), 0L, 0L, "创建项目");

        return project.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProject(@Valid ProjectSaveReqVO updateReqVO) {
        // 1. 校验项目存在
        ProjectDO project = validateProject(updateReqVO.getId());

        // 2. 更新
        ProjectDO updateObj = ProjectConvert.INSTANCE.convert(updateReqVO);
        projectMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProject(Long id) {
        // 1. 校验项目存在
        validateProject(id);

        // 2. 删除项目（逻辑删除）
        projectMapper.deleteById(id);

        // 3. 删除项目成员
        projectUserMapper.delete(ProjectUserDO::getProjectId, id);
    }

    @Override
    public ProjectDO getProject(Long id) {
        return projectMapper.selectById(id);
    }

    @Override
    public PageResult<ProjectDO> getProjectPage(ProjectPageReqVO pageReqVO) {
        return projectMapper.selectPage(pageReqVO, new LambdaQueryWrapper<ProjectDO>()
                .like(ProjectDO::getName, pageReqVO.getName())
                .orderByDesc(ProjectDO::getId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void archiveProject(Long id, boolean archive) {
        ProjectDO project = validateProject(id);
        Long userId = SecurityFrameworkUtils.getLoginUserId();

        if (archive) {
            project.setArchivedAt(LocalDateTime.now());
            project.setArchivedUserId(userId);
            recordLog(id, 0L, 0L, "项目归档");
        } else {
            project.setArchivedAt(null);
            project.setArchivedUserId(userId);
            recordLog(id, 0L, 0L, "项目取消归档");
        }
        projectMapper.updateById(project);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addMember(Long projectId, Long userId, boolean owner) {
        // 校验项目存在
        validateProject(projectId);

        // 检查是否已是成员
        Long count = projectUserMapper.selectCount(new LambdaQueryWrapper<ProjectUserDO>()
                .eq(ProjectUserDO::getProjectId, projectId)
                .eq(ProjectUserDO::getUserId, userId));
        if (count > 0) {
            throw exception(PROJECT_MEMBER_ALREADY_EXISTS);
        }

        ProjectUserDO projectUser = ProjectUserDO.builder()
                .projectId(projectId)
                .userId(userId)
                .owner(owner)
                .sort(0)
                .build();
        projectUserMapper.insert(projectUser);

        recordLog(projectId, 0L, 0L, "添加成员");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeMember(Long projectId, Long userId) {
        validateProject(projectId);

        int deleted = projectUserMapper.delete(new LambdaQueryWrapper<ProjectUserDO>()
                .eq(ProjectUserDO::getProjectId, projectId)
                .eq(ProjectUserDO::getUserId, userId));
        if (deleted == 0) {
            throw exception(PROJECT_MEMBER_NOT_FOUND);
        }

        recordLog(projectId, 0L, 0L, "移除成员");
    }

    @Override
    public List<ProjectUserDO> getMembers(Long projectId) {
        return projectUserMapper.selectList(ProjectUserDO::getProjectId, projectId);
    }

    private ProjectDO validateProject(Long id) {
        ProjectDO project = projectMapper.selectById(id);
        if (project == null) {
            throw exception(PROJECT_NOT_FOUND);
        }
        return project;
    }

    private void recordLog(Long projectId, Long columnId, Long taskId, String detail) {
        ProjectLogCreateReqVO reqVO = new ProjectLogCreateReqVO();
        reqVO.setProjectId(projectId);
        reqVO.setColumnId(columnId);
        reqVO.setTaskId(taskId);
        reqVO.setDetail(detail);
        projectLogService.createLog(reqVO);
    }
}
