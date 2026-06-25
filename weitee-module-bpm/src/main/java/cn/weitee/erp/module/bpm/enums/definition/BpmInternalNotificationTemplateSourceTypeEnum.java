package cn.weitee.erp.module.bpm.enums.definition;

import cn.hutool.core.util.ArrayUtil;
import cn.weitee.erp.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum BpmInternalNotificationTemplateSourceTypeEnum implements ArrayValuable<String> {

    DEFAULT("DEFAULT", "系统默认文案"),
    INLINE("INLINE", "自定义文案");

    public static final String[] ARRAYS = Arrays.stream(values())
            .map(BpmInternalNotificationTemplateSourceTypeEnum::getCode).toArray(String[]::new);

    private final String code;
    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

    public static BpmInternalNotificationTemplateSourceTypeEnum getByCode(String code) {
        return ArrayUtil.firstMatch(item -> item.getCode().equals(code), values());
    }

}
