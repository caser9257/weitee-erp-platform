package cn.weitee.erp.module.erp.dal.mysql.finance;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.period.ErpFinancePeriodPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinancePeriodDO;
import cn.weitee.erp.module.erp.enums.ErpFinancePeriodStatusEnum;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ErpFinancePeriodMapper extends BaseMapperX<ErpFinancePeriodDO> {

    default PageResult<ErpFinancePeriodDO> selectPage(ErpFinancePeriodPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpFinancePeriodDO>()
                .eqIfPresent(ErpFinancePeriodDO::getLedgerId, reqVO.getLedgerId())
                .likeIfPresent(ErpFinancePeriodDO::getPeriodCode, reqVO.getPeriodCode())
                .eqIfPresent(ErpFinancePeriodDO::getPeriodYear, reqVO.getPeriodYear())
                .eqIfPresent(ErpFinancePeriodDO::getPeriodMonth, reqVO.getPeriodMonth())
                .eqIfPresent(ErpFinancePeriodDO::getStatus, reqVO.getStatus())
                .orderByDesc(ErpFinancePeriodDO::getPeriodSort)
                .orderByDesc(ErpFinancePeriodDO::getId));
    }

    default ErpFinancePeriodDO selectByLedgerIdAndPeriodSort(Long ledgerId, Integer periodSort) {
        return selectOne(new LambdaQueryWrapperX<ErpFinancePeriodDO>()
                .eq(ErpFinancePeriodDO::getLedgerId, ledgerId)
                .eq(ErpFinancePeriodDO::getPeriodSort, periodSort));
    }

    default List<ErpFinancePeriodDO> selectListByLedgerIdAndYear(Long ledgerId, Integer periodYear) {
        return selectList(new LambdaQueryWrapperX<ErpFinancePeriodDO>()
                .eq(ErpFinancePeriodDO::getLedgerId, ledgerId)
                .eq(ErpFinancePeriodDO::getPeriodYear, periodYear)
                .orderByDesc(ErpFinancePeriodDO::getPeriodSort));
    }

    default Long selectCountByLedgerId(Long ledgerId) {
        return selectCount(ErpFinancePeriodDO::getLedgerId, ledgerId);
    }

    default Long selectEarlierOpenCount(Long ledgerId, Integer periodSort) {
        return selectCount(new LambdaQueryWrapperX<ErpFinancePeriodDO>()
                .eq(ErpFinancePeriodDO::getLedgerId, ledgerId)
                .lt(ErpFinancePeriodDO::getPeriodSort, periodSort)
                .eq(ErpFinancePeriodDO::getStatus, ErpFinancePeriodStatusEnum.OPEN.getStatus()));
    }

    default Long selectLaterClosedCount(Long ledgerId, Integer periodSort) {
        return selectCount(new LambdaQueryWrapperX<ErpFinancePeriodDO>()
                .eq(ErpFinancePeriodDO::getLedgerId, ledgerId)
                .gt(ErpFinancePeriodDO::getPeriodSort, periodSort)
                .eq(ErpFinancePeriodDO::getStatus, ErpFinancePeriodStatusEnum.CLOSED.getStatus()));
    }

    default ErpFinancePeriodDO selectCurrentOpenByDate(Long ledgerId, LocalDate bizDate) {
        return selectOne(new LambdaQueryWrapperX<ErpFinancePeriodDO>()
                .eq(ErpFinancePeriodDO::getLedgerId, ledgerId)
                .eq(ErpFinancePeriodDO::getStatus, ErpFinancePeriodStatusEnum.OPEN.getStatus())
                .le(ErpFinancePeriodDO::getStartDate, bizDate)
                .ge(ErpFinancePeriodDO::getEndDate, bizDate)
                .orderByDesc(ErpFinancePeriodDO::getPeriodSort)
                .last("LIMIT 1"));
    }
}
