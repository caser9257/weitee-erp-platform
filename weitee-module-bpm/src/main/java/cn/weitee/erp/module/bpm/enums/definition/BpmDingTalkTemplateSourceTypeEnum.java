package cn.weitee.erp.module.bpm.enums.definition;

import cn.hutool.core.util.ArrayUtil;
import cn.weitee.erp.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum BpmDingTalkTemplateSourceTypeEnum implements ArrayValuable<String> {

    RESERVED("RESERVED", "预留配置"),
    TEMPLATE("TEMPLATE", "模板编码");

    public static final String[] ARRAYS = Arrays.stream(values())
            .map(BpmDingTalkTemplateSourceTypeEnum::getCode).toArray(String[]::new);

    private final String code;
    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

    public static BpmDingTalkTemplateSourceTypeEnum getByCode(String code) {
        return ArrayUtil.firstMatch(item -> item.getCode().equals(code), values());
    }

}
