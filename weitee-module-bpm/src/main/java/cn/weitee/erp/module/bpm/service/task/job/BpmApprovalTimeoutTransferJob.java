package cn.weitee.erp.module.bpm.service.task.job;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.weitee.erp.module.bpm.dal.dataobject.approval.BpmApprovalInstanceSnapshotDO;
import cn.weitee.erp.module.bpm.dal.dataobject.approval.BpmApprovalRuleDO;
import cn.weitee.erp.module.bpm.dal.mysql.approval.BpmApprovalRuleMapper;
import cn.weitee.erp.module.bpm.enums.approval.BpmApprovalInstanceSnapshotStatusEnum;
import cn.weitee.erp.module.bpm.service.approval.BpmApprovalInstanceSnapshotService;
import cn.weitee.erp.module.bpm.service.task.BpmTaskService;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 审批超时自动转交定时任务
 *
 * 定时扫描超时的任务，根据配置自动执行转交操作
 */
@Component
@Slf4j
public class BpmApprovalTimeoutTransferJob {

    @Resource
    private TaskService taskService;

    @Resource
    private BpmTaskService bpmTaskService;

    @Resource
    private BpmApprovalInstanceSnapshotService approvalInstanceSnapshotService;

    @Resource
    private BpmApprovalRuleMapper approvalRuleMapper;

    /**
     * 执行超时转交任务
     *
     * 建议配置为每 5 分钟执行一次
     */
    public void execute() {
        log.info("[execute][开始执行审批超时自动转交任务]");

        // 1. 查询所有审批中的快照
        List<BpmApprovalInstanceSnapshotDO> snapshots = approvalInstanceSnapshotService
                .getSnapshotsByStatus(BpmApprovalInstanceSnapshotStatusEnum.PROCESSING.getStatus());

        if (CollUtil.isEmpty(snapshots)) {
            log.info("[execute][没有审批中的流程实例]");
            return;
        }

        // 2. 遍历每个快照，检查是否有超时任务
        for (BpmApprovalInstanceSnapshotDO snapshot : snapshots) {
            try {
                processSnapshot(snapshot);
            } catch (Exception e) {
                log.error("[execute][处理快照({})异常]", snapshot.getId(), e);
            }
        }

        log.info("[execute][审批超时自动转交任务执行完成]");
    }

    private void processSnapshot(BpmApprovalInstanceSnapshotDO snapshot) {
        // 1. 获取流程实例的所有任务
        List<Task> tasks = taskService.createTaskQuery()
                .processInstanceId(snapshot.getProcessInstanceId())
                .taskUnassigned()
                .list();

        // 2. 遍历任务，检查是否超时
        for (Task task : tasks) {
            try {
                processTask(task, snapshot);
            } catch (Exception e) {
                log.error("[processSnapshot][处理任务({})异常]", task.getId(), e);
            }
        }
    }

    private void processTask(Task task, BpmApprovalInstanceSnapshotDO snapshot) {
        // 1. 获取任务的超时配置
        // TODO: 从流程定义中获取超时配置
        // 这里需要根据实际情况从流程定义或规则中获取超时时间

        // 2. 检查任务是否超时
        Date dueDate = task.getDueDate();
        if (dueDate == null || dueDate.after(new Date())) {
            return; // 未超时
        }

        // 3. 获取超时处理策略
        // TODO: 从流程定义中获取超时处理策略
        // 这里假设是自动转交
        Integer timeoutHandlerType = getTimeoutHandlerType(task);
        if (timeoutHandlerType == null) {
            return;
        }

        // 4. 根据策略执行处理
        switch (timeoutHandlerType) {
            case 1: // 自动提醒
                handleReminder(task, snapshot);
                break;
            case 2: // 自动同意
                handleAutoApprove(task, snapshot);
                break;
            case 3: // 自动拒绝
                handleAutoReject(task, snapshot);
                break;
            case 4: // 自动转交
                handleAutoTransfer(task, snapshot);
                break;
            default:
                log.warn("[processTask][任务({}) 未知的超时处理策略({})]", task.getId(), timeoutHandlerType);
        }
    }

    private Integer getTimeoutHandlerType(Task task) {
        // TODO: 从流程定义中获取超时处理策略
        // 这里需要根据实际情况实现
        return null;
    }

    private void handleReminder(Task task, BpmApprovalInstanceSnapshotDO snapshot) {
        // TODO: 发送提醒通知
        log.info("[handleReminder][任务({}) 发送超时提醒]", task.getId());
    }

    private void handleAutoApprove(Task task, BpmApprovalInstanceSnapshotDO snapshot) {
        // TODO: 自动通过
        log.info("[handleAutoApprove][任务({}) 自动通过]", task.getId());
    }

    private void handleAutoReject(Task task, BpmApprovalInstanceSnapshotDO snapshot) {
        // TODO: 自动拒绝
        log.info("[handleAutoReject][任务({}) 自动拒绝]", task.getId());
    }

    private void handleAutoTransfer(Task task, BpmApprovalInstanceSnapshotDO snapshot) {
        // TODO: 自动转交
        log.info("[handleAutoTransfer][任务({}) 自动转交]", task.getId());
    }
}
