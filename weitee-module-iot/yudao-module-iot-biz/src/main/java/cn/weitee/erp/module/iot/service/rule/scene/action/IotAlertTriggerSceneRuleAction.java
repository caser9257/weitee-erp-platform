package cn.weitee.erp.module.iot.service.rule.scene.action;

import cn.hutool.core.collection.CollUtil;
import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.module.iot.core.mq.message.IotDeviceMessage;
import cn.weitee.erp.module.iot.dal.dataobject.alert.IotAlertConfigDO;
import cn.weitee.erp.module.iot.dal.dataobject.rule.IotSceneRuleDO;
import cn.weitee.erp.module.iot.enums.rule.IotSceneRuleActionTypeEnum;
import cn.weitee.erp.module.iot.service.alert.IotAlertConfigService;
import cn.weitee.erp.module.iot.service.alert.IotAlertRecordService;
import cn.weitee.erp.module.system.api.mail.MailSendApi;
import cn.weitee.erp.module.system.api.notify.NotifyMessageSendApi;
import cn.weitee.erp.module.system.api.sms.SmsSendApi;
import org.springframework.stereotype.Component;

import jakarta.annotation.Nullable;
import jakarta.annotation.Resource;
import java.util.List;

/**
 * IoT 告警触发的 {@link IotSceneRuleAction} 实现类
 *
 * @author WeTai
 */
@Component
public class IotAlertTriggerSceneRuleAction implements IotSceneRuleAction {

    @Resource
    private IotAlertConfigService alertConfigService;
    @Resource
    private IotAlertRecordService alertRecordService;

    @Resource
    private SmsSendApi smsSendApi;
    @Resource
    private MailSendApi mailSendApi;
    @Resource
    private NotifyMessageSendApi notifyMessageSendApi;

    @Override
    public void execute(@Nullable IotDeviceMessage message,
                        IotSceneRuleDO rule, IotSceneRuleDO.Action actionConfig) throws Exception {
        List<IotAlertConfigDO> alertConfigs = alertConfigService.getAlertConfigListBySceneRuleIdAndStatus(
                rule.getId(), CommonStatusEnum.ENABLE.getStatus());
        if (CollUtil.isEmpty(alertConfigs)) {
            return;
        }
        alertConfigs.forEach(alertConfig -> {
            // 记录告警记录，传递场景规则ID
            alertRecordService.createAlertRecord(alertConfig, rule.getId(), message);
            // 发送告警消息
            sendAlertMessage(alertConfig, message);
        });
    }

    private void sendAlertMessage(IotAlertConfigDO config, IotDeviceMessage deviceMessage) {
        // TODO @芋艿：等场景联动开发完，再实现
        // TODO @芋艿：短信
        // TODO @芋艿：邮箱
        // TODO @芋艿：站内信
    }

    @Override
    public IotSceneRuleActionTypeEnum getType() {
        return IotSceneRuleActionTypeEnum.ALERT_TRIGGER;
    }

}
