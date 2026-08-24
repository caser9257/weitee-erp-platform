package cn.weitee.erp.module.bpm.framework.flowable.core.candidate.strategy.dept;

import cn.hutool.core.lang.Assert;
import cn.weitee.erp.framework.common.util.number.NumberUtils;
import cn.weitee.erp.module.bpm.framework.flowable.core.candidate.BpmTaskCandidateStrategy;
import cn.weitee.erp.module.bpm.framework.flowable.core.enums.BpmTaskCandidateStrategyEnum;
import cn.weitee.erp.module.bpm.service.task.BpmProcessInstanceService;
import cn.weitee.erp.module.system.api.dept.dto.DeptRespDTO;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static cn.hutool.core.collection.ListUtil.toList;

/**
 * 发起人连续多级部门的负责人 {@link BpmTaskCandidateStrategy} 实现类
 *
 * @author jason
 */
@Component
public class BpmTaskCandidateStartUserDeptLeaderMultiStrategy extends AbstractBpmTaskCandidateDeptLeaderStrategy {

    @Resource
    @Lazy
    private BpmProcessInstanceService processInstanceService;

    @Override
    public BpmTaskCandidateStrategyEnum getStrategy() {
        return BpmTaskCandidateStrategyEnum.START_USER_DEPT_LEADER_MULTI;
    }

    @Override
    public void validateParam(String param) {
        int level = Integer.parseInt(param); // 参数是部门的层级
        Assert.isTrue(level > 0, "部门的层级必须大于 0");
    }

    @Override
    public Set<Long> calculateUsersByTask(DelegateExecution execution, String param) {
        int level = Integer.parseInt(param);
        // 从流程变量获取发起人（不依赖 processInstance 查询，兼容异步线程）
        Long startUserId = resolveStartUserId(execution);
        if (startUserId == null || startUserId == 0) {
            return new HashSet<>();
        }
        DeptRespDTO dept = super.getStartUserDept(startUserId);
        if (dept == null) {
            return new HashSet<>();
        }
        return super.getMultiLevelDeptLeaderIds(toList(dept.getId()), level);
    }

    /**
     * 解析发起人 ID：优先从 processInstance 获取，失败时从流程变量备用取值
     */
    private Long resolveStartUserId(DelegateExecution execution) {
        try {
            ProcessInstance processInstance = processInstanceService.getProcessInstance(execution.getProcessInstanceId());
            if (processInstance != null) {
                return NumberUtils.parseLong(processInstance.getStartUserId());
            }
        } catch (Exception e) {
            // 异步线程中获取 processInstance 可能失败，降级到从流程变量取
        }
        Object var = execution.getVariable("startUserId");
        return var != null ? NumberUtils.parseLong(String.valueOf(var)) : null;
    }

    @Override
    public Set<Long> calculateUsersByActivity(BpmnModel bpmnModel, String activityId, String param,
                                              Long startUserId, String processDefinitionId, Map<String, Object> processVariables) {
        int level = Integer.parseInt(param); // 参数是部门的层级
        DeptRespDTO dept = super.getStartUserDept(startUserId);
        if (dept == null) {
            return new HashSet<>();
        }
        return super.getMultiLevelDeptLeaderIds(toList(dept.getId()), level);
    }

}
