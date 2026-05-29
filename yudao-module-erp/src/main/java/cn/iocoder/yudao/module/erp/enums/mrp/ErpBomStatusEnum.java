package cn.iocoder.yudao.module.erp.enums.mrp;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum ErpBomStatusEnum {

    DISABLE(0),
    ENABLE(1);

    private final Integer status;

    public static boolean isValid(Integer status) {
        return Arrays.stream(values()).anyMatch(item -> item.getStatus().equals(status));
    }

}
