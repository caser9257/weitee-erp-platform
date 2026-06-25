package cn.weitee.erp.module.erp.enums.mrp;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErpMrpStockReservationStatusEnum {

    ACTIVE(0),
    RELEASED(1);

    private final Integer status;

}
