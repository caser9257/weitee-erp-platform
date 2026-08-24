package cn.weitee.erp.module.mes.service;

import cn.weitee.erp.module.erp.controller.admin.mrp.vo.report.ErpProductionReportCreateReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionOrderStepDO;
import cn.weitee.erp.module.erp.service.mrp.ErpProductionOrderStepService;
import cn.weitee.erp.module.erp.service.mrp.ErpProductionReportService;
import cn.weitee.erp.module.mes.controller.admin.vo.taskexecution.MesTaskExecutionReportReqVO;
import cn.weitee.erp.module.mes.controller.admin.vo.taskexecution.MesTaskExecutionRespVO;
import cn.weitee.erp.module.mes.dal.dataobject.MesTaskDispatchDO;
import cn.weitee.erp.module.mes.dal.dataobject.MesWorkTaskDO;
import cn.weitee.erp.module.mes.dal.mysql.MesTaskDispatchMapper;
import cn.weitee.erp.module.mes.dal.mysql.MesWorkTaskMapper;
import cn.weitee.erp.module.mes.enums.MesTaskDispatchStatusEnum;
import cn.weitee.erp.module.mes.enums.MesWorkTaskStatusEnum;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.List;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.mes.enums.ErrorCodeConstants.MES_EXECUTION_ERP_STEP_NOT_EXISTS;
import static cn.weitee.erp.module.mes.enums.ErrorCodeConstants.MES_EXECUTION_NOT_DISPATCHED;
import static cn.weitee.erp.module.mes.enums.ErrorCodeConstants.MES_WORK_TASK_NOT_EXISTS;
import static cn.weitee.erp.module.mes.enums.ErrorCodeConstants.MES_WORK_TASK_STATUS_INVALID;

/** MES 现场执行门面，负责派工校验和 ERP 执行编排。 */
@Service
@Validated
public class MesTaskExecutionServiceImpl implements MesTaskExecutionService {

    @Resource
    private MesWorkTaskMapper mesWorkTaskMapper;
    @Resource
    private MesTaskDispatchMapper mesTaskDispatchMapper;
    @Resource
    private ErpProductionOrderStepService erpProductionOrderStepService;
    @Resource
    private ErpProductionReportService erpProductionReportService;

    @Override
    public MesTaskExecutionRespVO getTask(String taskNo) {
        ExecutionContext context = loadContext(taskNo);
        return toResp(context.task(), context.dispatch(), context.step());
    }

    @Override
    public void start(String taskNo) {
        ExecutionContext context = loadContext(taskNo);
        validateNonTerminal(context.task());
        erpProductionOrderStepService.startStep(context.task().getOrderStepId());
    }

    @Override
    public void pause(String taskNo) {
        ExecutionContext context = loadContext(taskNo);
        validateNonTerminal(context.task());
        erpProductionOrderStepService.pauseStep(context.task().getOrderStepId());
    }

    @Override
    public void resume(String taskNo) {
        ExecutionContext context = loadContext(taskNo);
        validateNonTerminal(context.task());
        erpProductionOrderStepService.resumeStep(context.task().getOrderStepId());
    }

    @Override
    public Long report(MesTaskExecutionReportReqVO reqVO) {
        ExecutionContext context = loadContext(reqVO.getTaskNo());
        validateNonTerminal(context.task());
        ErpProductionReportCreateReqVO reportReq = new ErpProductionReportCreateReqVO()
                .setProductionOrderId(context.task().getProductionOrderId())
                .setReportType(1)
                .setBatchNo(reqVO.getBatchNo())
                .setRemark(reqVO.getRemark());
        ErpProductionReportCreateReqVO.Item item = new ErpProductionReportCreateReqVO.Item()
                .setProductionOrderStepId(context.task().getOrderStepId())
                .setDeviceId(context.dispatch().getDeviceId())
                .setWorkerUserId(context.dispatch().getWorkerUserId())
                .setReportedQty(reqVO.getReportedQty())
                .setQualifiedQty(reqVO.getQualifiedQty())
                .setScrapQty(reqVO.getScrapQty())
                .setWorkHour(reqVO.getWorkHour())
                .setBatchNo(reqVO.getBatchNo())
                .setRemark(reqVO.getRemark());
        reportReq.setItems(List.of(item));
        return erpProductionReportService.createReport(reportReq);
    }

    @Override
    public void finish(String taskNo) {
        ExecutionContext context = loadContext(taskNo);
        validateNonTerminal(context.task());
        erpProductionOrderStepService.finishStep(context.task().getOrderStepId());
    }

    private ExecutionContext loadContext(String taskNo) {
        MesWorkTaskDO task = mesWorkTaskMapper.selectByTaskNo(taskNo);
        if (task == null) {
            throw exception(MES_WORK_TASK_NOT_EXISTS);
        }
        MesTaskDispatchDO dispatch = mesTaskDispatchMapper.selectActiveByTaskId(task.getId());
        if (dispatch == null || !MesTaskDispatchStatusEnum.ASSIGNED.getStatus().equals(dispatch.getDispatchStatus())) {
            throw exception(MES_EXECUTION_NOT_DISPATCHED);
        }
        ErpProductionOrderStepDO step = erpProductionOrderStepService.getStepList(task.getProductionOrderId())
                .stream()
                .filter(item -> task.getOrderStepId().equals(item.getId()))
                .findFirst()
                .orElseThrow(() -> exception(MES_EXECUTION_ERP_STEP_NOT_EXISTS));
        return new ExecutionContext(task, dispatch, step);
    }

    private void validateNonTerminal(MesWorkTaskDO task) {
        if (MesWorkTaskStatusEnum.FINISHED.getStatus().equals(task.getStatus())
                || MesWorkTaskStatusEnum.CANCELED.getStatus().equals(task.getStatus())) {
            throw exception(MES_WORK_TASK_STATUS_INVALID);
        }
    }

    private MesTaskExecutionRespVO toResp(MesWorkTaskDO task, MesTaskDispatchDO dispatch,
                                          ErpProductionOrderStepDO step) {
        return new MesTaskExecutionRespVO()
                .setTaskId(task.getId())
                .setTaskNo(task.getTaskNo())
                .setProductionOrderId(task.getProductionOrderId())
                .setProductionOrderNo(task.getProductionOrderNo())
                .setOrderStepId(task.getOrderStepId())
                .setStepNo(task.getStepNo())
                .setStepCode(task.getStepCode())
                .setStepName(task.getStepName())
                .setWorkCenterId(task.getWorkCenterId())
                .setPlanQty(task.getPlanQty())
                .setTaskStatus(task.getStatus())
                .setPlanStartTime(task.getPlanStartTime())
                .setPlanEndTime(task.getPlanEndTime())
                .setDispatchId(dispatch.getId())
                .setDeviceId(dispatch.getDeviceId())
                .setTeamId(dispatch.getTeamId())
                .setWorkerUserId(dispatch.getWorkerUserId())
                .setDispatchStatus(dispatch.getDispatchStatus())
                .setErpStepStatus(step.getStepStatus())
                .setQcFlag(step.getQcFlag())
                .setReportedQty(step.getReportedQty())
                .setQualifiedQty(step.getQualifiedQty())
                .setScrapQty(step.getScrapQty());
    }

    private record ExecutionContext(MesWorkTaskDO task, MesTaskDispatchDO dispatch,
                                    ErpProductionOrderStepDO step) {
    }
}
