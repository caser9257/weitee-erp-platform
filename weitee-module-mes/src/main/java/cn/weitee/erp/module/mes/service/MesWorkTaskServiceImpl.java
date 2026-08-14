package cn.weitee.erp.module.mes.service;

import cn.hutool.core.collection.CollUtil;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionOrderDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionOrderStepDO;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionOrderMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionOrderStepMapper;
import cn.weitee.erp.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.mes.controller.admin.vo.worktask.MesWorkTaskPageReqVO;
import cn.weitee.erp.module.mes.controller.admin.vo.worktask.MesWorkTaskUpdatePlanTimeReqVO;
import cn.weitee.erp.module.mes.dal.dataobject.MesWorkTaskDO;
import cn.weitee.erp.module.mes.dal.mysql.MesWorkTaskMapper;
import cn.weitee.erp.module.mes.enums.MesWorkTaskStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.mes.enums.ErrorCodeConstants.MES_WORK_TASK_NOT_EXISTS;
import static cn.weitee.erp.module.mes.enums.ErrorCodeConstants.MES_WORK_TASK_PLAN_TIME_INVALID;
import static cn.weitee.erp.module.mes.enums.ErrorCodeConstants.MES_WORK_TASK_STATUS_INVALID;

@Service
@Validated
public class MesWorkTaskServiceImpl implements MesWorkTaskService {

    private static final Logger log = LoggerFactory.getLogger(MesWorkTaskServiceImpl.class);

    @Resource
    private MesWorkTaskMapper mesWorkTaskMapper;
    @Resource
    private ErpProductionOrderMapper erpProductionOrderMapper;
    @Resource
    private ErpProductionOrderStepMapper erpProductionOrderStepMapper;
    @Resource
    private ErpNoRedisDAO noRedisDAO;
    @Resource
    private cn.weitee.erp.module.mes.service.scheduling.MesSchedulingService mesSchedulingService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createTasksByOrderReleased(Long productionOrderId) {
        ErpProductionOrderDO order = erpProductionOrderMapper.selectById(productionOrderId);
        if (order == null) {
            log.warn("[createTasksByOrderReleased] 工单不存在，跳过任务生成 orderId={}", productionOrderId);
            return;
        }
        List<ErpProductionOrderStepDO> steps = erpProductionOrderStepMapper.selectListByOrderId(productionOrderId);
        if (CollUtil.isEmpty(steps)) {
            log.warn("[createTasksByOrderReleased] 工单无工序快照，跳过任务生成 orderId={}", productionOrderId);
            return;
        }
        // 批量查询已生成任务，内存判重（避免逐条查询）
        Set<Long> existedStepIds = mesWorkTaskMapper.selectListByOrderId(productionOrderId).stream()
                .map(MesWorkTaskDO::getOrderStepId)
                .collect(Collectors.toSet());
        for (ErpProductionOrderStepDO step : steps) {
            if (existedStepIds.contains(step.getId())) {
                continue; // 幂等：已生成的任务不重复
            }
            MesWorkTaskDO task = new MesWorkTaskDO()
                    .setTaskNo(noRedisDAO.generate("RW"))
                    .setProductionOrderId(order.getId())
                    .setProductionOrderNo(order.getOrderNo())
                    .setOrderStepId(step.getId())
                    .setStepNo(step.getStepNo())
                    .setStepCode(step.getStepCode())
                    .setStepName(step.getStepName())
                    .setWorkCenterId(step.getWorkCenterId())
                    .setPlanQty(step.getPlanQty())
                    .setStatus(MesWorkTaskStatusEnum.WAIT_SCHEDULE.getStatus());
            try {
                mesWorkTaskMapper.insert(task);
            } catch (org.springframework.dao.DuplicateKeyException e) {
                // 并发重复事件：唯一键 uk_mes_work_task_order_step 兜底，视为已生成
                log.info("[createTasksByOrderReleased][任务已存在，跳过 orderStepId={}]", step.getId());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void regenerateTasks(Long productionOrderId) {
        createTasksByOrderReleased(productionOrderId);
    }

    @Override
    public PageResult<MesWorkTaskDO> getTaskPage(MesWorkTaskPageReqVO pageReqVO) {
        return mesWorkTaskMapper.selectPage(pageReqVO);
    }

    @Override
    public List<MesWorkTaskDO> getTaskListByOrderId(Long productionOrderId) {
        return mesWorkTaskMapper.selectListByOrderId(productionOrderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePriority(Long id, Integer priority) {
        validateTaskExists(id);
        int count = mesWorkTaskMapper.updatePriority(id, priority);
        if (count == 0) {
            throw exception(MES_WORK_TASK_NOT_EXISTS);
        }
    }

    @Override
    public int schedule(Long productionOrderId) {
        return productionOrderId != null
                ? mesSchedulingService.scheduleByOrder(productionOrderId)
                : mesSchedulingService.scheduleAll();
    }

    @Override
    public int clearAndReschedule(Long productionOrderId) {
        return mesSchedulingService.clearAndReschedule(productionOrderId);
    }

    @Override
    public List<cn.weitee.erp.module.mes.service.scheduling.MesSchedulingService.ConflictPair> detectConflicts(Long workCenterId) {
        if (workCenterId == null) {
            return List.of();
        }
        return mesSchedulingService.detectConflicts(workCenterId);
    }

    @Override
    public List<MesWorkTaskDO> getGanttList(Long workCenterId, java.time.LocalDateTime startTime,
                                            java.time.LocalDateTime endTime, Integer status) {
        if (workCenterId == null || startTime == null || endTime == null) {
            return List.of();
        }
        return mesWorkTaskMapper.selectGanttList(workCenterId, startTime, endTime, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePlanTime(MesWorkTaskUpdatePlanTimeReqVO reqVO) {
        validateTaskExists(reqVO.getId());
        if (reqVO.getPlanEndTime().isBefore(reqVO.getPlanStartTime())) {
            throw exception(MES_WORK_TASK_PLAN_TIME_INVALID);
        }
        // CAS：仅待排程/已排程状态可更新，避免并发状态漂移
        int count = mesWorkTaskMapper.updatePlanTimeByCas(reqVO.getId(), reqVO.getPlanStartTime(),
                reqVO.getPlanEndTime(), reqVO.getRemark());
        if (count == 0) {
            throw exception(MES_WORK_TASK_STATUS_INVALID);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelTask(Long id) {
        validateTaskExists(id);
        // CAS：仅待排程/已排程状态可取消
        int count = mesWorkTaskMapper.cancelTaskByCas(id);
        if (count == 0) {
            throw exception(MES_WORK_TASK_STATUS_INVALID);
        }
    }

    private MesWorkTaskDO validateTaskExists(Long id) {
        MesWorkTaskDO task = mesWorkTaskMapper.selectById(id);
        if (task == null) {
            throw exception(MES_WORK_TASK_NOT_EXISTS);
        }
        return task;
    }

}
