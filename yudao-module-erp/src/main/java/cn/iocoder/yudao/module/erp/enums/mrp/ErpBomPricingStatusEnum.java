package cn.iocoder.yudao.module.erp.enums.mrp;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErpBomPricingStatusEnum {

    SUCCESS("SUCCESS"),
    NO_EFFECTIVE_BOM("NO_EFFECTIVE_BOM"),
    MISSING_PURCHASE_PRICE("MISSING_PURCHASE_PRICE"),
    CYCLIC_BOM("CYCLIC_BOM");

    private final String status;

}
