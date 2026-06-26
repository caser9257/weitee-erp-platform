package cn.weitee.erp.module.iot.core.topic.state;

import cn.weitee.erp.module.iot.core.enums.IotDeviceMessageMethodEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * IoT 设备状态更新 Request DTO
 * <p>
 * 用于 {@link IotDeviceMessageMethodEnum#STATE_UPDATE} 消息的 params 参数
 *
 * @author WeTai
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IotDeviceStateUpdateReqDTO {

    /**
     * 设备状态
     */
    private Integer state;

}
