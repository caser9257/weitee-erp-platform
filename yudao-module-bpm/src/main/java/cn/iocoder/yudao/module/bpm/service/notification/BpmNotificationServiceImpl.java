package cn.iocoder.yudao.module.bpm.service.notification;

import cn.iocoder.yudao.module.system.api.notify.NotifyMessageSendApi;
import cn.iocoder.yudao.module.system.api.notify.dto.NotifySendSingleToUserReqDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.Map;

/**
 * 通知服务实现类
 *
 * 支持多种通知渠道：站内信、钉钉、企业微信、邮件、短信
 * 
 * 注意：目前只实现了站内信通知，其他渠道为预留接口
 */
@Service
@Slf4j
public class BpmNotificationServiceImpl implements BpmNotificationService {

    @Resource
    private NotifyMessageSendApi notifyMessageSendApi;

    @Override
    public boolean sendNotification(NotificationChannel channel, Long userId, String templateCode, Map<String, Object> templateParams) {
        switch (channel) {
            case INTERNAL_MESSAGE:
                return sendInternalMessage(userId, templateCode, templateParams);
            case DING_TALK:
                return sendDingTalkMessage(userId, templateCode, templateParams);
            case WECHAT_WORK:
                return sendWeChatWorkMessage(userId, templateCode, templateParams);
            case EMAIL:
                return sendEmail(userId, templateCode, templateParams);
            case SMS:
                return sendSms(userId, templateCode, templateParams);
            default:
                log.warn("[sendNotification][未知的通知渠道({})]", channel);
                return false;
        }
    }

    @Override
    public boolean sendInternalMessage(Long userId, String templateCode, Map<String, Object> templateParams) {
        try {
            NotifySendSingleToUserReqDTO reqDTO = new NotifySendSingleToUserReqDTO();
            reqDTO.setUserId(userId);
            reqDTO.setTemplateCode(templateCode);
            reqDTO.setTemplateParams(templateParams);
            notifyMessageSendApi.sendSingleMessageToAdmin(reqDTO);
            return true;
        } catch (Exception e) {
            log.error("[sendInternalMessage][发送站内信失败] userId={}, templateCode={}", userId, templateCode, e);
            return false;
        }
    }

    @Override
    public boolean sendDingTalkMessage(Long userId, String templateCode, Map<String, Object> templateParams) {
        // TODO: 实现钉钉通知
        log.info("[sendDingTalkMessage][钉钉通知暂未实现] userId={}, templateCode={}", userId, templateCode);
        return false;
    }

    @Override
    public boolean sendWeChatWorkMessage(Long userId, String templateCode, Map<String, Object> templateParams) {
        // TODO: 实现企业微信通知
        log.info("[sendWeChatWorkMessage][企业微信通知暂未实现] userId={}, templateCode={}", userId, templateCode);
        return false;
    }

    @Override
    public boolean sendEmail(Long userId, String templateCode, Map<String, Object> templateParams) {
        // TODO: 实现邮件通知
        log.info("[sendEmail][邮件通知暂未实现] userId={}, templateCode={}", userId, templateCode);
        return false;
    }

    @Override
    public boolean sendSms(Long userId, String templateCode, Map<String, Object> templateParams) {
        // TODO: 实现短信通知
        log.info("[sendSms][短信通知暂未实现] userId={}, templateCode={}", userId, templateCode);
        return false;
    }

}
