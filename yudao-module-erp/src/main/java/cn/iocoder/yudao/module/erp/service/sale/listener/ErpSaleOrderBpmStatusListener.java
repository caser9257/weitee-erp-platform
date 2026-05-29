package cn.iocoder.yudao.module.erp.service.sale.listener;

import cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEventListener;
import cn.iocoder.yudao.module.erp.enums.ErpSaleOrderBpmConstants;
import cn.iocoder.yudao.module.erp.service.sale.ErpSaleOrderBpmService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
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
