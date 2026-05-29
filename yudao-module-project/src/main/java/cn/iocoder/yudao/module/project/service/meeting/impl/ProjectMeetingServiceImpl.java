package cn.iocoder.yudao.module.project.service.meeting.impl;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.project.controller.admin.vo.meeting.ProjectMeetingPageReqVO;
import cn.iocoder.yudao.module.project.controller.admin.vo.meeting.ProjectMeetingSaveReqVO;
import cn.iocoder.yudao.module.project.dal.dataobject.meeting.ProjectMeetingDO;
import cn.iocoder.yudao.module.project.dal.dataobject.meeting.ProjectMeetingMsgDO;
import cn.iocoder.yudao.module.project.dal.mysql.meeting.ProjectMeetingMapper;
import cn.iocoder.yudao.module.project.dal.mysql.meeting.ProjectMeetingMsgMapper;
import cn.iocoder.yudao.module.project.service.meeting.ProjectMeetingService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.project.enums.ErrorCodeConstants.MEETING_NOT_FOUND;

@Service
@Validated
public class ProjectMeetingServiceImpl implements ProjectMeetingService {

    @Resource
    private ProjectMeetingMapper projectMeetingMapper;
    @Resource
    private ProjectMeetingMsgMapper projectMeetingMsgMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createMeeting(@Valid ProjectMeetingSaveReqVO createReqVO) {
        ProjectMeetingDO meeting = ProjectMeetingDO.builder()
                .name(createReqVO.getName())
                .description(createReqVO.getDescription())
                .startAt(createReqVO.getStartAt())
                .endAt(createReqVO.getEndAt())
                .build();
        projectMeetingMapper.insert(meeting);
        return meeting.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMeeting(@Valid ProjectMeetingSaveReqVO updateReqVO) {
        validateMeeting(updateReqVO.getId());
        ProjectMeetingDO updateObj = ProjectMeetingDO.builder()
                .id(updateReqVO.getId())
                .name(updateReqVO.getName())
                .description(updateReqVO.getDescription())
                .startAt(updateReqVO.getStartAt())
                .endAt(updateReqVO.getEndAt())
                .build();
        projectMeetingMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMeeting(Long id) {
        validateMeeting(id);
        projectMeetingMapper.deleteById(id);
        projectMeetingMsgMapper.delete(ProjectMeetingMsgDO::getMeetingId, id);
    }

    @Override
    public ProjectMeetingDO getMeeting(Long id) {
        return projectMeetingMapper.selectById(id);
    }

    @Override
    public PageResult<ProjectMeetingDO> getMeetingPage(ProjectMeetingPageReqVO pageReqVO) {
        return projectMeetingMapper.selectPage(pageReqVO, new LambdaQueryWrapper<ProjectMeetingDO>()
                .like(ProjectMeetingDO::getName, pageReqVO.getName())
                .orderByDesc(ProjectMeetingDO::getId));
    }

    private ProjectMeetingDO validateMeeting(Long id) {
        ProjectMeetingDO meeting = projectMeetingMapper.selectById(id);
        if (meeting == null) {
            throw exception(MEETING_NOT_FOUND);
        }
        return meeting;
    }
}
