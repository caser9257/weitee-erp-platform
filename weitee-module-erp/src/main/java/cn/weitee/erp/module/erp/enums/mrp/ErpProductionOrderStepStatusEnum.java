package cn.weitee.erp.module.erp.enums.mrp;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErpProductionOrderStepStatusEnum {

    WAIT(0),
    PROCESSING(1),
    FINISHED(2),
    PAUSED(3);

    private final Integer status;

}
