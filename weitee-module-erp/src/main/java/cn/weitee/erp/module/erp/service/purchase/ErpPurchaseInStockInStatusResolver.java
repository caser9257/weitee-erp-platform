package cn.weitee.erp.module.erp.service.purchase;

import cn.hutool.core.util.ObjectUtil;
import cn.weitee.erp.module.erp.enums.ErpPurchaseInStockInStatusEnum;

import java.math.BigDecimal;

public final class ErpPurchaseInStockInStatusResolver {

    private ErpPurchaseInStockInStatusResolver() {
    }

    public static Integer resolve(BigDecimal qaPassCount, BigDecimal stockInCount) {
        BigDecimal safeQaPassCount = ObjectUtil.defaultIfNull(qaPassCount, BigDecimal.ZERO);
        BigDecimal safeStockInCount = ObjectUtil.defaultIfNull(stockInCount, BigDecimal.ZERO);
        if (safeQaPassCount.compareTo(BigDecimal.ZERO) <= 0) {
            return ErpPurchaseInStockInStatusEnum.NO_NEED_STOCK_IN.getStatus();
        }
        if (safeStockInCount.compareTo(BigDecimal.ZERO) <= 0) {
            return ErpPurchaseInStockInStatusEnum.TO_STOCK_IN.getStatus();
        }
        if (safeStockInCount.compareTo(safeQaPassCount) < 0) {
            return ErpPurchaseInStockInStatusEnum.PARTIAL_STOCKED_IN.getStatus();
        }
        return ErpPurchaseInStockInStatusEnum.STOCKED_IN.getStatus();
    }
}
