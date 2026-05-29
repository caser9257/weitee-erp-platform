package cn.iocoder.yudao.module.erp.service.finance.listener;

import cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEventListener;
import cn.iocoder.yudao.module.erp.enums.ErpFinanceExpenseBpmConstants;
import cn.iocoder.yudao.module.erp.service.finance.ErpFinanceExpenseBpmService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class ErpFinanceExpenseBpmStatusListener extends BpmProcessInstanceStatusEventListener {

    @Resource
    private ErpFinanceExpenseBpmService financeExpenseBpmService;

    @Override
    protected String getProcessDefinitionKey() {
        return ErpFinanceExpenseBpmConstants.PROCESS_DEFINITION_KEY;
    }

    @Override
    protected void onEvent(BpmProcessInstanceStatusEvent event) {
        financeExpenseBpmService.handleProcessInstanceResult(Long.parseLong(event.getBusinessKey()),
                event.getId(), event.getStatus(), event.getReason());
    }

}
