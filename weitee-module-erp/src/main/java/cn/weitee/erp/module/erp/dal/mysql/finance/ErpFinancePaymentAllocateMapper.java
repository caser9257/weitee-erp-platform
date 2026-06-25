package cn.weitee.erp.module.erp.dal.mysql.finance;

import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinancePaymentAllocateDO;
import cn.weitee.erp.module.erp.enums.ErpFinancePaymentAllocateStatusEnum;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper
public interface ErpFinancePaymentAllocateMapper extends BaseMapperX<ErpFinancePaymentAllocateDO> {

    default List<ErpFinancePaymentAllocateDO> selectListByPaymentId(Long paymentId) {
        return selectList(ErpFinancePaymentAllocateDO::getPaymentId, paymentId);
    }

    default List<ErpFinancePaymentAllocateDO> selectListByPaymentIds(Collection<Long> paymentIds) {
        if (paymentIds == null || paymentIds.isEmpty()) {
            return Collections.emptyList();
        }
        return selectList(ErpFinancePaymentAllocateDO::getPaymentId, paymentIds);
    }

    default List<ErpFinancePaymentAllocateDO> selectApprovedListByStatementIds(Collection<Long> statementIds) {
        if (statementIds == null || statementIds.isEmpty()) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<ErpFinancePaymentAllocateDO>()
                .inIfPresent(ErpFinancePaymentAllocateDO::getApStatementId, statementIds)
                .eq(ErpFinancePaymentAllocateDO::getStatus, ErpFinancePaymentAllocateStatusEnum.APPROVED.getStatus()));
    }

    default List<ErpFinancePaymentAllocateDO> selectListByStatementIds(Collection<Long> statementIds) {
        if (statementIds == null || statementIds.isEmpty()) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<ErpFinancePaymentAllocateDO>()
                .inIfPresent(ErpFinancePaymentAllocateDO::getApStatementId, statementIds)
                .orderByDesc(ErpFinancePaymentAllocateDO::getId));
    }

    default Long selectCountByBizTypeAndBizIdAndStatus(Integer bizType, Long bizId, Integer status) {
        return selectCount(new LambdaQueryWrapperX<ErpFinancePaymentAllocateDO>()
                .eqIfPresent(ErpFinancePaymentAllocateDO::getBizType, bizType)
                .eqIfPresent(ErpFinancePaymentAllocateDO::getBizId, bizId)
                .eqIfPresent(ErpFinancePaymentAllocateDO::getStatus, status));
    }

}
