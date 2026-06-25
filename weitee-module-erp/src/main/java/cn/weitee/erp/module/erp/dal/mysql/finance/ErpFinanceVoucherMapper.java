package cn.weitee.erp.module.erp.dal.mysql.finance;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.voucher.ErpFinanceVoucherPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceVoucherDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collections;
import java.util.List;

@Mapper
public interface ErpFinanceVoucherMapper extends BaseMapperX<ErpFinanceVoucherDO> {

    default PageResult<ErpFinanceVoucherDO> selectPage(ErpFinanceVoucherPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpFinanceVoucherDO>()
                .eqIfPresent(ErpFinanceVoucherDO::getLedgerId, reqVO.getLedgerId())
                .eqIfPresent(ErpFinanceVoucherDO::getPeriodId, reqVO.getPeriodId())
                .eqIfPresent(ErpFinanceVoucherDO::getBizType, reqVO.getBizType())
                .likeIfPresent(ErpFinanceVoucherDO::getBizNo, reqVO.getBizNo())
                .likeIfPresent(ErpFinanceVoucherDO::getVoucherNo, reqVO.getVoucherNo())
                .eqIfPresent(ErpFinanceVoucherDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(ErpFinanceVoucherDO::getVoucherTime, reqVO.getVoucherTime())
                .orderByDesc(ErpFinanceVoucherDO::getId));
    }

    default ErpFinanceVoucherDO selectByLedgerIdAndBiz(Long ledgerId, Integer bizType, Long bizId) {
        return selectOne(new LambdaQueryWrapperX<ErpFinanceVoucherDO>()
                .eq(ErpFinanceVoucherDO::getLedgerId, ledgerId)
                .eq(ErpFinanceVoucherDO::getBizType, bizType)
                .eq(ErpFinanceVoucherDO::getBizId, bizId));
    }

    default List<ErpFinanceVoucherDO> selectPostedListByLedgerIdAndPeriodId(Long ledgerId, Long periodId) {
        List<ErpFinanceVoucherDO> list = selectList(new LambdaQueryWrapperX<ErpFinanceVoucherDO>()
                .eq(ErpFinanceVoucherDO::getLedgerId, ledgerId)
                .eq(ErpFinanceVoucherDO::getPeriodId, periodId)
                .isNotNull(ErpFinanceVoucherDO::getPostTime)
                .orderByAsc(ErpFinanceVoucherDO::getVoucherTime)
                .orderByAsc(ErpFinanceVoucherDO::getId));
        return list == null ? Collections.emptyList() : list;
    }

    default List<ErpFinanceVoucherDO> selectPostedListByLedgerId(Long ledgerId) {
        List<ErpFinanceVoucherDO> list = selectList(new LambdaQueryWrapperX<ErpFinanceVoucherDO>()
                .eq(ErpFinanceVoucherDO::getLedgerId, ledgerId)
                .isNotNull(ErpFinanceVoucherDO::getPostTime)
                .orderByAsc(ErpFinanceVoucherDO::getPeriodId)
                .orderByAsc(ErpFinanceVoucherDO::getVoucherTime)
                .orderByAsc(ErpFinanceVoucherDO::getId));
        return list == null ? Collections.emptyList() : list;
    }

    /**
     * 按可见账簿ID列表查询凭证分页
     */
    default PageResult<ErpFinanceVoucherDO> selectPageByVisibleLedgerIds(ErpFinanceVoucherPageReqVO reqVO,
                                                                          List<Long> visibleLedgerIds) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpFinanceVoucherDO>()
                .inIfPresent(ErpFinanceVoucherDO::getLedgerId, visibleLedgerIds)
                .eqIfPresent(ErpFinanceVoucherDO::getLedgerId, reqVO.getLedgerId())
                .eqIfPresent(ErpFinanceVoucherDO::getPeriodId, reqVO.getPeriodId())
                .eqIfPresent(ErpFinanceVoucherDO::getBizType, reqVO.getBizType())
                .likeIfPresent(ErpFinanceVoucherDO::getBizNo, reqVO.getBizNo())
                .likeIfPresent(ErpFinanceVoucherDO::getVoucherNo, reqVO.getVoucherNo())
                .eqIfPresent(ErpFinanceVoucherDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(ErpFinanceVoucherDO::getVoucherTime, reqVO.getVoucherTime())
                .orderByDesc(ErpFinanceVoucherDO::getId));
    }

    /**
     * 按可见账簿ID列表查询凭证列表
     */
    default List<ErpFinanceVoucherDO> selectListByVisibleLedgerIds(List<Long> visibleLedgerIds) {
        List<ErpFinanceVoucherDO> list = selectList(new LambdaQueryWrapperX<ErpFinanceVoucherDO>()
                .in(ErpFinanceVoucherDO::getLedgerId, visibleLedgerIds)
                .orderByAsc(ErpFinanceVoucherDO::getVoucherTime)
                .orderByAsc(ErpFinanceVoucherDO::getId));
        return list == null ? Collections.emptyList() : list;
    }

    default int clearBizIdById(Long id) {
        return update(null, new LambdaUpdateWrapper<ErpFinanceVoucherDO>()
                .eq(ErpFinanceVoucherDO::getId, id)
                .set(ErpFinanceVoucherDO::getBizId, null));
    }
}
