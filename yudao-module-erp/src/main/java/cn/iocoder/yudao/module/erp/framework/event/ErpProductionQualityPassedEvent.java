package cn.iocoder.yudao.module.erp.framework.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErpProductionQualityPassedEvent {

    private final Long sourceOrderId;

}
