package cn.iocoder.yudao.module.system.service.notify;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.infra.api.websocket.WebSocketSenderApi;
import cn.iocoder.yudao.module.system.dal.dataobject.notify.NotifyTemplateDO;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class NotifySendServiceWebSocketTest extends BaseMockitoUnitTest {

    @InjectMocks
    private NotifySendServiceImpl notifySendService;

    @Mock
    private NotifyTemplateService notifyTemplateService;
    @Mock
    private NotifyMessageService notifyMessageService;
    @Mock
    private WebSocketSenderApi webSocketSenderApi;

    @Test
    public void testSendSingleNotify_sendWebSocketRefreshAfterCreateMessage() {
        Long userId = 101L;
        Integer userType = 1;
        String templateCode = "bpm_task_assigned";
        Map<String, Object> templateParams = new HashMap<>();
        templateParams.put("processInstanceName", "采购审批");
        templateParams.put("taskName", "部门负责人审批");
        String content = "您收到了一条新的待办任务";
        Long messageId = 202L;

        NotifyTemplateDO template = new NotifyTemplateDO();
        template.setCode(templateCode);
        template.setNickname("审批助手");
        template.setContent("{taskName}");
        template.setParams(Arrays.asList("processInstanceName", "taskName"));
        template.setStatus(CommonStatusEnum.ENABLE.getStatus());

        when(notifyTemplateService.getNotifyTemplateByCodeFromCache(eq(templateCode))).thenReturn(template);
        when(notifyTemplateService.formatNotifyTemplateContent(eq(template.getContent()), eq(templateParams)))
                .thenReturn(content);
        when(notifyMessageService.createNotifyMessage(eq(userId), eq(userType), eq(template), eq(content), eq(templateParams)))
                .thenReturn(messageId);

        Long result = notifySendService.sendSingleNotify(userId, userType, templateCode, templateParams);

        assertEquals(messageId, result);
        verify(webSocketSenderApi).sendObject(eq(userType), eq(userId), eq("notify-message-create"),
                argThat(payload -> payload instanceof Map
                        && messageId.equals(((Map<?, ?>) payload).get("id"))
                        && templateCode.equals(((Map<?, ?>) payload).get("templateCode"))
                        && content.equals(((Map<?, ?>) payload).get("templateContent"))
                        && "审批助手".equals(((Map<?, ?>) payload).get("templateNickname"))));
    }

}
