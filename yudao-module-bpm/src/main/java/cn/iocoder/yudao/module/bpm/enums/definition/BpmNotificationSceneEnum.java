package cn.iocoder.yudao.module.bpm.enums.definition;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@AllArgsConstructor
public enum BpmNotificationSceneEnum implements ArrayValuable<String> {

    TASK_ASSIGNED("TASK_ASSIGNED", "任务到达提醒", BpmNotificationReceiverTypeEnum.ASSIGNEE.getCode(),
            new LinkedHashSet<>(Arrays.asList("processInstanceName", "taskName", "startUserNickname", "detailUrl"))),
    TASK_TIMEOUT("TASK_TIMEOUT", "任务超时提醒", BpmNotificationReceiverTypeEnum.ASSIGNEE.getCode(),
            new LinkedHashSet<>(Arrays.asList("processInstanceName", "taskName", "detailUrl"))),
    PROCESS_APPROVE("PROCESS_APPROVE", "流程通过通知", BpmNotificationReceiverTypeEnum.START_USER.getCode(),
            new LinkedHashSet<>(Arrays.asList("processInstanceName", "detailUrl"))),
    PROCESS_REJECT("PROCESS_REJECT", "流程驳回通知", BpmNotificationReceiverTypeEnum.START_USER.getCode(),
            new LinkedHashSet<>(Arrays.asList("processInstanceName", "reason", "detailUrl")));

    public static final String[] ARRAYS = Arrays.stream(values())
            .map(BpmNotificationSceneEnum::getCode).toArray(String[]::new);

    private final String code;
    private final String name;
    private final String receiverType;
    private final Set<String> allowedVariables;

    @Override
    public String[] array() {
        return ARRAYS;
    }

    public static BpmNotificationSceneEnum getByCode(String code) {
        return ArrayUtil.firstMatch(item -> item.getCode().equals(code), values());
    }

}
