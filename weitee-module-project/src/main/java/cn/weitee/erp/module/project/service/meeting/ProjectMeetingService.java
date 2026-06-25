package cn.weitee.erp.module.project.service.meeting;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.project.controller.admin.vo.meeting.ProjectMeetingPageReqVO;
import cn.weitee.erp.module.project.controller.admin.vo.meeting.ProjectMeetingSaveReqVO;
import cn.weitee.erp.module.project.dal.dataobject.meeting.ProjectMeetingDO;

import jakarta.validation.Valid;

public interface ProjectMeetingService {
    Long createMeeting(@Valid ProjectMeetingSaveReqVO createReqVO);
    void updateMeeting(@Valid ProjectMeetingSaveReqVO updateReqVO);
    void deleteMeeting(Long id);
    ProjectMeetingDO getMeeting(Long id);
    PageResult<ProjectMeetingDO> getMeetingPage(ProjectMeetingPageReqVO pageReqVO);
}
