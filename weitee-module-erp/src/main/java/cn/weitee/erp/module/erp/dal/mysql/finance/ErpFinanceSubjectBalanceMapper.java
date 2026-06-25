package cn.weitee.erp.module.erp.dal.mysql.finance;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.report.ErpFinanceReportReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.generalledger.ErpFinanceSubjectBalancePageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceSubjectBalanceDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collections;
import java.util.List;

@Mapper
public interface ErpFinanceSubjectBalanceMapper extends BaseMapperX<ErpFinanceSubjectBalanceDO> {

    default PageResult<ErpFinanceSubjectBalanceDO> selectPage(ErpFinanceSubjectBalancePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpFinanceSubjectBalanceDO>()
                .eq(ErpFinanceSubjectBalanceDO::getLedgerId, reqVO.getLedgerId())
                .eq(ErpFinanceSubjectBalanceDO::getPeriodId, reqVO.getPeriodId())
                .likeIfPresent(ErpFinanceSubjectBalanceDO::getSubjectCode, reqVO.getSubjectCode())
                .likeIfPresent(ErpFinanceSubjectBalanceDO::getSubjectName, reqVO.getSubjectName())
                .orderByAsc(ErpFinanceSubjectBalanceDO::getSubjectCode)
                .orderByAsc(ErpFinanceSubjectBalanceDO::getId));
    }

    default ErpFinanceSubjectBalanceDO selectByLedgerIdAndPeriodIdAndSubjectCode(Long ledgerId, Long periodId, String subjectCode) {
        return selectOne(new LambdaQueryWrapperX<ErpFinanceSubjectBalanceDO>()
                .eq(ErpFinanceSubjectBalanceDO::getLedgerId, ledgerId)
                .eq(ErpFinanceSubjectBalanceDO::getPeriodId, periodId)
                .eq(ErpFinanceSubjectBalanceDO::getSubjectCode, subjectCode));
    }

    default ErpFinanceSubjectBalanceDO selectLatestBeforePeriod(Long ledgerId, String subjectCode, Integer periodSort) {
        return selectOne(new LambdaQueryWrapperX<ErpFinanceSubjectBalanceDO>()
                .eq(ErpFinanceSubjectBalanceDO::getLedgerId, ledgerId)
                .eq(ErpFinanceSubjectBalanceDO::getSubjectCode, subjectCode)
                .lt(ErpFinanceSubjectBalanceDO::getPeriodSort, periodSort)
                .orderByDesc(ErpFinanceSubjectBalanceDO::getPeriodSort)
                .last("LIMIT 1"));
    }

    default List<ErpFinanceSubjectBalanceDO> selectListAfterPeriod(Long ledgerId, String subjectCode, Integer periodSort) {
        List<ErpFinanceSubjectBalanceDO> list = selectList(new LambdaQueryWrapperX<ErpFinanceSubjectBalanceDO>()
                .eq(ErpFinanceSubjectBalanceDO::getLedgerId, ledgerId)
                .eq(ErpFinanceSubjectBalanceDO::getSubjectCode, subjectCode)
                .gt(ErpFinanceSubjectBalanceDO::getPeriodSort, periodSort)
                .orderByAsc(ErpFinanceSubjectBalanceDO::getPeriodSort)
                .orderByAsc(ErpFinanceSubjectBalanceDO::getId));
        return list == null ? Collections.emptyList() : list;
    }

    default List<ErpFinanceSubjectBalanceDO> selectListByReportReq(ErpFinanceReportReqVO reqVO) {
        List<ErpFinanceSubjectBalanceDO> list = selectList(new LambdaQueryWrapperX<ErpFinanceSubjectBalanceDO>()
                .eq(ErpFinanceSubjectBalanceDO::getLedgerId, reqVO.getLedgerId())
                .eq(ErpFinanceSubjectBalanceDO::getPeriodId, reqVO.getPeriodId())
                .likeIfPresent(ErpFinanceSubjectBalanceDO::getSubjectCode, reqVO.getSubjectCode())
                .likeIfPresent(ErpFinanceSubjectBalanceDO::getSubjectName, reqVO.getSubjectName())
                .orderByAsc(ErpFinanceSubjectBalanceDO::getSubjectCode)
                .orderByAsc(ErpFinanceSubjectBalanceDO::getId));
        return list == null ? Collections.emptyList() : list;
    }

    default List<ErpFinanceSubjectBalanceDO> selectListByLedgerIdAndPeriodId(Long ledgerId, Long periodId) {
        List<ErpFinanceSubjectBalanceDO> list = selectList(new LambdaQueryWrapperX<ErpFinanceSubjectBalanceDO>()
                .eq(ErpFinanceSubjectBalanceDO::getLedgerId, ledgerId)
                .eq(ErpFinanceSubjectBalanceDO::getPeriodId, periodId)
                .orderByAsc(ErpFinanceSubjectBalanceDO::getSubjectCode)
                .orderByAsc(ErpFinanceSubjectBalanceDO::getId));
        return list == null ? Collections.emptyList() : list;
    }

    default int deleteByLedgerId(Long ledgerId) {
        return delete(new LambdaQueryWrapper<ErpFinanceSubjectBalanceDO>()
                .eq(ErpFinanceSubjectBalanceDO::getLedgerId, ledgerId));
    }

}
