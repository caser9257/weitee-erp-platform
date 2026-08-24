package cn.weitee.erp.module.mes.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MesWorkTaskStatusEnum {

    WAIT_SCHEDULE(0),
    SCHEDULED(1),
    PROCESSING(2),
    FINISHED(3),
    CANCELED(4);

    private final Integer status;

}
