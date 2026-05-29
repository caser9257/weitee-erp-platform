package cn.iocoder.yudao.module.erp.dal.mysql.finance;

import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceDualLedgerAmountDiffLogDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collections;
import java.util.List;

/**
 * ERP 双账套金额差异计算日志 Mapper
 */
@Mapper
public interface ErpFinanceDualLedgerAmountDiffLogMapper extends BaseMapperX<ErpFinanceDualLedgerAmountDiffLogDO> {

    default List<ErpFinanceDualLedgerAmountDiffLogDO> selectListByBizTypeAndBizId(Integer bizType, Long bizId) {
        List<ErpFinanceDualLedgerAmountDiffLogDO> list = selectList(new LambdaQueryWrapperX<ErpFinanceDualLedgerAmountDiffLogDO>()
                .eq(ErpFinanceDualLedgerAmountDiffLogDO::getBizType, bizType)
                .eq(ErpFinanceDualLedgerAmountDiffLogDO::getBizId, bizId)
                .orderByAsc(ErpFinanceDualLedgerAmountDiffLogDO::getDiffItemType)
                .orderByAsc(ErpFinanceDualLedgerAmountDiffLogDO::getId));
        return list == null ? Collections.emptyList() : list;
    }

    default void deleteByBizTypeAndBizId(Integer bizType, Long bizId) {
        delete(new LambdaQueryWrapperX<ErpFinanceDualLedgerAmountDiffLogDO>()
                .eq(ErpFinanceDualLedgerAmountDiffLogDO::getBizType, bizType)
                .eq(ErpFinanceDualLedgerAmountDiffLogDO::getBizId, bizId));
    }

}
