package cn.weitee.erp.module.erp.dal.mysql.finance;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.framework.mybatis.core.util.MyBatisUtils;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.subject.ErpFinanceSubjectPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceSubjectDO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mapper
public interface ErpFinanceSubjectMapper extends BaseMapperX<ErpFinanceSubjectDO> {

    default PageResult<ErpFinanceSubjectDO> selectPage(ErpFinanceSubjectPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpFinanceSubjectDO>()
                .eqIfPresent(ErpFinanceSubjectDO::getLedgerId, reqVO.getLedgerId())
                .eqIfPresent(ErpFinanceSubjectDO::getParentId, reqVO.getParentId())
                .likeIfPresent(ErpFinanceSubjectDO::getSubjectCode, reqVO.getSubjectCode())
                .likeIfPresent(ErpFinanceSubjectDO::getSubjectName, reqVO.getSubjectName())
                .eqIfPresent(ErpFinanceSubjectDO::getSubjectType, reqVO.getSubjectType())
                .eqIfPresent(ErpFinanceSubjectDO::getStatus, reqVO.getStatus())
                .orderByAsc(ErpFinanceSubjectDO::getSort)
                .orderByAsc(ErpFinanceSubjectDO::getSubjectCode)
                .orderByAsc(ErpFinanceSubjectDO::getId));
    }

    default PageResult<ErpFinanceSubjectDO> selectPageByVisibleLedgerIds(ErpFinanceSubjectPageReqVO reqVO, List<Long> ledgerIds) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpFinanceSubjectDO>()
                .in(ErpFinanceSubjectDO::getLedgerId, ledgerIds)
                .eqIfPresent(ErpFinanceSubjectDO::getLedgerId, reqVO.getLedgerId())
                .eqIfPresent(ErpFinanceSubjectDO::getParentId, reqVO.getParentId())
                .likeIfPresent(ErpFinanceSubjectDO::getSubjectCode, reqVO.getSubjectCode())
                .likeIfPresent(ErpFinanceSubjectDO::getSubjectName, reqVO.getSubjectName())
                .eqIfPresent(ErpFinanceSubjectDO::getSubjectType, reqVO.getSubjectType())
                .eqIfPresent(ErpFinanceSubjectDO::getStatus, reqVO.getStatus())
                .orderByAsc(ErpFinanceSubjectDO::getSort)
                .orderByAsc(ErpFinanceSubjectDO::getSubjectCode)
                .orderByAsc(ErpFinanceSubjectDO::getId));
    }

    default PageResult<ErpFinanceSubjectDO> selectPageByVisibleLedgerIdsAndSubjectCodes(
            ErpFinanceSubjectPageReqVO reqVO, Collection<Long> ledgerIds,
            Map<Long, Set<String>> subjectCodesByLedger) {
        Page<ErpFinanceSubjectDO> page = MyBatisUtils.buildPage(reqVO);
        selectPageByVisibleLedgerIdsAndSubjectCodes(page, reqVO, ledgerIds, subjectCodesByLedger);
        return new PageResult<>(page.getRecords(), page.getTotal());
    }

    Page<ErpFinanceSubjectDO> selectPageByVisibleLedgerIdsAndSubjectCodes(
            Page<ErpFinanceSubjectDO> page, @Param("reqVO") ErpFinanceSubjectPageReqVO reqVO,
            @Param("ledgerIds") Collection<Long> ledgerIds,
            @Param("subjectCodesByLedger") Map<Long, Set<String>> subjectCodesByLedger);

    default ErpFinanceSubjectDO selectByLedgerIdAndSubjectCode(Long ledgerId, String subjectCode) {
        return selectOne(new LambdaQueryWrapperX<ErpFinanceSubjectDO>()
                .eq(ErpFinanceSubjectDO::getLedgerId, ledgerId)
                .eq(ErpFinanceSubjectDO::getSubjectCode, subjectCode));
    }

    default List<ErpFinanceSubjectDO> selectListByLedgerIdAndSubjectCodes(Long ledgerId, Collection<String> subjectCodes) {
        if (subjectCodes == null || subjectCodes.isEmpty()) {
            return Collections.emptyList();
        }
        List<ErpFinanceSubjectDO> list = selectList(new LambdaQueryWrapperX<ErpFinanceSubjectDO>()
                .eq(ErpFinanceSubjectDO::getLedgerId, ledgerId)
                .in(ErpFinanceSubjectDO::getSubjectCode, subjectCodes)
                .orderByAsc(ErpFinanceSubjectDO::getSubjectCode));
        return list == null ? Collections.emptyList() : list;
    }

    default List<ErpFinanceSubjectDO> selectListByLedgerId(Long ledgerId) {
        List<ErpFinanceSubjectDO> list = selectList(new LambdaQueryWrapperX<ErpFinanceSubjectDO>()
                .eq(ErpFinanceSubjectDO::getLedgerId, ledgerId)
                .orderByAsc(ErpFinanceSubjectDO::getSort)
                .orderByAsc(ErpFinanceSubjectDO::getSubjectCode));
        return list == null ? Collections.emptyList() : list;
    }

}
