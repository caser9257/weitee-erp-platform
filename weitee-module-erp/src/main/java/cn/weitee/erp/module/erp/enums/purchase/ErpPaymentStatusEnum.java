package cn.weitee.erp.module.erp.enums.purchase;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErpPaymentStatusEnum {

    NONE(0, "未付款"),
    PARTIAL(1, "部分付款"),
    FULL(2, "全额付款");

    private final Integer status;
    private final String name;

}
