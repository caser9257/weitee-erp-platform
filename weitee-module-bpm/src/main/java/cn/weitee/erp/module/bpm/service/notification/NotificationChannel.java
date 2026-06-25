package cn.weitee.erp.module.bpm.service.notification;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通知渠道枚举
 */
@Getter
@AllArgsConstructor
public enum NotificationChannel {

    INTERNAL_MESSAGE("INTERNAL_MESSAGE", "站内信"),
    DING_TALK("DING_TALK", "钉钉"),
    WECHAT_WORK("WECHAT_WORK", "企业微信"),
    EMAIL("EMAIL", "邮件"),
    SMS("SMS", "短信");

    private final String code;
    private final String name;

}
