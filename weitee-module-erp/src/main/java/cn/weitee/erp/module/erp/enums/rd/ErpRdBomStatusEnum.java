package cn.weitee.erp.module.erp.enums.rd;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErpRdBomStatusEnum {

    DRAFT(0),
    PUBLISHED(1),
    PROCESS(10),
    APPROVE(20),
    REJECT(30),
    FAILED(60);

    private final Integer status;

}
