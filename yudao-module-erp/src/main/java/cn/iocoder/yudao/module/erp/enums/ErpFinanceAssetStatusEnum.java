package cn.iocoder.yudao.module.erp.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum ErpFinanceAssetStatusEnum implements ArrayValuable<Integer> {

    DRAFT(0, "草稿"),
    ACTIVE(10, "使用中"),
    DISABLED(20, "已停用"),
    DISPOSED(30, "已处置");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(ErpFinanceAssetStatusEnum::getStatus).toArray(Integer[]::new);

    private final Integer status;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }
}
