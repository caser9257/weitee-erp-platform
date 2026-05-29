package cn.iocoder.yudao.module.project.service.log.impl;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.project.controller.admin.vo.log.ProjectLogCreateReqVO;
import cn.iocoder.yudao.module.project.controller.admin.vo.log.ProjectLogPageReqVO;
import cn.iocoder.yudao.module.project.controller.admin.vo.log.ProjectLogRespVO;
import cn.iocoder.yudao.module.project.dal.dataobject.project.ProjectLogDO;
import cn.iocoder.yudao.module.project.dal.mysql.project.ProjectLogMapper;
import cn.iocoder.yudao.module.project.service.log.ProjectLogService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@Validated
public class ProjectLogServiceImpl implements ProjectLogService {

    @Resource
    private ProjectLogMapper projectLogMapper;
    @Resource
    private AdminUserApi adminUserApi;

    @Override
    public Long createLog(ProjectLogCreateReqVO reqVO) {
        Assert.notNull(reqVO, "日志参数不能为空");
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        ProjectLogDO projectLog = ProjectLogDO.builder()
                .projectId(reqVO.getProjectId())
                .columnId(reqVO.getColumnId())
                .taskId(reqVO.getTaskId())
                .userId(userId)
                .detail(reqVO.getDetail())
                .record(reqVO.getRecord())
                .build();
        projectLogMapper.insert(projectLog);
        return projectLog.getId();
    }

    @Override
    public PageResult<ProjectLogRespVO> getLogPage(ProjectLogPageReqVO reqVO) {
        // 构建查询条件
        LambdaQueryWrapper<ProjectLogDO> queryWrapper = new LambdaQueryWrapper<ProjectLogDO>()
                .eq(reqVO.getProjectId() != null, ProjectLogDO::getProjectId, reqVO.getProjectId())
                .eq(reqVO.getTaskId() != null, ProjectLogDO::getTaskId, reqVO.getTaskId())
                .orderByDesc(ProjectLogDO::getId);

        // 分页查询
        PageResult<ProjectLogDO> pageResult = projectLogMapper.selectPage(reqVO, queryWrapper);
        List<ProjectLogDO> logList = pageResult.getList() == null ? Collections.emptyList() : pageResult.getList();
        Set<Long> userIds = logList.stream()
                .map(ProjectLogDO::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, AdminUserRespDTO> userMap = userIds.isEmpty() ? Collections.emptyMap() : adminUserApi.getUserMap(userIds);

        // 转换为 RespVO
        List<ProjectLogRespVO> respList = logList.stream()
                .map(projectLog -> convertToRespVO(projectLog, userMap.get(projectLog.getUserId())))
                .collect(Collectors.toList());

        return new PageResult<>(respList, pageResult.getTotal());
    }

    private ProjectLogRespVO convertToRespVO(ProjectLogDO projectLog, AdminUserRespDTO user) {
        ProjectLogRespVO respVO = new ProjectLogRespVO();
        respVO.setId(projectLog.getId());
        respVO.setProjectId(projectLog.getProjectId());
        respVO.setTaskId(projectLog.getTaskId());
        respVO.setColumnId(projectLog.getColumnId());
        respVO.setUserId(projectLog.getUserId());
        respVO.setUserName(user != null ? user.getNickname() : "用户" + projectLog.getUserId());
        respVO.setUserAvatar(user != null ? user.getAvatar() : null);
        respVO.setDetail(projectLog.getDetail());
        respVO.setRecord(projectLog.getRecord());
        respVO.setCreateTime(projectLog.getCreateTime());
        return respVO;
    }

}
