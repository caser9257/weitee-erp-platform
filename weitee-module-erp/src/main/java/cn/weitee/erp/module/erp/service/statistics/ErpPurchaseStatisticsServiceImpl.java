package cn.weitee.erp.module.erp.service.statistics;

import cn.hutool.core.map.MapUtil;
import cn.weitee.erp.module.erp.dal.mysql.statistics.ErpPurchaseStatisticsMapper;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    @Override
    public Map<String, BigDecimal> getPurchasePriceMapByMonth(LocalDateTime beginTime, LocalDateTime endTime) {
        List<Map<String, Object>> rows = erpPurchaseStatisticsMapper.getPurchasePriceListByMonth(beginTime, endTime);
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
