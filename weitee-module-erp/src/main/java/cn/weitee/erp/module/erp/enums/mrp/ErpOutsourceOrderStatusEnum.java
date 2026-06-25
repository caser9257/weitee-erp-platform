package cn.weitee.erp.module.erp.enums.mrp;

import java.util.Arrays;

public enum ErpOutsourceOrderStatusEnum {

    CREATED(10, "已创建"),
    PROCESSING(20, "处理中"),
    COMPLETED(30, "已完工"),
    CLOSED(40, "已结案");

    private final Integer status;
    private final String name;

    ErpOutsourceOrderStatusEnum(Integer status, String name) {
        this.status = status;
        this.name = name;
    }

    public Integer getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }

    public static String resolveName(Integer status) {
        return Arrays.stream(values())
                .filter(item -> item.status.equals(status))
                .map(ErpOutsourceOrderStatusEnum::getName)
                .findFirst()
                .orElse(null);
    }

}
