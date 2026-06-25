package cn.weitee.erp.framework.common.enums;

import cn.weitee.erp.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 批量修改模式枚举
 */
@Getter
@AllArgsConstructor
public enum BatchEditModeEnum implements ArrayValuable<String> {

    OVERWRITE("overwrite", "覆盖修改");

    public static final String[] ARRAYS = Arrays.stream(values()).map(BatchEditModeEnum::getValue).toArray(String[]::new);

    /**
     * 模式值
     */
    private final String value;
    /**
     * 模式名
     */
    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
