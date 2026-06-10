package cn.iocoder.yudao.module.ai.enums.image;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * AI 绘画状态的枚举
 *
 * @author fansili
 */
@AllArgsConstructor
@Getter
public enum AiImageStatusEnum {

    IN_PROGRESS(10, "进行�?),
    SUCCESS(20, "已完�?),
    FAIL(30, "已失�?);

    /**
     * 状�?
     */
    private final Integer status;
    /**
     * 状态名
     */
    private final String name;

    public static AiImageStatusEnum valueOfStatus(Integer status) {
        for (AiImageStatusEnum statusEnum : AiImageStatusEnum.values()) {
            if (statusEnum.getStatus().equals(status)) {
                return statusEnum;
            }
        }
        throw new IllegalArgumentException("未知会话状态： " + status);
    }

}
