package cn.weitee.erp.module.bpm.service.task;

import cn.weitee.erp.framework.test.core.ut.BaseMockitoUnitTest;
import cn.weitee.erp.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import cn.weitee.erp.module.bpm.controller.admin.task.vo.instance.BpmApprovalDetailReqVO;
import cn.weitee.erp.module.bpm.controller.admin.task.vo.instance.BpmApprovalDetailRespVO;
import cn.weitee.erp.module.bpm.dal.dataobject.definition.BpmProcessDefinitionInfoDO;
import cn.weitee.erp.module.bpm.framework.flowable.core.enums.BpmnVariableConstants;
import cn.weitee.erp.module.bpm.service.definition.BpmProcessDefinitionService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.runtime.ProcessInstanceBuilder;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class BpmProcessInstanceServiceImplTest extends BaseMockitoUnitTest {

    @Spy
    @InjectMocks
    private BpmProcessInstanceServiceImpl processInstanceService;

    @Mock
    private RuntimeService runtimeService;
    @Mock
    private BpmProcessDefinitionService processDefinitionService;

    @Test
    public void testCreateProcessInstanceAddsStartUserVariablesBeforeValidate() {
        Long userId = 145L;
        String processDefinitionKey = "erp_stock_in_approval";
        String processDefinitionId = "erp_stock_in_approval:2:1";
        Map<String, Object> variables = new HashMap<>();
        AtomicReference<Map<String, Object>> validateVariables = new AtomicReference<>();

        ProcessDefinition definition = mock(ProcessDefinition.class);
        when(definition.getId()).thenReturn(processDefinitionId);
        when(definition.getName()).thenReturn("其它入库审批");
        when(definition.isSuspended()).thenReturn(false);
        when(processDefinitionService.getActiveProcessDefinition(processDefinitionKey)).thenReturn(definition);
        when(processDefinitionService.getProcessDefinitionInfo(processDefinitionId))
                .thenReturn(new BpmProcessDefinitionInfoDO());
        when(processDefinitionService.canUserStartProcessDefinition(any(), eq(userId))).thenReturn(true);

        BpmApprovalDetailRespVO approvalDetail = new BpmApprovalDetailRespVO();
        doAnswer(invocation -> {
            BpmApprovalDetailReqVO reqVO = invocation.getArgument(1);
            validateVariables.set(new HashMap<>(reqVO.getProcessVariables()));
            return approvalDetail;
        }).when(processInstanceService).getApprovalDetail(eq(userId), any(BpmApprovalDetailReqVO.class));

        ProcessInstanceBuilder builder = mock(ProcessInstanceBuilder.class, RETURNS_SELF);
        ProcessInstance instance = mock(ProcessInstance.class);
        when(instance.getId()).thenReturn("process-instance-id");
        when(builder.start()).thenReturn(instance);
        when(runtimeService.createProcessInstanceBuilder()).thenReturn(builder);

        String id = processInstanceService.createProcessInstance(userId, new BpmProcessInstanceCreateReqDTO()
                .setProcessDefinitionKey(processDefinitionKey)
                .setVariables(variables)
                .setBusinessKey("14"));

        assertEquals("process-instance-id", id);
        assertEquals(userId, validateVariables.get().get(BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_START_USER_ID));
        assertEquals(userId, validateVariables.get().get("startUserId"));
    }

}
