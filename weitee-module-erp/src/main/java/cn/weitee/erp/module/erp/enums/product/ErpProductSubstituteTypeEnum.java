package cn.weitee.erp.module.erp.enums.product;

import cn.weitee.erp.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * ERP 物料替代类型枚举
 *
 * <p>全局通用：物料主数据层预设，可被研发/制造 BOM 一键带出；
 * 临时替代：仅针对特定场景使用的临时性替代，不参与全局推荐。</p>
 *
 * @author WeTai
 */
@RequiredArgsConstructor
@Getter
public enum ErpProductSubstituteTypeEnum implements ArrayValuable<Integer> {

    GLOBAL(1, "全局通用"),
    TEMPORARY(2, "临时替代");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(ErpProductSubstituteTypeEnum::getType).toArray(Integer[]::new);

    private final Integer type;
    private final String name;

    public static String nameOf(Integer type) {
        return Arrays.stream(values())
                .filter(item -> item.getType().equals(type))
                .map(ErpProductSubstituteTypeEnum::getName)
                .findFirst()
                .orElse(String.valueOf(type));
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
