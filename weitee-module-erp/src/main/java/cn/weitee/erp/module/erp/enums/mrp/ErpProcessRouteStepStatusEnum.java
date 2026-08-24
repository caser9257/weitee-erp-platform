package cn.weitee.erp.module.erp.enums.mrp;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErpProcessRouteStepStatusEnum {

    WAITING(0),
    IN_PROGRESS(1),
    COMPLETED(2),
    PAUSED(3);

    private final Integer status;
}
