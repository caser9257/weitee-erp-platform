package cn.weitee.erp.module.erp.enums.mrp;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErpDeviceStatusEnum {

    IDLE(1),
    RUNNING(2),
    MAINTENANCE(3),
    DISABLED(4);

    private final Integer status;
}
