package cn.weitee.erp.module.project.controller.admin;

import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.project.controller.admin.vo.meeting.ProjectMeetingPageReqVO;
import cn.weitee.erp.module.project.controller.admin.vo.meeting.ProjectMeetingRespVO;
import cn.weitee.erp.module.project.controller.admin.vo.meeting.ProjectMeetingSaveReqVO;
import cn.weitee.erp.module.project.dal.dataobject.meeting.ProjectMeetingDO;
import cn.weitee.erp.module.project.service.meeting.ProjectMeetingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

import static cn.weitee.erp.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 会议管理")
@RestController
@RequestMapping("/project/meeting")
@Validated
public class ProjectMeetingController {

    @Resource
    private ProjectMeetingService meetingService;

    @PostMapping("/create")
    @Operation(summary = "创建会议")
    @PreAuthorize("@ss.hasPermission('project:meeting:create')")
    public CommonResult<Long> createMeeting(@Valid @RequestBody ProjectMeetingSaveReqVO createReqVO) {
        return success(meetingService.createMeeting(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新会议")
    @PreAuthorize("@ss.hasPermission('project:meeting:update')")
    public CommonResult<Boolean> updateMeeting(@Valid @RequestBody ProjectMeetingSaveReqVO updateReqVO) {
        meetingService.updateMeeting(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除会议")
    @Parameter(name = "id", description = "会议编号", required = true)
    @PreAuthorize("@ss.hasPermission('project:meeting:delete')")
    public CommonResult<Boolean> deleteMeeting(@RequestParam("id") Long id) {
        meetingService.deleteMeeting(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得会议详情")
    @Parameter(name = "id", description = "会议编号", required = true)
    @PreAuthorize("@ss.hasPermission('project:meeting:query')")
    public CommonResult<ProjectMeetingRespVO> getMeeting(@RequestParam("id") Long id) {
        ProjectMeetingDO meeting = meetingService.getMeeting(id);
        if (meeting == null) {
            return success(null);
        }
        return success(convertToRespVO(meeting));
    }

    @GetMapping("/page")
    @Operation(summary = "获得会议分页")
    @PreAuthorize("@ss.hasPermission('project:meeting:query')")
    public CommonResult<PageResult<ProjectMeetingRespVO>> getMeetingPage(@Valid ProjectMeetingPageReqVO pageReqVO) {
        PageResult<ProjectMeetingDO> pageResult = meetingService.getMeetingPage(pageReqVO);
        List<ProjectMeetingRespVO> respList = pageResult.getList().stream()
                .map(this::convertToRespVO)
                .collect(Collectors.toList());
        return success(new PageResult<>(respList, pageResult.getTotal()));
    }

    private ProjectMeetingRespVO convertToRespVO(ProjectMeetingDO meeting) {
        ProjectMeetingRespVO resp = new ProjectMeetingRespVO();
        resp.setId(meeting.getId());
        resp.setName(meeting.getName());
        resp.setDescription(meeting.getDescription());
        resp.setStartAt(meeting.getStartAt());
        resp.setEndAt(meeting.getEndAt());
        resp.setCreateTime(meeting.getCreateTime());
        return resp;
    }
}
