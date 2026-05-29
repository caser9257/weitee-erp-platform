package cn.iocoder.yudao.module.erp.enums.mrp;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErpProductionOrderStatusEnum {

    CREATED(0),
    RELEASED(10),
    FINISHED(20),
    CLOSED(30);

    private final Integer status;

}
