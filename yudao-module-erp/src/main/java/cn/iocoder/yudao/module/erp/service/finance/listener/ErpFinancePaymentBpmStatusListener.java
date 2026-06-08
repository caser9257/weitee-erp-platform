package cn.iocoder.yudao.module.erp.service.finance.listener;

import cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEventListener;
import cn.iocoder.yudao.module.erp.enums.ErpFinancePaymentBpmConstants;
import cn.iocoder.yudao.module.erp.service.finance.ErpFinancePaymentBpmService;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

@Component
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
