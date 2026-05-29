package cn.iocoder.yudao.module.erp.enums.mrp;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum ErpProductionInboundStatusEnum implements ArrayValuable<Integer> {

    PENDING(10, "待入库"),
    EXECUTED(20, "已入库"),
    CANCELED(30, "已作废");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(ErpProductionInboundStatusEnum::getStatus)
            .toArray(Integer[]::new);

    private final Integer status;

    private final String name;

    public static String getNameByStatus(Integer status) {
        if (status == null) {
            return "";
        }
        for (ErpProductionInboundStatusEnum value : values()) {
            if (value.getStatus().equals(status)) {
                return value.getName();
            }
        }
        return "";
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
