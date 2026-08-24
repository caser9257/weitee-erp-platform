package cn.weitee.erp.module.erp.enums.mrp;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErpProductionReportTypeEnum {

    STEP(1),
    FINISH(2);

    private final Integer type;

}
