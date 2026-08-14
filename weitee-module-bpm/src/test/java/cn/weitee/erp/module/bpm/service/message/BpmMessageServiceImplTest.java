package cn.weitee.erp.module.bpm.service.message;

import cn.hutool.core.map.MapUtil;
import cn.weitee.erp.framework.test.core.ut.BaseMockitoUnitTest;
import cn.weitee.erp.framework.web.config.WebProperties;
import cn.weitee.erp.module.bpm.service.message.dto.BpmMessageSendWhenTaskCreatedReqDTO;
import cn.weitee.erp.module.system.api.notify.NotifyMessageSendApi;
import cn.weitee.erp.module.system.api.notify.dto.NotifySendSingleToUserReqDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BpmMessageServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private BpmMessageServiceImpl bpmMessageService;

    @Mock
    private NotifyMessageSendApi notifyMessageSendApi;
    @Mock
    private WebProperties webProperties;

    @BeforeEach
    public void setUp() {
        WebProperties.Ui adminUi = new WebProperties.Ui();
        adminUi.setUrl("http://admin.local");
        when(webProperties.getAdminUi()).thenReturn(adminUi);
    }

    @Test
    public void testSendMessageWhenTaskAssigned_sendNotifyMessageToAssignee() {
        BpmMessageSendWhenTaskCreatedReqDTO reqDTO = new BpmMessageSendWhenTaskCreatedReqDTO();
        reqDTO.setAssigneeUserId(1024L);
        reqDTO.setProcessInstanceId("PROC-1");
        reqDTO.setProcessInstanceName("采购申请");
        reqDTO.setTaskId("TASK-1");
        reqDTO.setTaskName("财务审批");
        reqDTO.setStartUserId(2048L);
        reqDTO.setStartUserNickname("张三");

        bpmMessageService.sendMessageWhenTaskAssigned(reqDTO);

        verify(notifyMessageSendApi).sendSingleMessageToAdmin(argThat(matchesNotifyRequest(reqDTO)));
    }

    @Test
    public void testSendMessageWhenTaskAssigned_stillSendNotifyWhenNotifySucceeds() {
        BpmMessageSendWhenTaskCreatedReqDTO reqDTO = new BpmMessageSendWhenTaskCreatedReqDTO();
        reqDTO.setAssigneeUserId(1024L);
        reqDTO.setProcessInstanceId("PROC-1");
        reqDTO.setProcessInstanceName("采购申请");
        reqDTO.setTaskId("TASK-1");
        reqDTO.setTaskName("财务审批");
        reqDTO.setStartUserId(2048L);
        reqDTO.setStartUserNickname("张三");

        bpmMessageService.sendMessageWhenTaskAssigned(reqDTO);

        verify(notifyMessageSendApi).sendSingleMessageToAdmin(argThat(matchesNotifyRequest(reqDTO)));
    }

    private static ArgumentMatcher<NotifySendSingleToUserReqDTO> matchesNotifyRequest(
            BpmMessageSendWhenTaskCreatedReqDTO reqDTO) {
        return actual -> actual != null
                && reqDTO.getAssigneeUserId().equals(actual.getUserId())
                && "bpm_task_assigned".equals(actual.getTemplateCode())
                && MapUtil.builder("processInstanceName", reqDTO.getProcessInstanceName())
                .put("taskName", reqDTO.getTaskName())
                .put("startUserNickname", reqDTO.getStartUserNickname())
                .put("detailUrl", "http://admin.local/bpm/process-instance/detail?id=" + reqDTO.getProcessInstanceId())
                .build().equals(actual.getTemplateParams());
    }

}
