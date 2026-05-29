package cn.iocoder.yudao.module.erp.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum ErpFinanceDualLedgerDiffSourceTypeEnum implements ArrayValuable<Integer> {

    COST_ITEM(10, "生产成本项目"),
    ASSET_DEPRECIATION(20, "固定资产折旧");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(ErpFinanceDualLedgerDiffSourceTypeEnum::getType)
            .toArray(Integer[]::new);

    private final Integer type;
    private final String name;

    public static String resolveName(Integer type) {
        return Arrays.stream(values())
                .filter(item -> item.type.equals(type))
                .map(ErpFinanceDualLedgerDiffSourceTypeEnum::getName)
                .findFirst()
                .orElse(null);
    }

    public static boolean requiresSourceValue(Integer type) {
        return COST_ITEM.type.equals(type);
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
