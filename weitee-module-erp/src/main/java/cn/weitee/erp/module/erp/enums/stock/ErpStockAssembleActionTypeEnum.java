package cn.weitee.erp.module.erp.enums.stock;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErpStockAssembleActionTypeEnum {

    ASSEMBLE("ASSEMBLE", "组装"),
    DISASSEMBLE("DISASSEMBLE", "拆卸");

    private final String type;
    private final String name;

    public static boolean isValid(String type) {
        for (ErpStockAssembleActionTypeEnum item : values()) {
            if (item.type.equals(type)) {
                return true;
            }
        }
        return false;
    }
}
