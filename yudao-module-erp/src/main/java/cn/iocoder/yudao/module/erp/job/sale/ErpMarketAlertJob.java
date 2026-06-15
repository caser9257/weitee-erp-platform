package cn.iocoder.yudao.module.erp.job.sale;

import cn.iocoder.yudao.module.erp.service.sale.ErpMarketAlertService;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.MarketAlertVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 市场预警定时任务
 *
 * 每天凌晨 2:00 执行预警检查
 *
 * @author system
 */
@Component
@Slf4j
public class ErpMarketAlertJob {

    @Resource
    private ErpMarketAlertService erpMarketAlertService;

    /**
     * 每天凌晨 2:00 执行预警检查
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void executeAlertCheck() {
        log.info("[executeAlertCheck] 开始执行市场预警检查...");
        try {
            int alertCount = erpMarketAlertService.checkAndTriggerAlerts();
            log.info("[executeAlertCheck] 预警检查完成，发现 {} 条预警", alertCount);
        } catch (Exception e) {
            log.error("[executeAlertCheck] 预警检查执行异常", e);
        }
    }

}
