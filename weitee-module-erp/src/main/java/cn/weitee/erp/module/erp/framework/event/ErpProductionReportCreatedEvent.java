package cn.weitee.erp.module.erp.framework.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/** 生产报工创建事件：供 MES 模块监听回写任务实际时间与状态。 */
@Getter
@AllArgsConstructor
public class ErpProductionReportCreatedEvent {

    private final Long productionOrderId;

    /** 报工涉及的工单工序快照编号列表 */
    private final List<Long> orderStepIds;

    /** 报工时间 */
    private final java.time.LocalDateTime reportTime;

}
