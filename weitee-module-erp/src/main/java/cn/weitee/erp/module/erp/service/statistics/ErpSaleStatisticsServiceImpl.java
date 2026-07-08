package cn.weitee.erp.module.erp.service.statistics;

import cn.hutool.core.map.MapUtil;
import cn.weitee.erp.module.erp.dal.mysql.statistics.ErpSaleStatisticsMapper;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    @Override
    public Map<String, BigDecimal> getSalePriceMapByMonth(LocalDateTime beginTime, LocalDateTime endTime) {
        List<Map<String, Object>> rows = erpSaleStatisticsMapper.getSalePriceListByMonth(beginTime, endTime);
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            result.put(MapUtil.getStr(row, "ym"), toBigDecimal(row.get("price")));
        }
        return result;
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        return new BigDecimal(value.toString());
    }

}
