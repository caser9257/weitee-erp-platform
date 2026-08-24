package cn.weitee.erp.module.bpm.service.message;

import cn.weitee.erp.framework.web.config.WebProperties;
import cn.weitee.erp.module.bpm.convert.message.BpmMessageConvert;
import cn.weitee.erp.module.bpm.enums.message.BpmMessageEnum;
import cn.weitee.erp.module.bpm.service.message.dto.BpmMessageSendWhenProcessInstanceApproveReqDTO;
import cn.weitee.erp.module.bpm.service.message.dto.BpmMessageSendWhenProcessInstanceCancelReqDTO;
import cn.weitee.erp.module.bpm.service.message.dto.BpmMessageSendWhenProcessInstanceRejectReqDTO;
import cn.weitee.erp.module.bpm.service.message.dto.BpmMessageSendWhenTaskCreatedReqDTO;
import cn.weitee.erp.module.bpm.service.message.dto.BpmMessageSendWhenTaskDelegateReqDTO;
import cn.weitee.erp.module.bpm.service.message.dto.BpmMessageSendWhenTaskTimeoutReqDTO;
import cn.weitee.erp.module.bpm.service.message.dto.BpmMessageSendWhenTaskTransferReqDTO;
import cn.weitee.erp.module.bpm.service.message.dto.BpmMessageSendWhenTaskUrgeReqDTO;
import cn.weitee.erp.module.system.api.notify.NotifyMessageSendApi;
import cn.weitee.erp.module.system.api.notify.dto.NotifySendSingleToUserReqDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * BPM 消息 Service 实现类
 *
 * @author WeTai
 */
@Service
@Validated
@Slf4j
public class BpmMessageServiceImpl implements BpmMessageService {

    @Resource
    private NotifyMessageSendApi notifyMessageSendApi;

    @Resource
    private WebProperties webProperties;

    @Override
    public void sendMessageWhenProcessInstanceApprove(BpmMessageSendWhenProcessInstanceApproveReqDTO reqDTO) {
        Map<String, Object> templateParams = new HashMap<>();
        templateParams.put("processInstanceName", reqDTO.getProcessInstanceName());
        templateParams.put("detailUrl", getProcessInstanceDetailUrl(reqDTO.getProcessInstanceId()));
        sendNotifyMessage(reqDTO.getStartUserId(), BpmMessageEnum.PROCESS_INSTANCE_APPROVE.getSmsTemplateCode(), templateParams);
    }

    @Override
    public void sendMessageWhenProcessInstanceReject(BpmMessageSendWhenProcessInstanceRejectReqDTO reqDTO) {
        Map<String, Object> templateParams = new HashMap<>();
        templateParams.put("processInstanceName", reqDTO.getProcessInstanceName());
        templateParams.put("reason", reqDTO.getReason());
        templateParams.put("detailUrl", getProcessInstanceDetailUrl(reqDTO.getProcessInstanceId()));
        sendNotifyMessage(reqDTO.getStartUserId(), BpmMessageEnum.PROCESS_INSTANCE_REJECT.getSmsTemplateCode(), templateParams);
    }

    @Override
    public void sendMessageWhenProcessInstanceCancel(BpmMessageSendWhenProcessInstanceCancelReqDTO reqDTO) {
        Map<String, Object> templateParams = new HashMap<>();
        templateParams.put("processInstanceName", reqDTO.getProcessInstanceName());
        templateParams.put("reason", reqDTO.getReason());
        templateParams.put("detailUrl", getProcessInstanceDetailUrl(reqDTO.getProcessInstanceId()));
        sendNotifyMessage(reqDTO.getStartUserId(), BpmMessageEnum.PROCESS_INSTANCE_CANCEL.getSmsTemplateCode(), templateParams);
    }

    @Override
    public void sendMessageWhenTaskAssigned(BpmMessageSendWhenTaskCreatedReqDTO reqDTO) {
        Map<String, Object> templateParams = new HashMap<>();
        templateParams.put("processInstanceName", reqDTO.getProcessInstanceName());
        templateParams.put("taskName", reqDTO.getTaskName());
        templateParams.put("startUserNickname", reqDTO.getStartUserNickname());
        templateParams.put("detailUrl", getProcessInstanceDetailUrl(reqDTO.getProcessInstanceId()));
        sendNotifyMessage(reqDTO.getAssigneeUserId(), BpmMessageEnum.TASK_ASSIGNED.getSmsTemplateCode(), templateParams);
    }

    @Override
    public void sendMessageWhenTaskTimeout(BpmMessageSendWhenTaskTimeoutReqDTO reqDTO) {
        Map<String, Object> templateParams = new HashMap<>();
        templateParams.put("processInstanceName", reqDTO.getProcessInstanceName());
        templateParams.put("taskName", reqDTO.getTaskName());
        templateParams.put("detailUrl", getProcessInstanceDetailUrl(reqDTO.getProcessInstanceId()));
        sendNotifyMessage(reqDTO.getAssigneeUserId(), BpmMessageEnum.TASK_TIMEOUT.getSmsTemplateCode(), templateParams);
    }

    @Override
    public void sendMessageWhenTaskUrge(BpmMessageSendWhenTaskUrgeReqDTO reqDTO) {
        Map<String, Object> templateParams = new HashMap<>();
        templateParams.put("processInstanceName", reqDTO.getProcessInstanceName());
        templateParams.put("taskName", reqDTO.getTaskName());
        templateParams.put("urgeMessage", reqDTO.getUrgeMessage());
        templateParams.put("detailUrl", getProcessInstanceDetailUrl(reqDTO.getProcessInstanceId()));
        sendNotifyMessage(reqDTO.getAssigneeUserId(), BpmMessageEnum.TASK_URGE.getSmsTemplateCode(), templateParams);
    }

    @Override
    public void sendMessageWhenTaskTransfer(BpmMessageSendWhenTaskTransferReqDTO reqDTO) {
        Map<String, Object> templateParams = new HashMap<>();
        templateParams.put("processInstanceName", reqDTO.getProcessInstanceName());
        templateParams.put("taskName", reqDTO.getTaskName());
        templateParams.put("reason", reqDTO.getReason());
        templateParams.put("detailUrl", getProcessInstanceDetailUrl(reqDTO.getProcessInstanceId()));
        sendNotifyMessage(reqDTO.getNewAssigneeUserId(), BpmMessageEnum.TASK_TRANSFER.getSmsTemplateCode(), templateParams);
    }

    @Override
    public void sendMessageWhenTaskDelegate(BpmMessageSendWhenTaskDelegateReqDTO reqDTO) {
        Map<String, Object> templateParams = new HashMap<>();
        templateParams.put("processInstanceName", reqDTO.getProcessInstanceName());
        templateParams.put("taskName", reqDTO.getTaskName());
        templateParams.put("reason", reqDTO.getReason());
        templateParams.put("detailUrl", getProcessInstanceDetailUrl(reqDTO.getProcessInstanceId()));
        sendNotifyMessage(reqDTO.getDelegateUserId(), BpmMessageEnum.TASK_DELEGATE.getSmsTemplateCode(), templateParams);
    }

    /**
     * 发送站内信消息
     */
    private void sendNotifyMessage(Long userId, String templateCode, Map<String, Object> templateParams) {
        try {
            NotifySendSingleToUserReqDTO notifyReqDTO = new NotifySendSingleToUserReqDTO();
            notifyReqDTO.setUserId(userId);
            notifyReqDTO.setTemplateCode(templateCode);
            notifyReqDTO.setTemplateParams(templateParams);
            notifyMessageSendApi.sendSingleMessageToAdmin(notifyReqDTO);
        } catch (Exception ex) {
            log.error("[sendNotifyMessage][userId({}) templateCode({}) 站内信发送失败]", userId, templateCode, ex);
        }
    }

    private String getProcessInstanceDetailUrl(String taskId) {
        return webProperties.getAdminUi().getUrl() + "/bpm/process-instance/detail?id=" + taskId;
    }

}
