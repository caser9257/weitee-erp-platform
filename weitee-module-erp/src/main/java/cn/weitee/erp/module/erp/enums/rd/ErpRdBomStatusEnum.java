package cn.weitee.erp.module.erp.enums.rd;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErpRdBomStatusEnum {

    DRAFT(0),
    PUBLISHED(1);

    private final Integer status;

}
