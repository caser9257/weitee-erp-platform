package cn.weitee.erp.module.erp.dal.mysql.finance;

import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinancePrepaymentAllocateDO;
import cn.weitee.erp.module.erp.enums.ErpFinancePrepaymentAllocateStatusEnum;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper
public interface ErpFinancePrepaymentAllocateMapper extends BaseMapperX<ErpFinancePrepaymentAllocateDO> {

    default List<ErpFinancePrepaymentAllocateDO> selectListByPrepaymentId(Long prepaymentId) {
        return selectList(ErpFinancePrepaymentAllocateDO::getPrepaymentId, prepaymentId);
    }

    default List<ErpFinancePrepaymentAllocateDO> selectListByPrepaymentIds(Collection<Long> prepaymentIds) {
        if (prepaymentIds == null || prepaymentIds.isEmpty()) {
            return Collections.emptyList();
        }
        return selectList(ErpFinancePrepaymentAllocateDO::getPrepaymentId, prepaymentIds);
    }

    default List<ErpFinancePrepaymentAllocateDO> selectApprovedListByPrepaymentId(Long prepaymentId) {
        return selectList(new LambdaQueryWrapperX<ErpFinancePrepaymentAllocateDO>()
                .eq(ErpFinancePrepaymentAllocateDO::getPrepaymentId, prepaymentId)
                .eq(ErpFinancePrepaymentAllocateDO::getStatus, ErpFinancePrepaymentAllocateStatusEnum.APPROVED.getStatus()));
    }

    default List<ErpFinancePrepaymentAllocateDO> selectApprovedListByPrepaymentIds(Collection<Long> prepaymentIds) {
        if (prepaymentIds == null || prepaymentIds.isEmpty()) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<ErpFinancePrepaymentAllocateDO>()
                .in(ErpFinancePrepaymentAllocateDO::getPrepaymentId, prepaymentIds)
                .eq(ErpFinancePrepaymentAllocateDO::getStatus, ErpFinancePrepaymentAllocateStatusEnum.APPROVED.getStatus()));
    }

    default List<ErpFinancePrepaymentAllocateDO> selectApprovedListByStatementIds(Collection<Long> statementIds) {
        if (statementIds == null || statementIds.isEmpty()) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<ErpFinancePrepaymentAllocateDO>()
                .in(ErpFinancePrepaymentAllocateDO::getApStatementId, statementIds)
                .eq(ErpFinancePrepaymentAllocateDO::getStatus, ErpFinancePrepaymentAllocateStatusEnum.APPROVED.getStatus()));
    }

    default Long selectCountByBizTypeAndBizIdAndStatus(Integer bizType, Long bizId, Integer status) {
        return selectCount(new LambdaQueryWrapperX<ErpFinancePrepaymentAllocateDO>()
                .eqIfPresent(ErpFinancePrepaymentAllocateDO::getBizType, bizType)
                .eqIfPresent(ErpFinancePrepaymentAllocateDO::getBizId, bizId)
                .eqIfPresent(ErpFinancePrepaymentAllocateDO::getStatus, status));
    }

}
