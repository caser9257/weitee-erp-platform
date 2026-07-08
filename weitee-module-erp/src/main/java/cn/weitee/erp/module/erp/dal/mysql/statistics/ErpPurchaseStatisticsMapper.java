package cn.weitee.erp.module.erp.dal.mysql.statistics;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * ERP 采购统计 Mapper
 *
 * @author WeTai
 */
@Mapper
public interface ErpPurchaseStatisticsMapper {

    BigDecimal getPurchasePrice(@Param("beginTime") LocalDateTime beginTime,
                                @Param("endTime") LocalDateTime endTime);

    List<Map<String, Object>> getPurchasePriceListByMonth(@Param("beginTime") LocalDateTime beginTime,
                                                          @Param("endTime") LocalDateTime endTime);

}
