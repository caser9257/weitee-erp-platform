package cn.weitee.erp.module.mes.service;

import cn.weitee.erp.module.erp.controller.admin.mrp.vo.report.ErpProductionReportCreateReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionOrderStepDO;
import cn.weitee.erp.module.erp.service.mrp.ErpProductionOrderStepService;
import cn.weitee.erp.module.erp.service.mrp.ErpProductionReportService;
import cn.weitee.erp.module.mes.controller.admin.vo.taskexecution.MesTaskExecutionReportReqVO;
import cn.weitee.erp.module.mes.dal.dataobject.MesTaskDispatchDO;
import cn.weitee.erp.module.mes.dal.dataobject.MesWorkTaskDO;
import cn.weitee.erp.module.mes.dal.mysql.MesTaskDispatchMapper;
import cn.weitee.erp.module.mes.dal.mysql.MesWorkTaskMapper;
import cn.weitee.erp.module.mes.enums.MesTaskDispatchStatusEnum;
import cn.weitee.erp.module.mes.enums.MesWorkTaskStatusEnum;
import cn.weitee.erp.framework.common.exception.ServiceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MesTaskExecutionServiceImplTest {

    @InjectMocks
    private MesTaskExecutionServiceImpl service;

    @Mock
    private MesWorkTaskMapper mesWorkTaskMapper;
    @Mock
    private MesTaskDispatchMapper mesTaskDispatchMapper;
    @Mock
    private ErpProductionOrderStepService erpProductionOrderStepService;
    @Mock
    private ErpProductionReportService erpProductionReportService;

    @Test
    void getTask_shouldReturnTaskDispatchAndErpContext() {
        MesWorkTaskDO task = task(MesWorkTaskStatusEnum.SCHEDULED.getStatus());
        MesTaskDispatchDO dispatch = dispatch();
        ErpProductionOrderStepDO step = step(1);
        givenContext(task, dispatch, step);

        var result = service.getTask(task.getTaskNo());

        assertEquals(task.getId(), result.getTaskId());
        assertEquals(dispatch.getId(), result.getDispatchId());
        assertEquals(step.getStepStatus(), result.getErpStepStatus());
        assertEquals(step.getReportedQty(), result.getReportedQty());
    }

    @Test
    void start_shouldCallErpStepService() {
        MesWorkTaskDO task = task(MesWorkTaskStatusEnum.SCHEDULED.getStatus());
        givenContext(task, dispatch(), step(0));

        service.start(task.getTaskNo());

        verify(erpProductionOrderStepService).startStep(task.getOrderStepId());
    }

    @Test
    void report_shouldPassDispatchResourcesToErp() {
        MesWorkTaskDO task = task(MesWorkTaskStatusEnum.SCHEDULED.getStatus());
        MesTaskDispatchDO dispatch = dispatch();
        givenContext(task, dispatch, step(1));
        MesTaskExecutionReportReqVO reqVO = new MesTaskExecutionReportReqVO()
                .setTaskNo(task.getTaskNo())
                .setReportedQty(new BigDecimal("5"))
                .setQualifiedQty(new BigDecimal("4"))
                .setScrapQty(new BigDecimal("1"))
                .setWorkHour(new BigDecimal("2.5"))
                .setBatchNo("B-01")
                .setRemark("首件");
        when(erpProductionReportService.createReport(any())).thenReturn(900L);

        Long reportId = service.report(reqVO);

        assertEquals(900L, reportId);
        ArgumentCaptor<ErpProductionReportCreateReqVO> captor =
                ArgumentCaptor.forClass(ErpProductionReportCreateReqVO.class);
        verify(erpProductionReportService).createReport(captor.capture());
        ErpProductionReportCreateReqVO reportReq = captor.getValue();
        assertEquals(task.getProductionOrderId(), reportReq.getProductionOrderId());
        assertEquals(dispatch.getDeviceId(), reportReq.getItems().get(0).getDeviceId());
        assertEquals(dispatch.getWorkerUserId(), reportReq.getItems().get(0).getWorkerUserId());
        assertEquals(reqVO.getReportedQty(), reportReq.getItems().get(0).getReportedQty());
    }

    @Test
    void getTask_shouldRejectMissingTask() {
        when(mesWorkTaskMapper.selectByTaskNo("RW-MISSING")).thenReturn(null);

        assertThrows(ServiceException.class, () -> service.getTask("RW-MISSING"));
    }

    @Test
    void start_shouldRejectTaskWithoutActiveDispatch() {
        MesWorkTaskDO task = task(MesWorkTaskStatusEnum.SCHEDULED.getStatus());
        when(mesWorkTaskMapper.selectByTaskNo(task.getTaskNo())).thenReturn(task);
        when(mesTaskDispatchMapper.selectActiveByTaskId(task.getId())).thenReturn(null);

        assertThrows(ServiceException.class, () -> service.start(task.getTaskNo()));
    }

    @Test
    void finish_shouldRejectTerminalTaskBeforeCallingErp() {
        MesWorkTaskDO task = task(MesWorkTaskStatusEnum.FINISHED.getStatus());
        when(mesWorkTaskMapper.selectByTaskNo(task.getTaskNo())).thenReturn(task);
        when(mesTaskDispatchMapper.selectActiveByTaskId(task.getId())).thenReturn(dispatch());
        when(erpProductionOrderStepService.getStepList(task.getProductionOrderId())).thenReturn(List.of(step(2)));

        assertThrows(ServiceException.class, () -> service.finish(task.getTaskNo()));
    }

    private void givenContext(MesWorkTaskDO task, MesTaskDispatchDO dispatch, ErpProductionOrderStepDO step) {
        when(mesWorkTaskMapper.selectByTaskNo(task.getTaskNo())).thenReturn(task);
        when(mesTaskDispatchMapper.selectActiveByTaskId(task.getId())).thenReturn(dispatch);
        when(erpProductionOrderStepService.getStepList(task.getProductionOrderId())).thenReturn(List.of(step));
    }

    private MesWorkTaskDO task(Integer status) {
        return new MesWorkTaskDO()
                .setId(10L)
                .setTaskNo("RW-001")
                .setProductionOrderId(20L)
                .setProductionOrderNo("SCGD-001")
                .setOrderStepId(30L)
                .setStepNo(10)
                .setStepCode("OP-10")
                .setStepName("装配")
                .setWorkCenterId(40L)
                .setPlanQty(new BigDecimal("10"))
                .setStatus(status);
    }

    private MesTaskDispatchDO dispatch() {
        return new MesTaskDispatchDO()
                .setId(50L)
                .setTaskId(10L)
                .setDispatchStatus(MesTaskDispatchStatusEnum.ASSIGNED.getStatus())
                .setActiveFlag(1)
                .setDeviceId(60L)
                .setTeamId(70L)
                .setWorkerUserId(80L);
    }

    private ErpProductionOrderStepDO step(Integer status) {
        return new ErpProductionOrderStepDO()
                .setId(30L)
                .setProductionOrderId(20L)
                .setStepStatus(status)
                .setQcFlag(false)
                .setReportedQty(new BigDecimal("2"))
                .setQualifiedQty(new BigDecimal("2"))
                .setScrapQty(BigDecimal.ZERO);
    }
}
