package cn.weitee.erp.module.erp.enums;

import cn.weitee.erp.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * ERP 应收台账明细类型枚举
 *
 * 编码与 erp_ar_statement_item.item_type 历史数据保持一致，禁止变更取值
 *
 * @author WeTai
 */
@RequiredArgsConstructor
@Getter
public enum ErpArStatementItemTypeEnum implements ArrayValuable<Integer> {

    CREATED(1, "生成应收"),
    RECEIPT_ALLOCATED(2, "收款分配"),
    RECEIPT_RETURNED(3, "收款退回"),
    CLOSED(4, "台账关闭");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(ErpArStatementItemTypeEnum::getStatus)
            .toArray(Integer[]::new);

    private final Integer status;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
