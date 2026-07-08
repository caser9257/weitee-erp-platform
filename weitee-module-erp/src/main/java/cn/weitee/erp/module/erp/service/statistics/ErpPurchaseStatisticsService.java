package cn.weitee.erp.module.erp.service.statistics;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * ERP 采购统计 Service 接口
 *
 * @author WeTai
 */
public interface ErpPurchaseStatisticsService {

    /**
     * 获得采购金额
     *
     * 计算逻辑：采购出库的金额 - 采购退货的金额
     *
     * @param beginTime >= 开始时间
     * @param endTime < 结束时间
     * @return 采购金额
     */
    BigDecimal getPurchasePrice(LocalDateTime beginTime, LocalDateTime endTime);

    Map<String, BigDecimal> getPurchasePriceMapByMonth(LocalDateTime beginTime, LocalDateTime endTime);

}
