package cn.weitee.erp.module.bpm.service.notification;

import java.util.Map;

/**
 * 通知服务接口
 *
 * 支持多种通知渠道：站内信、钉钉、企业微信、邮件、短信
 */
public interface BpmNotificationService {

    /**
     * 发送通知
     *
     * @param channel 通知渠道
     * @param userId 接收人 ID
     * @param templateCode 模板编码
     * @param templateParams 模板参数
     * @return 是否发送成功
     */
    boolean sendNotification(NotificationChannel channel, Long userId, String templateCode, Map<String, Object> templateParams);

    /**
     * 发送站内信通知
     *
     * @param userId 接收人 ID
     * @param templateCode 模板编码
     * @param templateParams 模板参数
     * @return 是否发送成功
     */
    boolean sendInternalMessage(Long userId, String templateCode, Map<String, Object> templateParams);

    /**
     * 发送钉钉通知
     *
     * @param userId 接收人 ID
     * @param templateCode 模板编码
     * @param templateParams 模板参数
     * @return 是否发送成功
     */
    boolean sendDingTalkMessage(Long userId, String templateCode, Map<String, Object> templateParams);

    /**
     * 发送企业微信通知
     *
     * @param userId 接收人 ID
     * @param templateCode 模板编码
     * @param templateParams 模板参数
     * @return 是否发送成功
     */
    boolean sendWeChatWorkMessage(Long userId, String templateCode, Map<String, Object> templateParams);

    /**
     * 发送邮件通知
     *
     * @param userId 接收人 ID
     * @param templateCode 模板编码
     * @param templateParams 模板参数
     * @return 是否发送成功
     */
    boolean sendEmail(Long userId, String templateCode, Map<String, Object> templateParams);

    /**
     * 发送短信通知
     *
     * @param userId 接收人 ID
     * @param templateCode 模板编码
     * @param templateParams 模板参数
     * @return 是否发送成功
     */
    boolean sendSms(Long userId, String templateCode, Map<String, Object> templateParams);

}
