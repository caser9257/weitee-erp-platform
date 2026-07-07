package cn.weitee.erp.module.erp.enums.sale;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErpReceiptStatusEnum {

    NONE(0, "未收款"),
    PARTIAL(1, "部分收款"),
    FULL(2, "全额收款");

    private final Integer status;
    private final String name;

}
