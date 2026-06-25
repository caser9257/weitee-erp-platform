package cn.weitee.erp.module.erp.service.statistics;

import cn.weitee.erp.module.erp.dal.mysql.statistics.ErpSaleStatisticsMapper;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ERP 销售统计 Service 实现类
 *
 * @author WeTai
 */
@Service
public class ErpSaleStatisticsServiceImpl implements ErpSaleStatisticsService {

    @Resource
    private ErpSaleStatisticsMapper erpSaleStatisticsMapper;

    @Override
    public BigDecimal getSalePrice(LocalDateTime beginTime, LocalDateTime endTime) {
        return erpSaleStatisticsMapper.getSalePrice(beginTime, endTime);
    }

}
