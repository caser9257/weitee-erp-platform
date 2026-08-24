package cn.weitee.erp.module.erp.framework.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 工序完工事件：供 MES 模块监听回写任务实际结束时间与状态。 */
@Getter
@AllArgsConstructor
public class ErpProductionOrderStepFinishedEvent {

    private final Long orderStepId;

}
