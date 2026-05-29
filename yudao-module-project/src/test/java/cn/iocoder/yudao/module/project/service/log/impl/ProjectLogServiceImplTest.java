package cn.iocoder.yudao.module.project.service.log.impl;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.project.controller.admin.vo.log.ProjectLogCreateReqVO;
import cn.iocoder.yudao.module.project.controller.admin.vo.log.ProjectLogPageReqVO;
import cn.iocoder.yudao.module.project.controller.admin.vo.log.ProjectLogRespVO;
import cn.iocoder.yudao.module.project.dal.dataobject.project.ProjectLogDO;
import cn.iocoder.yudao.module.project.dal.mysql.project.ProjectLogMapper;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectLogServiceImplTest {

    @Mock
    private ProjectLogMapper projectLogMapper;
    @Mock
    private AdminUserApi adminUserApi;

    @InjectMocks
    private ProjectLogServiceImpl projectLogService;

    @Test
    void createLog_shouldInsertLogAndReturnId() {
        ProjectLogCreateReqVO reqVO = new ProjectLogCreateReqVO();
        reqVO.setProjectId(1L);
        reqVO.setColumnId(2L);
        reqVO.setTaskId(3L);
        reqVO.setDetail("创建任务");
        reqVO.setRecord(Map.of("before", "待办", "after", "进行中"));

        try (MockedStatic<SecurityFrameworkUtils> mockedStatic = mockStatic(SecurityFrameworkUtils.class)) {
            mockedStatic.when(SecurityFrameworkUtils::getLoginUserId).thenReturn(100L);
            doAnswer(invocation -> {
                ProjectLogDO projectLog = invocation.getArgument(0);
                projectLog.setId(888L);
                return 1;
            }).when(projectLogMapper).insert(any(ProjectLogDO.class));

            Long result = projectLogService.createLog(reqVO);

            assertThat(result).isEqualTo(888L);
            ArgumentCaptor<ProjectLogDO> captor = ArgumentCaptor.forClass(ProjectLogDO.class);
            verify(projectLogMapper).insert(captor.capture());
            assertThat(captor.getValue().getUserId()).isEqualTo(100L);
            assertThat(captor.getValue().getProjectId()).isEqualTo(1L);
            assertThat(captor.getValue().getTaskId()).isEqualTo(3L);
            assertThat(captor.getValue().getDetail()).isEqualTo("创建任务");
        }
    }

    @Test
    void createLog_shouldThrowWhenReqIsNull() {
        assertThatThrownBy(() -> projectLogService.createLog(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("日志参数不能为空");
    }

    @Test
    void getLogPage_shouldFillUserInfoAndFallbackWhenMissing() {
        ProjectLogDO projectLog = new ProjectLogDO();
        projectLog.setId(1L);
        projectLog.setProjectId(10L);
        projectLog.setColumnId(20L);
        projectLog.setTaskId(30L);
        projectLog.setUserId(100L);
        projectLog.setDetail("创建任务");
        projectLog.setRecord(Map.of("key", "value"));
        projectLog.setCreateTime(LocalDateTime.of(2026, 5, 18, 10, 30));

        ProjectLogDO fallbackLog = new ProjectLogDO();
        fallbackLog.setId(2L);
        fallbackLog.setProjectId(10L);
        fallbackLog.setTaskId(40L);
        fallbackLog.setUserId(101L);
        fallbackLog.setDetail("删除任务");
        fallbackLog.setCreateTime(LocalDateTime.of(2026, 5, 18, 11, 0));

        when(projectLogMapper.selectPage(any(ProjectLogPageReqVO.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new PageResult<>(List.of(projectLog, fallbackLog), 2L));
        when(adminUserApi.getUserMap(anyCollection())).thenReturn(Map.of(100L, adminUser("张三", "avatar-zhangsan")));

        PageResult<ProjectLogRespVO> result = projectLogService.getLogPage(new ProjectLogPageReqVO());

        assertThat(result.getTotal()).isEqualTo(2L);
        assertThat(result.getList()).hasSize(2);
        assertThat(result.getList().get(0).getUserName()).isEqualTo("张三");
        assertThat(result.getList().get(0).getUserAvatar()).isEqualTo("avatar-zhangsan");
        assertThat(result.getList().get(1).getUserName()).isEqualTo("用户101");
        assertThat(result.getList().get(1).getUserAvatar()).isNull();
        verify(adminUserApi).getUserMap(anyCollection());
    }

    @Test
    void getLogPage_shouldReturnEmptyListWithoutUserLookup() {
        when(projectLogMapper.selectPage(any(ProjectLogPageReqVO.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new PageResult<>(List.of(), 0L));

        PageResult<ProjectLogRespVO> result = projectLogService.getLogPage(new ProjectLogPageReqVO());

        assertThat(result.getTotal()).isZero();
        assertThat(result.getList()).isEmpty();
        verify(adminUserApi, org.mockito.Mockito.never()).getUserMap(anyCollection());
    }

    private AdminUserRespDTO adminUser(String nickname, String avatar) {
        AdminUserRespDTO user = new AdminUserRespDTO();
        user.setNickname(nickname);
        user.setAvatar(avatar);
        return user;
    }

}
