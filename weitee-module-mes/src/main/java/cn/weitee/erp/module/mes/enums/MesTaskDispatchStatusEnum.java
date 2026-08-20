package cn.weitee.erp.module.mes.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** MES 工序任务派工状态。 */
@Getter
@AllArgsConstructor
public enum MesTaskDispatchStatusEnum {

    ASSIGNED(1),
    REVOKED(2);

    private final Integer status;
}
