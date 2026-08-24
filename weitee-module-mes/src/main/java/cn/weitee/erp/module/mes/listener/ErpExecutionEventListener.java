package cn.weitee.erp.module.mes.listener;

import cn.hutool.core.collection.CollUtil;
import cn.weitee.erp.module.erp.framework.event.ErpProductionOrderStepFinishedEvent;
import cn.weitee.erp.module.erp.framework.event.ErpProductionReportCreatedEvent;
import cn.weitee.erp.module.mes.dal.mysql.MesWorkTaskMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;

/**
 * ERP 执行事件监听：回写工序任务的实际时间与状态。
 * <ul>
 *   <li>报工创建 → 任务 actual_start（首次）+ 状态进行中</li>
 *   <li>工序完工 → 任务 actual_end + 状态已完成</li>
 * </ul>
 */
@Component
public class ErpExecutionEventListener {

    private static final Logger log = LoggerFactory.getLogger(ErpExecutionEventListener.class);

    @Resource
    private MesWorkTaskMapper mesWorkTaskMapper;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onReportCreated(ErpProductionReportCreatedEvent event) {
        if (CollUtil.isEmpty(event.getOrderStepIds())) {
            return;
        }
        try {
            for (Long orderStepId : event.getOrderStepIds()) {
                mesWorkTaskMapper.markActualStart(orderStepId, event.getReportTime());
            }
        } catch (Exception e) {
            log.error("[onReportCreated][报工回写任务实际时间失败 orderId={}]", event.getProductionOrderId(), e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onStepFinished(ErpProductionOrderStepFinishedEvent event) {
        try {
            mesWorkTaskMapper.markActualEnd(event.getOrderStepId(), LocalDateTime.now());
        } catch (Exception e) {
            log.error("[onStepFinished][工序完工回写任务失败 orderStepId={}]", event.getOrderStepId(), e);
        }
    }

}
