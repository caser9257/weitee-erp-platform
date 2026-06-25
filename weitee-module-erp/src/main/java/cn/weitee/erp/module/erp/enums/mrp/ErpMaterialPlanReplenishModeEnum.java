package cn.weitee.erp.module.erp.enums.mrp;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum ErpMaterialPlanReplenishModeEnum {

    LOT_FOR_LOT("LOT_FOR_LOT"),
    FIXED_LOT("FIXED_LOT");

    private final String mode;

    public static boolean contains(String mode) {
        return Arrays.stream(values()).anyMatch(item -> item.getMode().equals(mode));
    }

}
