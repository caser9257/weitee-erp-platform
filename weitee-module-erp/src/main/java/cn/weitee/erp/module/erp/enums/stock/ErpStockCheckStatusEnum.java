package cn.weitee.erp.module.erp.enums.stock;

import cn.weitee.erp.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * ERP 盘点单状态枚举
 *
 * 场景 A 简化状态机：
 * DRAFT(0) → COUNTING(10) → REVIEWING(20) → APPROVED(30) → CLOSED(40)
 *
 * @author ruoyi-vue-pro
 */
@RequiredArgsConstructor
@Getter
public enum ErpStockCheckStatusEnum implements ArrayValuable<Integer> {

    /**
     * 草稿 - 新建盘点单
     */
    DRAFT(0, "草稿"),

    /**
     * 盘点中 - 已生成快照，正在录入实盘数量
     */
    COUNTING(10, "盘点中"),

    /**
     * 审核中 - 已提交，等待审核
     */
    REVIEWING(20, "审核中"),

    /**
     * 已审核 - 审核通过，已生成凭证
     */
    APPROVED(30, "已审核"),

    /**
     * 已关闭 - 终态，已解冻仓库
     */
    CLOSED(40, "已关闭");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(ErpStockCheckStatusEnum::getStatus).toArray(Integer[]::new);

    /**
     * 状态值
     */
    private final Integer status;

    /**
     * 状态名称
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    /**
     * 根据状态值获取枚举
     *
     * @param status 状态值
     * @return 枚举，不存在返回 null
     */
    public static ErpStockCheckStatusEnum fromStatus(Integer status) {
        if (status == null) {
            return null;
        }
        for (ErpStockCheckStatusEnum value : values()) {
            if (value.getStatus().equals(status)) {
                return value;
            }
        }
        return null;
    }

    /**
     * 判断是否可以流转到目标状态
     *
     * @param targetStatus 目标状态
     * @return 是否可以流转
     */
    public boolean canTransitionTo(Integer targetStatus) {
        ErpStockCheckStatusEnum target = fromStatus(targetStatus);
        if (target == null) {
            return false;
        }

        return switch (this) {
            case DRAFT -> target == COUNTING;
            case COUNTING -> target == REVIEWING || target == DRAFT;
            case REVIEWING -> target == APPROVED || target == COUNTING;
            case APPROVED -> target == CLOSED;
            case CLOSED -> false; // 终态，不允许流转
        };
    }

}
