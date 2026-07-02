package cn.weitee.erp.module.erp.dal.mysql.sale;

import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpMarketAlertRecordDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * 市场预警记录 Mapper
 *
 * @author system
 */
@Mapper
public interface ErpMarketAlertRecordMapper extends BaseMapperX<ErpMarketAlertRecordDO> {

    /**
     * 查询指定触发日期、指定订单集合下已存在的预警记录（用于批量去重，避免循环内逐条 count）
     *
     * @param triggerDate 触发日期
     * @param orderIds    订单编号集合
     * @return 已存在的预警记录（仅含 ruleCode、orderId 两列）
     */
    default List<ErpMarketAlertRecordDO> selectListByTriggerDateAndOrderIds(LocalDate triggerDate,
                                                                            java.util.Collection<Long> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<ErpMarketAlertRecordDO>()
                .select(ErpMarketAlertRecordDO::getRuleCode, ErpMarketAlertRecordDO::getOrderId)
                .eq(ErpMarketAlertRecordDO::getTriggerDate, triggerDate)
                .in(ErpMarketAlertRecordDO::getOrderId, orderIds));
    }

}
