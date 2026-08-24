package cn.weitee.erp.module.mes.listener;

import cn.weitee.erp.module.erp.framework.event.ErpProductionOrderReleasedEvent;
import cn.weitee.erp.module.mes.service.MesWorkTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import jakarta.annotation.Resource;

@Component
public class ErpProductionOrderReleasedListener {

    private static final Logger log = LoggerFactory.getLogger(ErpProductionOrderReleasedListener.class);

    @Resource
    private MesWorkTaskService mesWorkTaskService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onProductionOrderReleased(ErpProductionOrderReleasedEvent event) {
        try {
            mesWorkTaskService.createTasksByOrderReleased(event.getProductionOrderId());
        } catch (Exception e) {
            // MES 任务生成失败不影响 ERP 主流程，仅告警，支持"重新生成"重试
            log.error("[onProductionOrderReleased][工单下达后生成工序任务失败 orderId={}]",
                    event.getProductionOrderId(), e);
        }
    }

}
