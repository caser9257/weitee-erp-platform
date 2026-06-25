package cn.weitee.erp.module.iot.mq.consumer.rule;

import cn.weitee.erp.module.iot.core.messagebus.core.IotMessageBus;
import cn.weitee.erp.module.iot.core.messagebus.core.IotMessageSubscriber;
import cn.weitee.erp.module.iot.core.mq.message.IotDeviceMessage;
import cn.weitee.erp.module.iot.service.rule.data.IotDataRuleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

/**
 * 针对 {@link IotDeviceMessage} 的消费者，处理数据流转
 *
 * @author WeTai
 */
@Component
@Slf4j
public class IotDataRuleMessageSubscriber implements IotMessageSubscriber<IotDeviceMessage> {

    @Resource
    private IotDataRuleService dataRuleService;

    @Resource
    private IotMessageBus messageBus;

    @PostConstruct
    public void init() {
        messageBus.register(this);
    }

    @Override
    public String getTopic() {
        return IotDeviceMessage.MESSAGE_BUS_DEVICE_MESSAGE_TOPIC;
    }

    @Override
    public String getGroup() {
        return "iot_data_rule_consumer";
    }

    @Override
    public void onMessage(IotDeviceMessage message) {
        dataRuleService.executeDataRule(message);
    }

}
