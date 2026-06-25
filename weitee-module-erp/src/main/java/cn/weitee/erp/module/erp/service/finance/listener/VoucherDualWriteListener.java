package cn.weitee.erp.module.erp.service.finance.listener;

import cn.weitee.erp.module.erp.service.finance.ErpFinanceDualWriteService;
import cn.weitee.erp.module.erp.service.finance.event.VoucherCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * 凭证双写监听器
 * 监听凭证创建事件，触发双写逻辑
 */
@Slf4j
@Component
public class VoucherDualWriteListener {

    @Resource
    private ErpFinanceDualWriteService dualWriteService;

    @Async
    @EventListener
    public void onVoucherCreated(VoucherCreatedEvent event) {
        log.info("收到凭证创建事件，开始双写处理。voucherId={}, sourceLedgerId={}",
                event.getVoucher().getId(), event.getSourceLedgerId());
        try {
            dualWriteService.dualWriteVoucher(event.getVoucher(), event.getEntries(), event.getSourceLedgerId());
            log.info("双写处理完成。voucherId={}", event.getVoucher().getId());
        } catch (Exception e) {
            log.error("双写处理失败。voucherId={}", event.getVoucher().getId(), e);
        }
    }
}
