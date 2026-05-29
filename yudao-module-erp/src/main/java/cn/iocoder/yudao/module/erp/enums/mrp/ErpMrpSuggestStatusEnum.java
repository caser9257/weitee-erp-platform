package cn.iocoder.yudao.module.erp.enums.mrp;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErpMrpSuggestStatusEnum {

    TO_CONFIRM(0),
    CONFIRMED(10),
    CONVERTED(20),
    REJECTED(30);

    private final Integer status;

}
