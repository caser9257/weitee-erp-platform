package cn.weitee.erp.module.erp.enums.mrp;

import java.util.Arrays;

public enum ErpOutsourceIssueTypeEnum {

    NORMAL(10, "正常发料"),
    SUPPLEMENT(20, "补料发料");

    private final Integer type;
    private final String name;

    ErpOutsourceIssueTypeEnum(Integer type, String name) {
        this.type = type;
        this.name = name;
    }

    public Integer getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public static boolean isValid(Integer type) {
        return Arrays.stream(values()).anyMatch(item -> item.type.equals(type));
    }

    public static Integer defaultType(Integer type) {
        return type != null ? type : NORMAL.getType();
    }

    public static String resolveName(Integer type) {
        return Arrays.stream(values())
                .filter(item -> item.type.equals(type))
                .map(ErpOutsourceIssueTypeEnum::getName)
                .findFirst()
                .orElse(null);
    }

}
