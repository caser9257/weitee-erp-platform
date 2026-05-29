package cn.iocoder.yudao.module.erp.enums.sale;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ErpSaleOrderDeliveryReadyStatusEnum {

    NOT_READY("NOT_READY"),
    PART_READY("PART_READY"),
    READY_TO_SHIP("READY_TO_SHIP");

    private final String status;

}
