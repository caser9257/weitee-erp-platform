package cn.weitee.erp.module.erp.service.sale.listener;

import cn.weitee.erp.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import cn.weitee.erp.module.bpm.api.event.BpmProcessInstanceStatusEventListener;
import cn.weitee.erp.module.erp.enums.ErpSaleOrderBpmConstants;
import cn.weitee.erp.module.erp.service.sale.ErpSaleOrderBpmService;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * @deprecated 已迁移到 BpmApprovalEventDispatcher + SaleOrderResultHandler 模式
 * 保留此类仅为兼容旧 BPM 实例，新流程不再使用
 */
@Deprecated
public class ErpSaleOrderBpmStatusListener extends BpmProcessInstanceStatusEventListener {

    @Resource
    private ErpSaleOrderBpmService saleOrderBpmService;

    @Override
    protected String getProcessDefinitionKey() {
        return ErpSaleOrderBpmConstants.PROCESS_DEFINITION_KEY;
    }

    @Override
    protected void onEvent(BpmProcessInstanceStatusEvent event) {
        saleOrderBpmService.handleProcessInstanceResult(Long.parseLong(event.getBusinessKey()),
                event.getId(), event.getStatus(), event.getReason());
    }

}
