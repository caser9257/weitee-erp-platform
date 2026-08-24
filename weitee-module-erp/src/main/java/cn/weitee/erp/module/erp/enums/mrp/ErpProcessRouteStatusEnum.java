package cn.weitee.erp.module.erp.enums.mrp;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErpProcessRouteStatusEnum {

    DRAFT(0),
    ENABLED(1),
    DISABLED(2);

    private final Integer status;
}
