package cn.weitee.erp.module.erp.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErpFinancePeriodStatusEnum {

    OPEN(10, "打开"),
    CLOSED(20, "已关账");

    private final Integer status;
    private final String name;

    public static boolean isOpen(Integer status) {
        return OPEN.getStatus().equals(status);
    }

    public static boolean isClosed(Integer status) {
        return CLOSED.getStatus().equals(status);
    }
}
