package cn.iocoder.yudao.module.bpm.service.task.listener;

import cn.iocoder.yudao.module.bpm.service.approval.BpmApprovalDelegationService;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * 审批委托任务监听器
 *
 * 在任务分配时检查是否存在委托关系，如果存在则自动将任务转交给代理人
 */
@Component
@Slf4j
@Scope("prototype")
public class BpmDelegationTaskListener implements TaskListener {

    public static final String DELEGATE_EXPRESSION = "${bpmDelegationTaskListener}";

    @Resource
    private BpmApprovalDelegationService approvalDelegationService;

    @Override
    public void notify(DelegateTask delegateTask) {
        // 1. 获取当前审批人
        String assignee = delegateTask.getAssignee();
        if (assignee == null || assignee.isEmpty()) {
            return;
        }

        // 2. 获取场景编码
        String sceneCode = (String) delegateTask.getVariable("sceneCode");
        if (sceneCode == null || sceneCode.isEmpty()) {
            return;
        }

        // 3. 检查是否存在委托关系
        try {
            Long assigneeUserId = Long.parseLong(assignee);
            Long delegateUserId = approvalDelegationService.getDelegateUserId(assigneeUserId, sceneCode);
            
            if (delegateUserId != null) {
                // 4. 存在委托关系，将任务转交给代理人
                log.info("[notify][任务({}) 审批人({}) 存在委托关系，转交给代理人({})]", 
                        delegateTask.getId(), assigneeUserId, delegateUserId);
                
                delegateTask.setAssignee(String.valueOf(delegateUserId));
                delegateTask.setOwner(assignee);
                
                // 5. 设置委托标记，用于后续记录
                delegateTask.setVariableLocal("delegatedFrom", assigneeUserId);
                delegateTask.setVariableLocal("delegatedTo", delegateUserId);
            }
        } catch (NumberFormatException e) {
            log.warn("[notify][任务({}) 审批人({}) 格式不正确]", delegateTask.getId(), assignee, e);
        } catch (Exception e) {
            log.error("[notify][任务({}) 检查委托关系异常]", delegateTask.getId(), e);
        }
    }
}
