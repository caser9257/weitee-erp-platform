package cn.iocoder.yudao.module.project.service.project;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.project.controller.admin.vo.project.ProjectPageReqVO;
import cn.iocoder.yudao.module.project.controller.admin.vo.project.ProjectSaveReqVO;
import cn.iocoder.yudao.module.project.dal.dataobject.project.ProjectDO;
import cn.iocoder.yudao.module.project.dal.dataobject.project.ProjectUserDO;

import jakarta.validation.Valid;
import java.util.List;

public interface ProjectService {
    Long createProject(@Valid ProjectSaveReqVO createReqVO);
    void updateProject(@Valid ProjectSaveReqVO updateReqVO);
    void deleteProject(Long id);
    ProjectDO getProject(Long id);
    PageResult<ProjectDO> getProjectPage(ProjectPageReqVO pageReqVO);
    void archiveProject(Long id, boolean archive);
    void addMember(Long projectId, Long userId, boolean owner);
    void removeMember(Long projectId, Long userId);
    List<ProjectUserDO> getMembers(Long projectId);
}
