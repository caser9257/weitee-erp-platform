package cn.iocoder.yudao.module.erp.enums.mrp;

import java.util.Arrays;

public enum ErpOutsourceOrderTypeEnum {

    BOM(10, "有 BOM 委外"),
    SIMPLE(20, "无 BOM 委外");

    private final Integer type;
    private final String name;

    ErpOutsourceOrderTypeEnum(Integer type, String name) {
        this.type = type;
        this.name = name;
    }

    public Integer getType() {
        return type;
    }

    public static String resolveName(Integer type) {
        return Arrays.stream(values())
                .filter(item -> item.type.equals(type))
                .map(item -> item.name)
                .findFirst()
                .orElse(null);
    }

}
