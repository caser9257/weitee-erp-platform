package cn.weitee.erp.module.bpm.framework.flowable.core.candidate.strategy.dept;

import cn.hutool.core.lang.Assert;
import cn.weitee.erp.framework.common.util.number.NumberUtils;
import cn.weitee.erp.module.bpm.framework.flowable.core.candidate.BpmTaskCandidateStrategy;
import cn.weitee.erp.module.bpm.framework.flowable.core.enums.BpmTaskCandidateStrategyEnum;
import cn.weitee.erp.module.bpm.service.task.BpmProcessInstanceService;
import cn.weitee.erp.module.system.api.dept.dto.DeptRespDTO;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static cn.weitee.erp.framework.common.util.collection.SetUtils.asSet;

/**
 * 发起人的部门负责人, 可以是上级部门负责人 {@link BpmTaskCandidateStrategy} 实现类
 *
 * @author jason
 */
@Slf4j
@Component
public class BpmTaskCandidateStartUserDeptLeaderStrategy extends AbstractBpmTaskCandidateDeptLeaderStrategy {

    @Resource
    @Lazy // 避免循环依赖
    private BpmProcessInstanceService processInstanceService;

    @Override
    public BpmTaskCandidateStrategyEnum getStrategy() {
        return BpmTaskCandidateStrategyEnum.START_USER_DEPT_LEADER;
    }

    @Override
    public void validateParam(String param) {
        // 参数是部门的层级
        Assert.isTrue(Integer.parseInt(param) > 0, "部门的层级必须大于 0");
    }

    @Override
    public Set<Long> calculateUsersByTask(DelegateExecution execution, String param) {
        // 获取流程发起人：优先从 processInstance（主线程），失败则从流程变量取
        Long startUserId = resolveStartUserId(execution);
        if (startUserId == null || startUserId == 0) {
            return new HashSet<>();
        }
        return getStartUserDeptLeader(startUserId, param);
    }

    @Override
    public Set<Long> calculateUsersByActivity(BpmnModel bpmnModel, String activityId, String param,
                                              Long startUserId, String processDefinitionId, Map<String, Object> processVariables) {
        // 获取发起人的部门负责人
        return getStartUserDeptLeader(startUserId, param);
    }

    private Set<Long> getStartUserDeptLeader(Long startUserId, String param) {
        int level = Integer.parseInt(param);
        DeptRespDTO dept = super.getStartUserDept(startUserId);
        if (dept == null) {
            log.info("[getStartUserDeptLeader] startUserId={}, dept=null (用户或部门查找失败), userIds={}", startUserId, new HashSet<>());
            return new HashSet<>();
        }
        Long deptLeaderId = super.getAssignLevelDeptLeaderId(dept, level);
        Set<Long> result = deptLeaderId != null ? asSet(deptLeaderId) : new HashSet<>();
        log.info("[getStartUserDeptLeader] startUserId={}, deptId={}, deptName={}, level={}, deptLeaderId={}, result={}",
                startUserId, dept.getId(), dept.getName(), level, deptLeaderId, result);
        return result;
    }

    /**
     * 解析发起人 ID：优先从 Flowable ProcessInstance 获取，失败时从流程变量备用取值
     *
     * 解决异步执行器线程中 processInstance 查询可能失败（无事务上下文）导致候选人为空的问题
     */
    private Long resolveStartUserId(DelegateExecution execution) {
        Long startUserId = null;
        try {
            ProcessInstance processInstance = processInstanceService.getProcessInstance(execution.getProcessInstanceId());
            if (processInstance != null) {
                startUserId = NumberUtils.parseLong(processInstance.getStartUserId());
            }
        } catch (Exception e) {
            // 异步线程中获取 processInstance 可能失败，降级到从流程变量取
        }
        if (startUserId == null || startUserId == 0) {
            Object var = execution.getVariable("startUserId");
            startUserId = var != null ? NumberUtils.parseLong(String.valueOf(var)) : null;
        }
        log.info("[resolveStartUserId][execution={}, startUserId={}]", execution.getProcessInstanceId(), startUserId);
        return startUserId;
    }

}
