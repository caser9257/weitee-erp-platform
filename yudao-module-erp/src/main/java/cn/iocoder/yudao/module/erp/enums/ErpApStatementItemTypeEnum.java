package cn.iocoder.yudao.module.erp.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum ErpApStatementItemTypeEnum implements ArrayValuable<Integer> {

    CREATED(10, "生成应付"),
    PAYMENT_ALLOCATED(20, "付款核销"),
    PAYMENT_ALLOCATE_ROLLBACK(30, "核销回滚"),
    CLOSED(40, "台账关闭"),
    INVOICE_UPDATED(50, "收票登记"),
    PREPAYMENT_ALLOCATED(60, "预付核销"),
    PREPAYMENT_ALLOCATE_ROLLBACK(70, "预付核销回滚"),
    INVOICE_MATCHED(80, "发票匹配"),
    INVOICE_MATCH_CANCELED(90, "发票匹配撤销");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(ErpApStatementItemTypeEnum::getStatus)
            .toArray(Integer[]::new);

    private final Integer status;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
