package cn.weitee.erp.module.bpm.enums.definition;

import cn.weitee.erp.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum BpmNotificationReceiverTypeEnum implements ArrayValuable<String> {

    ASSIGNEE("ASSIGNEE", "审批人"),
    START_USER("START_USER", "发起人");

    public static final String[] ARRAYS = Arrays.stream(values())
            .map(BpmNotificationReceiverTypeEnum::getCode).toArray(String[]::new);

    private final String code;
    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
