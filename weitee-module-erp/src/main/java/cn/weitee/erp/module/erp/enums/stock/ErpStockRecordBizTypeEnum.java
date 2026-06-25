package cn.weitee.erp.module.erp.enums.stock;

import cn.weitee.erp.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * ERP 库存流水业务类型枚举
 */
@RequiredArgsConstructor
@Getter
public enum ErpStockRecordBizTypeEnum implements ArrayValuable<Integer> {

    OTHER_IN(10, "其他入库"),
    OTHER_IN_CANCEL(11, "其他入库（作废）"),

    OTHER_OUT(20, "其他出库"),
    OTHER_OUT_CANCEL(21, "其他出库（作废）"),

    MOVE_IN(30, "调拨入库"),
    MOVE_IN_CANCEL(31, "调拨入库（作废）"),
    MOVE_OUT(32, "调拨出库"),
    MOVE_OUT_CANCEL(33, "调拨出库（作废）"),

    CHECK_MORE_IN(40, "盘盈入库"),
    CHECK_MORE_IN_CANCEL(41, "盘盈入库（作废）"),
    CHECK_LESS_OUT(42, "盘亏出库"),
    CHECK_LESS_OUT_CANCEL(43, "盘亏出库（作废）"),

    SALE_OUT(50, "销售出库"),
    SALE_OUT_CANCEL(51, "销售出库（作废）"),

    SALE_RETURN(60, "销售退货入库"),
    SALE_RETURN_CANCEL(61, "销售退货入库（作废）"),

    PURCHASE_IN(70, "采购入库"),
    PURCHASE_IN_CANCEL(71, "采购入库（作废）"),

    PURCHASE_RETURN(80, "采购退货出库"),
    PURCHASE_RETURN_CANCEL(81, "采购退货出库（作废）"),

    PRODUCTION_ISSUE(90, "生产领料"),
    PRODUCTION_RETURN(91, "生产退料"),

    OUTSOURCE_ISSUE(92, "委外发料"),
    OUTSOURCE_RETURN(93, "委外退料"),
    OUTSOURCE_INBOUND(94, "委外入库"),
    PRODUCTION_IN(95, "自制入库"),
    PRODUCTION_IN_CANCEL(96, "自制入库（反执行）"),

    BATCH_ADJUST_IN(100, "批次调增"),
    BATCH_ADJUST_OUT(101, "批次调减");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(ErpStockRecordBizTypeEnum::getType)
            .toArray(Integer[]::new);

    private final Integer type;

    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
