package cn.weitee.erp.module.erp.service.statistics;

import cn.weitee.erp.module.erp.dal.mysql.statistics.ErpPurchaseStatisticsMapper;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ERP 采购统计 Service 实现类
 *
 * @author WeTai
 */
@Service
public class ErpPurchaseStatisticsServiceImpl implements ErpPurchaseStatisticsService {

    @Resource
    private ErpPurchaseStatisticsMapper erpPurchaseStatisticsMapper;

    @Override
    public BigDecimal getPurchasePrice(LocalDateTime beginTime, LocalDateTime endTime) {
        return erpPurchaseStatisticsMapper.getPurchasePrice(beginTime, endTime);
    }

}
