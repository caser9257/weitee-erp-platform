package cn.weitee.erp.module.mes.service;

import cn.weitee.erp.module.erp.service.mrp.ErpDeviceService;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.mes.controller.admin.vo.taskdispatch.MesTaskDispatchPageReqVO;
import cn.weitee.erp.module.mes.controller.admin.vo.taskdispatch.MesTaskDispatchRespVO;
import cn.weitee.erp.module.mes.controller.admin.vo.taskdispatch.MesTaskDispatchSaveReqVO;
import cn.weitee.erp.module.mes.controller.admin.vo.worktask.MesWorkTaskPageReqVO;
import cn.weitee.erp.module.mes.dal.dataobject.MesTaskDispatchDO;
import cn.weitee.erp.module.mes.dal.dataobject.MesWorkTaskDO;
import cn.weitee.erp.module.mes.dal.mysql.MesTaskDispatchMapper;
import cn.weitee.erp.module.mes.dal.mysql.MesWorkTaskMapper;
import cn.weitee.erp.module.mes.enums.MesTaskDispatchStatusEnum;
import cn.weitee.erp.module.mes.enums.MesWorkTaskStatusEnum;
import cn.weitee.erp.module.system.service.dept.DeptService;
import cn.weitee.erp.module.system.service.user.AdminUserService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Collections;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.mes.enums.ErrorCodeConstants.MES_TASK_DISPATCH_ALREADY_EXISTS;
import static cn.weitee.erp.module.mes.enums.ErrorCodeConstants.MES_TASK_DISPATCH_ASSIGNEE_REQUIRED;
import static cn.weitee.erp.module.mes.enums.ErrorCodeConstants.MES_TASK_DISPATCH_NOT_EXISTS;
import static cn.weitee.erp.module.mes.enums.ErrorCodeConstants.MES_WORK_TASK_NOT_EXISTS;
import static cn.weitee.erp.module.mes.enums.ErrorCodeConstants.MES_WORK_TASK_STATUS_INVALID;

@Service
@Validated
public class MesTaskDispatchServiceImpl implements MesTaskDispatchService {

    @Resource
    private MesTaskDispatchMapper mesTaskDispatchMapper;
    @Resource
    private MesWorkTaskMapper mesWorkTaskMapper;
    @Resource
    private AdminUserService adminUserService;
    @Resource
    private DeptService deptService;
    @Resource
    private ErpDeviceService erpDeviceService;

    @Override
    public PageResult<MesTaskDispatchRespVO> getWorkbenchPage(MesTaskDispatchPageReqVO pageReqVO) {
        MesWorkTaskPageReqVO taskPageReqVO = new MesWorkTaskPageReqVO();
        taskPageReqVO.setPageNo(pageReqVO.getPageNo());
        taskPageReqVO.setPageSize(pageReqVO.getPageSize());
        taskPageReqVO.setTaskNo(pageReqVO.getTaskNo());
        taskPageReqVO.setProductionOrderNo(pageReqVO.getProductionOrderNo());
        taskPageReqVO.setWorkCenterId(pageReqVO.getWorkCenterId());
        taskPageReqVO.setStatus(pageReqVO.getTaskStatus());

        PageResult<MesWorkTaskDO> taskPage;
        if (pageReqVO.getDispatchStatus() == null) {
            taskPage = mesWorkTaskMapper.selectPage(taskPageReqVO);
        } else {
            List<Long> taskIds = mesTaskDispatchMapper.selectTaskIdsByLatestStatus(pageReqVO.getDispatchStatus());
            if (taskIds.isEmpty()) {
                return new PageResult<>(List.of(), 0L);
            }
            taskPage = mesWorkTaskMapper.selectPage(taskPageReqVO, taskIds);
        }
        List<MesWorkTaskDO> tasks = taskPage.getList();
        if (tasks == null || tasks.isEmpty()) {
            return new PageResult<>(List.of(), taskPage.getTotal());
        }
        Map<Long, MesTaskDispatchDO> dispatchMap = mesTaskDispatchMapper
                .selectLatestListByTaskIds(tasks.stream().map(MesWorkTaskDO::getId).toList())
                .stream()
                .collect(Collectors.toMap(MesTaskDispatchDO::getTaskId, Function.identity(), (left, right) -> left));
        List<MesTaskDispatchRespVO> result = tasks.stream()
                .map(task -> toResp(task, dispatchMap.get(task.getId())))
                .toList();
        return new PageResult<>(result, taskPage.getTotal());
    }

    @Override
    public List<MesTaskDispatchRespVO> getHistory(Long taskId) {
        validateTaskExists(taskId);
        return mesTaskDispatchMapper.selectHistoryByTaskId(taskId).stream()
                .map(item -> toResp(null, item))
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long assign(MesTaskDispatchSaveReqVO reqVO) {
        MesWorkTaskDO task = lockAndValidateTask(reqVO.getTaskId());
        validateAssignee(reqVO);
        MesTaskDispatchDO current = mesTaskDispatchMapper.selectActiveByTaskId(task.getId());
        if (current != null) {
            throw exception(MES_TASK_DISPATCH_ALREADY_EXISTS);
        }
        return insertDispatch(task, reqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long reassign(MesTaskDispatchSaveReqVO reqVO) {
        MesWorkTaskDO task = lockAndValidateTask(reqVO.getTaskId());
        validateAssignee(reqVO);
        MesTaskDispatchDO current = mesTaskDispatchMapper.selectActiveByTaskId(task.getId());
        if (current != null && sameAssignee(current, reqVO)) {
            if (!Objects.equals(current.getRemark(), reqVO.getRemark())) {
                mesTaskDispatchMapper.updateActiveRemarkById(current.getId(), reqVO.getRemark());
            }
            return current.getId();
        }
        if (current != null) {
            int count = mesTaskDispatchMapper.revokeActiveByTaskId(task.getId(), LocalDateTime.now());
            if (count == 0) {
                throw exception(MES_TASK_DISPATCH_NOT_EXISTS);
            }
        }
        return insertDispatch(task, reqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revoke(Long taskId) {
        MesWorkTaskDO task = lockAndValidateTask(taskId);
        int count = mesTaskDispatchMapper.revokeActiveByTaskId(task.getId(), LocalDateTime.now());
        if (count == 0) {
            throw exception(MES_TASK_DISPATCH_NOT_EXISTS);
        }
    }

    private Long insertDispatch(MesWorkTaskDO task, MesTaskDispatchSaveReqVO reqVO) {
        MesTaskDispatchDO dispatch = new MesTaskDispatchDO()
                .setTaskId(task.getId())
                .setTaskNo(task.getTaskNo())
                .setProductionOrderId(task.getProductionOrderId())
                .setProductionOrderNo(task.getProductionOrderNo())
                .setOrderStepId(task.getOrderStepId())
                .setStepNo(task.getStepNo())
                .setStepCode(task.getStepCode())
                .setStepName(task.getStepName())
                .setWorkCenterId(task.getWorkCenterId())
                .setDeviceId(reqVO.getDeviceId())
                .setTeamId(reqVO.getTeamId())
                .setWorkerUserId(reqVO.getWorkerUserId())
                .setDispatchStatus(MesTaskDispatchStatusEnum.ASSIGNED.getStatus())
                .setActiveFlag(1)
                .setDispatchTime(LocalDateTime.now())
                .setRemark(reqVO.getRemark());
        try {
            mesTaskDispatchMapper.insert(dispatch);
        } catch (DuplicateKeyException ex) {
            throw exception(MES_TASK_DISPATCH_ALREADY_EXISTS);
        }
        return dispatch.getId();
    }

    private MesWorkTaskDO lockAndValidateTask(Long taskId) {
        MesWorkTaskDO task = mesWorkTaskMapper.selectByIdForUpdate(taskId);
        if (task == null) {
            throw exception(MES_WORK_TASK_NOT_EXISTS);
        }
        if (!isDispatchable(task.getStatus())) {
            throw exception(MES_WORK_TASK_STATUS_INVALID);
        }
        return task;
    }

    private MesWorkTaskDO validateTaskExists(Long taskId) {
        MesWorkTaskDO task = mesWorkTaskMapper.selectById(taskId);
        if (task == null) {
            throw exception(MES_WORK_TASK_NOT_EXISTS);
        }
        return task;
    }

    private boolean isDispatchable(Integer taskStatus) {
        return MesWorkTaskStatusEnum.WAIT_SCHEDULE.getStatus().equals(taskStatus)
                || MesWorkTaskStatusEnum.SCHEDULED.getStatus().equals(taskStatus);
    }

    private void validateAssignee(MesTaskDispatchSaveReqVO reqVO) {
        if (reqVO.getDeviceId() == null && reqVO.getTeamId() == null && reqVO.getWorkerUserId() == null) {
            throw exception(MES_TASK_DISPATCH_ASSIGNEE_REQUIRED);
        }
        if (reqVO.getTeamId() != null) {
            deptService.validateDeptList(Collections.singleton(reqVO.getTeamId()));
        }
        if (reqVO.getWorkerUserId() != null) {
            adminUserService.validateUserList(Collections.singleton(reqVO.getWorkerUserId()));
        }
        if (reqVO.getDeviceId() != null) {
            erpDeviceService.get(reqVO.getDeviceId());
        }
    }

    private boolean sameAssignee(MesTaskDispatchDO current, MesTaskDispatchSaveReqVO reqVO) {
        return Objects.equals(current.getDeviceId(), reqVO.getDeviceId())
                && Objects.equals(current.getTeamId(), reqVO.getTeamId())
                && Objects.equals(current.getWorkerUserId(), reqVO.getWorkerUserId());
    }

    private MesTaskDispatchRespVO toResp(MesWorkTaskDO task, MesTaskDispatchDO dispatch) {
        MesTaskDispatchRespVO resp = new MesTaskDispatchRespVO();
        if (task != null) {
            resp.setTaskId(task.getId());
            resp.setTaskNo(task.getTaskNo());
            resp.setProductionOrderId(task.getProductionOrderId());
            resp.setProductionOrderNo(task.getProductionOrderNo());
            resp.setOrderStepId(task.getOrderStepId());
            resp.setStepNo(task.getStepNo());
            resp.setStepCode(task.getStepCode());
            resp.setStepName(task.getStepName());
            resp.setWorkCenterId(task.getWorkCenterId());
            resp.setPlanQty(task.getPlanQty());
            resp.setTaskStatus(task.getStatus());
            resp.setPlanStartTime(task.getPlanStartTime());
            resp.setPlanEndTime(task.getPlanEndTime());
        }
        if (dispatch != null) {
            resp.setId(dispatch.getId());
            resp.setTaskId(dispatch.getTaskId());
            resp.setTaskNo(dispatch.getTaskNo());
            resp.setProductionOrderId(dispatch.getProductionOrderId());
            resp.setProductionOrderNo(dispatch.getProductionOrderNo());
            resp.setOrderStepId(dispatch.getOrderStepId());
            resp.setStepNo(dispatch.getStepNo());
            resp.setStepCode(dispatch.getStepCode());
            resp.setStepName(dispatch.getStepName());
            resp.setWorkCenterId(dispatch.getWorkCenterId());
            resp.setDeviceId(dispatch.getDeviceId());
            resp.setTeamId(dispatch.getTeamId());
            resp.setWorkerUserId(dispatch.getWorkerUserId());
            resp.setDispatchStatus(dispatch.getDispatchStatus());
            resp.setActiveFlag(dispatch.getActiveFlag());
            resp.setDispatchTime(dispatch.getDispatchTime());
            resp.setRevokeTime(dispatch.getRevokeTime());
            resp.setRemark(dispatch.getRemark());
        }
        return resp;
    }
}
