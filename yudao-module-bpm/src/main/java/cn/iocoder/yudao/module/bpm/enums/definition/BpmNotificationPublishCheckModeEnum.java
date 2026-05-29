package cn.iocoder.yudao.module.bpm.enums.definition;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum BpmNotificationPublishCheckModeEnum implements ArrayValuable<String> {

    STRICT("STRICT", "严格校验"),
    WARN("WARN", "仅提醒");

    public static final String[] ARRAYS = Arrays.stream(values())
            .map(BpmNotificationPublishCheckModeEnum::getCode).toArray(String[]::new);

    private final String code;
    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
