package cn.weitee.erp.module.erp.enums.mrp;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErpMrpPlanStatusEnum {

    DRAFT(0),
    RUNNING(10),
    FINISHED(20),
    CLOSED(30),
    FAILED(40);

    private final Integer status;

}
