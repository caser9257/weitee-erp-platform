package cn.weitee.erp.module.bpm.service.notification;

import cn.weitee.erp.module.system.api.mail.MailSendApi;
import cn.weitee.erp.module.system.api.mail.dto.MailSendSingleToUserReqDTO;
import cn.weitee.erp.module.system.api.notify.NotifyMessageSendApi;
import cn.weitee.erp.module.system.api.notify.dto.NotifySendSingleToUserReqDTO;
import cn.weitee.erp.module.system.api.sms.SmsSendApi;
import cn.weitee.erp.module.system.api.sms.dto.send.SmsSendSingleToUserReqDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.Map;

/**
 * 通知服务实现类
 *
 * 支持多种通知渠道：站内信、钉钉、企业微信、邮件、短信
 *
 * 当前实现状态：
 * - 站内信、短信、邮件：已接入系统真实发送通道
 * - 钉钉、企业微信：需外部机器人/Webhook 配置，暂为预留占位
 */
@Service
@Slf4j
public class BpmNotificationServiceImpl implements BpmNotificationService {

    @Resource
    private NotifyMessageSendApi notifyMessageSendApi;

    @Resource
    private SmsSendApi smsSendApi;

    @Resource
    private MailSendApi mailSendApi;

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
        // 预留：需配置钉钉机器人 Webhook 后接入
        log.info("[sendDingTalkMessage][钉钉通知暂未接入，需配置钉钉机器人 Webhook] userId={}, templateCode={}", userId, templateCode);
        return false;
    }

    @Override
    public boolean sendWeChatWorkMessage(Long userId, String templateCode, Map<String, Object> templateParams) {
        // 预留：需配置企业微信应用/机器人后接入
        log.info("[sendWeChatWorkMessage][企业微信通知暂未接入，需配置企业微信应用/机器人] userId={}, templateCode={}", userId, templateCode);
        return false;
    }

    @Override
    public boolean sendEmail(Long userId, String templateCode, Map<String, Object> templateParams) {
        try {
            MailSendSingleToUserReqDTO reqDTO = new MailSendSingleToUserReqDTO();
            reqDTO.setUserId(userId);
            reqDTO.setTemplateCode(templateCode);
            reqDTO.setTemplateParams(templateParams);
            mailSendApi.sendSingleMailToAdmin(reqDTO);
            return true;
        } catch (Exception e) {
            log.error("[sendEmail][发送邮件失败] userId={}, templateCode={}", userId, templateCode, e);
            return false;
        }
    }

    @Override
    public boolean sendSms(Long userId, String templateCode, Map<String, Object> templateParams) {
        try {
            SmsSendSingleToUserReqDTO reqDTO = new SmsSendSingleToUserReqDTO();
            reqDTO.setUserId(userId);
            reqDTO.setTemplateCode(templateCode);
            reqDTO.setTemplateParams(templateParams);
            smsSendApi.sendSingleSmsToAdmin(reqDTO);
            return true;
        } catch (Exception e) {
            log.error("[sendSms][发送短信失败] userId={}, templateCode={}", userId, templateCode, e);
            return false;
        }
    }

}
