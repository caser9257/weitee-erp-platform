package cn.weitee.erp.module.erp.framework.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 生产工单下达事件：供 MES 模块监听生成工序任务。 */
@Getter
@AllArgsConstructor
public class ErpProductionOrderReleasedEvent {

    private final Long productionOrderId;

}
