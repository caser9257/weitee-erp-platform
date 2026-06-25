package cn.weitee.erp.module.erp.service.finance.listener;

import cn.weitee.erp.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import cn.weitee.erp.module.bpm.api.event.BpmProcessInstanceStatusEventListener;
import cn.weitee.erp.module.erp.enums.ErpFinancePaymentBpmConstants;
import cn.weitee.erp.module.erp.service.finance.ErpFinancePaymentBpmService;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * @deprecated 已迁移到 BpmApprovalEventDispatcher + FinancePaymentResultHandler 模式
 * 保留此类仅为兼容旧 BPM 实例，新流程不再使用
 */
@Deprecated
public class ErpFinancePaymentBpmStatusListener extends BpmProcessInstanceStatusEventListener {

    @Resource
    private ErpFinancePaymentBpmService financePaymentBpmService;

    @Override
    protected String getProcessDefinitionKey() {
        return ErpFinancePaymentBpmConstants.PROCESS_DEFINITION_KEY;
    }

    @Override
    protected void onEvent(BpmProcessInstanceStatusEvent event) {
        financePaymentBpmService.handleProcessInstanceResult(Long.parseLong(event.getBusinessKey()),
                event.getId(), event.getStatus(), event.getReason());
    }

}
