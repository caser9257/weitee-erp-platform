package cn.iocoder.yudao.module.erp.framework.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * 库存低于安全库存事件
 *
 * 当库存变动后，库存数量低于安全库存时触发
 */
@Getter
@AllArgsConstructor
public class StockBelowSafetyEvent {

    /**
     * 产品ID
     */
    private final Long productId;

    /**
     * 仓库ID
     */
    private final Long warehouseId;

    /**
     * 当前库存数量
     */
    private final BigDecimal currentCount;

    /**
     * 安全库存数量
     */
    private final BigDecimal safetyStock;

    /**
     * 缺口数量（安全库存 - 当前库存）
     */
    private final BigDecimal shortage;

}
