package cn.weitee.erp.module.mes.service;

import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.service.mrp.ErpDeviceService;
import cn.weitee.erp.module.mes.controller.admin.vo.taskdispatch.MesTaskDispatchPageReqVO;
import cn.weitee.erp.module.mes.controller.admin.vo.taskdispatch.MesTaskDispatchSaveReqVO;
import cn.weitee.erp.module.mes.controller.admin.vo.taskdispatch.MesTaskDispatchRespVO;
import cn.weitee.erp.module.mes.controller.admin.vo.worktask.MesWorkTaskPageReqVO;
import cn.weitee.erp.module.mes.dal.dataobject.MesTaskDispatchDO;
import cn.weitee.erp.module.mes.dal.dataobject.MesWorkTaskDO;
import cn.weitee.erp.module.mes.dal.mysql.MesTaskDispatchMapper;
import cn.weitee.erp.module.mes.dal.mysql.MesWorkTaskMapper;
import cn.weitee.erp.module.mes.enums.MesWorkTaskStatusEnum;
import cn.weitee.erp.module.system.service.dept.DeptService;
import cn.weitee.erp.module.system.service.user.AdminUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MesTaskDispatchServiceImplTest {

    @Mock
    private MesTaskDispatchMapper mesTaskDispatchMapper;
    @Mock
    private MesWorkTaskMapper mesWorkTaskMapper;
    @Mock
    private AdminUserService adminUserService;
    @Mock
    private DeptService deptService;
    @Mock
    private ErpDeviceService erpDeviceService;
    @InjectMocks
    private MesTaskDispatchServiceImpl service;

    @Test
    void assign_shouldCreateDispatchWithTaskSnapshot() {
        MesWorkTaskDO task = task(MesWorkTaskStatusEnum.SCHEDULED.getStatus());
        when(mesWorkTaskMapper.selectByIdForUpdate(10L)).thenReturn(task);
        when(mesTaskDispatchMapper.selectActiveByTaskId(10L)).thenReturn(null);
        doAnswer(invocation -> {
            MesTaskDispatchDO dispatch = invocation.getArgument(0);
            dispatch.setId(99L);
            return 1;
        }).when(mesTaskDispatchMapper).insert(any(MesTaskDispatchDO.class));

        MesTaskDispatchSaveReqVO reqVO = req(10L);
        Long id = service.assign(reqVO);

        assertEquals(99L, id);
        ArgumentCaptor<MesTaskDispatchDO> captor = ArgumentCaptor.forClass(MesTaskDispatchDO.class);
        verify(mesTaskDispatchMapper).insert(captor.capture());
        MesTaskDispatchDO inserted = captor.getValue();
        assertEquals("RW-001", inserted.getTaskNo());
        assertEquals("工序一", inserted.getStepName());
        assertEquals(20L, inserted.getWorkerUserId());
        assertEquals(1, inserted.getActiveFlag());
    }

    @Test
    void assign_shouldRejectWhenTaskAlreadyHasActiveDispatch() {
        when(mesWorkTaskMapper.selectByIdForUpdate(10L)).thenReturn(task(MesWorkTaskStatusEnum.WAIT_SCHEDULE.getStatus()));
        when(mesTaskDispatchMapper.selectActiveByTaskId(10L)).thenReturn(new MesTaskDispatchDO().setId(88L));

        assertThrows(ServiceException.class, () -> service.assign(req(10L)));
        verify(mesTaskDispatchMapper, never()).insert(any(MesTaskDispatchDO.class));
    }

    @Test
    void assign_shouldRejectWhenTaskIsProcessing() {
        when(mesWorkTaskMapper.selectByIdForUpdate(10L)).thenReturn(task(MesWorkTaskStatusEnum.PROCESSING.getStatus()));

        assertThrows(ServiceException.class, () -> service.assign(req(10L)));
        verifyNoInteractions(mesTaskDispatchMapper);
    }

    @Test
    void assign_shouldRejectWhenAssigneeIsMissing() {
        when(mesWorkTaskMapper.selectByIdForUpdate(10L)).thenReturn(task(MesWorkTaskStatusEnum.SCHEDULED.getStatus()));
        MesTaskDispatchSaveReqVO reqVO = new MesTaskDispatchSaveReqVO();
        reqVO.setTaskId(10L);

        assertThrows(ServiceException.class, () -> service.assign(reqVO));
        verifyNoInteractions(mesTaskDispatchMapper);
    }

    @Test
    void assign_shouldValidateEverySelectedAssignee() {
        when(mesWorkTaskMapper.selectByIdForUpdate(10L)).thenReturn(task(MesWorkTaskStatusEnum.SCHEDULED.getStatus()));
        when(mesTaskDispatchMapper.selectActiveByTaskId(10L)).thenReturn(null);
        doAnswer(invocation -> {
            MesTaskDispatchDO dispatch = invocation.getArgument(0);
            dispatch.setId(99L);
            return 1;
        }).when(mesTaskDispatchMapper).insert(any(MesTaskDispatchDO.class));

        MesTaskDispatchSaveReqVO reqVO = req(10L);
        reqVO.setTeamId(30L);
        reqVO.setDeviceId(40L);
        service.assign(reqVO);

        verify(deptService).validateDeptList(java.util.Collections.singleton(30L));
        verify(adminUserService).validateUserList(java.util.Collections.singleton(20L));
        verify(erpDeviceService).get(40L);
    }

    @Test
    void assign_shouldRejectWhenWorkerIsInvalid() {
        when(mesWorkTaskMapper.selectByIdForUpdate(10L)).thenReturn(task(MesWorkTaskStatusEnum.SCHEDULED.getStatus()));
        doThrow(new ServiceException()).when(adminUserService).validateUserList(any());

        assertThrows(ServiceException.class, () -> service.assign(req(10L)));
        verifyNoInteractions(mesTaskDispatchMapper);
    }

    @Test
    void reassign_shouldRevokeOldAndCreateNewDispatch() {
        MesWorkTaskDO task = task(MesWorkTaskStatusEnum.SCHEDULED.getStatus());
        when(mesWorkTaskMapper.selectByIdForUpdate(10L)).thenReturn(task);
        when(mesTaskDispatchMapper.selectActiveByTaskId(10L))
                .thenReturn(new MesTaskDispatchDO().setId(88L).setWorkerUserId(30L));
        when(mesTaskDispatchMapper.revokeActiveByTaskId(eq(10L), any())).thenReturn(1);
        doAnswer(invocation -> {
            MesTaskDispatchDO dispatch = invocation.getArgument(0);
            dispatch.setId(100L);
            return 1;
        }).when(mesTaskDispatchMapper).insert(any(MesTaskDispatchDO.class));

        MesTaskDispatchSaveReqVO reqVO = req(10L);
        reqVO.setWorkerUserId(40L);
        assertEquals(100L, service.reassign(reqVO));

        verify(mesTaskDispatchMapper).revokeActiveByTaskId(eq(10L), any());
        verify(mesTaskDispatchMapper).insert(any(MesTaskDispatchDO.class));
    }

    @Test
    void reassign_shouldUpdateRemarkWhenAssigneeIsUnchanged() {
        MesWorkTaskDO task = task(MesWorkTaskStatusEnum.SCHEDULED.getStatus());
        when(mesWorkTaskMapper.selectByIdForUpdate(10L)).thenReturn(task);
        when(mesTaskDispatchMapper.selectActiveByTaskId(10L))
                .thenReturn(new MesTaskDispatchDO().setId(88L).setWorkerUserId(20L).setRemark("旧备注"));

        MesTaskDispatchSaveReqVO reqVO = req(10L);
        reqVO.setRemark("新备注");
        assertEquals(88L, service.reassign(reqVO));

        verify(mesTaskDispatchMapper).updateActiveRemarkById(88L, "新备注");
        verify(mesTaskDispatchMapper, never()).revokeActiveByTaskId(eq(10L), any());
        verify(mesTaskDispatchMapper, never()).insert(any(MesTaskDispatchDO.class));
    }

    @Test
    void getWorkbenchPage_shouldApplyDispatchFilterBeforePagination() {
        MesTaskDispatchPageReqVO reqVO = new MesTaskDispatchPageReqVO();
        reqVO.setPageNo(1);
        reqVO.setPageSize(10);
        reqVO.setDispatchStatus(1);
        MesWorkTaskDO task = task(MesWorkTaskStatusEnum.SCHEDULED.getStatus());
        when(mesTaskDispatchMapper.selectTaskIdsByLatestStatus(1)).thenReturn(java.util.List.of(10L));
        when(mesWorkTaskMapper.selectPage(any(MesWorkTaskPageReqVO.class), eq(java.util.List.of(10L))))
                .thenReturn(new PageResult<>(java.util.List.of(task), 1L));
        when(mesTaskDispatchMapper.selectLatestListByTaskIds(java.util.List.of(10L)))
                .thenReturn(java.util.List.of(new MesTaskDispatchDO().setId(88L).setTaskId(10L)
                        .setDispatchStatus(1).setActiveFlag(1).setWorkerUserId(20L)));

        PageResult<MesTaskDispatchRespVO> result = service.getWorkbenchPage(reqVO);

        assertEquals(1L, result.getTotal());
        assertEquals(1, result.getList().size());
        verify(mesWorkTaskMapper).selectPage(any(MesWorkTaskPageReqVO.class), eq(java.util.List.of(10L)));
    }

    @Test
    void revoke_shouldRejectWhenNoActiveDispatchExists() {
        when(mesWorkTaskMapper.selectByIdForUpdate(10L)).thenReturn(task(MesWorkTaskStatusEnum.SCHEDULED.getStatus()));
        when(mesTaskDispatchMapper.revokeActiveByTaskId(eq(10L), any())).thenReturn(0);

        assertThrows(ServiceException.class, () -> service.revoke(10L));
    }

    private MesTaskDispatchSaveReqVO req(Long taskId) {
        MesTaskDispatchSaveReqVO reqVO = new MesTaskDispatchSaveReqVO();
        reqVO.setTaskId(taskId);
        reqVO.setWorkerUserId(20L);
        reqVO.setRemark("首期浏览器派工");
        return reqVO;
    }

    private MesWorkTaskDO task(Integer status) {
        return new MesWorkTaskDO()
                .setId(10L)
                .setTaskNo("RW-001")
                .setProductionOrderId(100L)
                .setProductionOrderNo("SCGD-001")
                .setOrderStepId(1000L)
                .setStepNo(10)
                .setStepCode("OP-10")
                .setStepName("工序一")
                .setWorkCenterId(500L)
                .setPlanQty(new BigDecimal("10"))
                .setStatus(status);
    }
}
