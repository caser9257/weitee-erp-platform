package cn.weitee.erp.module.erp.dal.mysql.finance;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.framework.mybatis.core.util.MyBatisUtils;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.report.ErpFinanceReportReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.reportitem.ErpFinanceReportItemPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceReportItemDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mapper
public interface ErpFinanceReportItemMapper extends BaseMapperX<ErpFinanceReportItemDO> {

    default PageResult<ErpFinanceReportItemDO> selectPage(ErpFinanceReportItemPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpFinanceReportItemDO>()
                .eqIfPresent(ErpFinanceReportItemDO::getLedgerId, reqVO.getLedgerId())
                .eqIfPresent(ErpFinanceReportItemDO::getReportType, reqVO.getReportType())
                .eqIfPresent(ErpFinanceReportItemDO::getItemCategory, reqVO.getItemCategory())
                .likeIfPresent(ErpFinanceReportItemDO::getItemCode, reqVO.getItemCode())
                .likeIfPresent(ErpFinanceReportItemDO::getItemName, reqVO.getItemName())
                .eqIfPresent(ErpFinanceReportItemDO::getStatus, reqVO.getStatus())
                .orderByAsc(ErpFinanceReportItemDO::getSort)
                .orderByAsc(ErpFinanceReportItemDO::getId));
    }

    default PageResult<ErpFinanceReportItemDO> selectPageByVisibleLedgerIds(ErpFinanceReportItemPageReqVO reqVO, List<Long> ledgerIds) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpFinanceReportItemDO>()
                .in(ErpFinanceReportItemDO::getLedgerId, ledgerIds)
                .eqIfPresent(ErpFinanceReportItemDO::getLedgerId, reqVO.getLedgerId())
                .eqIfPresent(ErpFinanceReportItemDO::getReportType, reqVO.getReportType())
                .eqIfPresent(ErpFinanceReportItemDO::getItemCategory, reqVO.getItemCategory())
                .likeIfPresent(ErpFinanceReportItemDO::getItemCode, reqVO.getItemCode())
                .likeIfPresent(ErpFinanceReportItemDO::getItemName, reqVO.getItemName())
                .eqIfPresent(ErpFinanceReportItemDO::getStatus, reqVO.getStatus())
                .orderByAsc(ErpFinanceReportItemDO::getSort)
                .orderByAsc(ErpFinanceReportItemDO::getId));
    }

    default PageResult<ErpFinanceReportItemDO> selectPageByVisibleLedgerIdsAndSubjectCodes(
            ErpFinanceReportItemPageReqVO reqVO, Collection<Long> ledgerIds,
            Map<Long, Set<String>> subjectCodesByLedger) {
        Page<ErpFinanceReportItemDO> page = MyBatisUtils.buildPage(reqVO);
        selectPageByVisibleLedgerIdsAndSubjectCodes(page, reqVO, ledgerIds, subjectCodesByLedger);
        return new PageResult<>(page.getRecords(), page.getTotal());
    }

    Page<ErpFinanceReportItemDO> selectPageByVisibleLedgerIdsAndSubjectCodes(
            Page<ErpFinanceReportItemDO> page, @Param("reqVO") ErpFinanceReportItemPageReqVO reqVO,
            @Param("ledgerIds") Collection<Long> ledgerIds,
            @Param("subjectCodesByLedger") Map<Long, Set<String>> subjectCodesByLedger);

    default ErpFinanceReportItemDO selectByLedgerIdAndReportTypeAndItemCode(Long ledgerId, Integer reportType, String itemCode) {
        return selectOne(new LambdaQueryWrapperX<ErpFinanceReportItemDO>()
                .eq(ErpFinanceReportItemDO::getLedgerId, ledgerId)
                .eq(ErpFinanceReportItemDO::getReportType, reportType)
                .eq(ErpFinanceReportItemDO::getItemCode, itemCode));
    }

    default List<ErpFinanceReportItemDO> selectListByReportReq(ErpFinanceReportReqVO reqVO, Integer reportType) {
        List<ErpFinanceReportItemDO> list = selectList(new LambdaQueryWrapperX<ErpFinanceReportItemDO>()
                .eq(ErpFinanceReportItemDO::getLedgerId, reqVO.getLedgerId())
                .eq(ErpFinanceReportItemDO::getReportType, reportType)
                .eq(ErpFinanceReportItemDO::getStatus, cn.weitee.erp.framework.common.enums.CommonStatusEnum.ENABLE.getStatus())
                .orderByAsc(ErpFinanceReportItemDO::getSort)
                .orderByAsc(ErpFinanceReportItemDO::getId));
        return list == null ? Collections.emptyList() : list;
    }

}
