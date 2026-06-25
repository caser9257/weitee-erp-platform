package cn.weitee.erp.module.erp.enums.mrp;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErpMrpSupplyTypeEnum {

    PURCHASE("PURCHASE"),
    MAKE("MAKE");

    private final String type;

}
