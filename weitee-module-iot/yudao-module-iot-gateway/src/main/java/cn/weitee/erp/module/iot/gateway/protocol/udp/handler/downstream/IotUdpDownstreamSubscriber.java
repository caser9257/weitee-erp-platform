package cn.weitee.erp.module.iot.gateway.protocol.udp.handler.downstream;

import cn.weitee.erp.module.iot.core.messagebus.core.IotMessageBus;
import cn.weitee.erp.module.iot.core.mq.message.IotDeviceMessage;
import cn.weitee.erp.module.iot.gateway.protocol.IotProtocol;
import cn.weitee.erp.module.iot.gateway.protocol.AbstractIotProtocolDownstreamSubscriber;
import lombok.extern.slf4j.Slf4j;

/**
 * IoT 网关 UDP 下游订阅者：接收下行给设备的消息
 *
 * @author WeTai
 */
@Slf4j
public class IotUdpDownstreamSubscriber extends AbstractIotProtocolDownstreamSubscriber {

    private final IotUdpDownstreamHandler downstreamHandler;

    public IotUdpDownstreamSubscriber(IotProtocol protocol,
                                      IotUdpDownstreamHandler downstreamHandler,
                                      IotMessageBus messageBus) {
        super(protocol, messageBus);
        this.downstreamHandler = downstreamHandler;
    }

    @Override
    protected void handleMessage(IotDeviceMessage message) {
        downstreamHandler.handle(message);
    }

}
