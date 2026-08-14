package cn.weitee.erp.module.mes.service;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.mes.controller.admin.vo.worktask.MesWorkTaskPageReqVO;
import cn.weitee.erp.module.mes.controller.admin.vo.worktask.MesWorkTaskUpdatePlanTimeReqVO;
import cn.weitee.erp.module.mes.dal.dataobject.MesWorkTaskDO;

import java.util.List;

public interface MesWorkTaskService {

    /**
     * 工单下达后生成工序任务（幂等：按 order_step_id 判重）。
     */
    void createTasksByOrderReleased(Long productionOrderId);

    /**
     * 按工单重新生成缺失任务（重试入口）。
     */
    void regenerateTasks(Long productionOrderId);

    PageResult<MesWorkTaskDO> getTaskPage(MesWorkTaskPageReqVO pageReqVO);

    List<MesWorkTaskDO> getTaskListByOrderId(Long productionOrderId);

    void updatePlanTime(MesWorkTaskUpdatePlanTimeReqVO reqVO);

    void cancelTask(Long id);

    /**
     * 更新优先级（仅影响排程顺序）。
     */
    void updatePriority(Long id, Integer priority);

    /**
     * 自动排程：按工单（可选）或全部待排程任务，返回排程成功数。
     */
    int schedule(Long productionOrderId);

    /**
     * 清空并重排某工单任务。
     */
    int clearAndReschedule(Long productionOrderId);

    /**
     * 冲突检测：返回指定工作中心的冲突任务对。
     */
    List<cn.weitee.erp.module.mes.service.scheduling.MesSchedulingService.ConflictPair> detectConflicts(Long workCenterId);

    /**
     * 甘特数据：按工作中心 + 时间范围查询已排程任务块。
     */
    List<MesWorkTaskDO> getGanttList(Long workCenterId, java.time.LocalDateTime startTime, java.time.LocalDateTime endTime);

}
