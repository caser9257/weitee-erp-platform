package cn.iocoder.yudao.module.erp.dal.mysql.finance;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.report.ErpFinanceReportReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.reportitem.ErpFinanceReportItemPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceReportItemDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collections;
import java.util.List;

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
                .eq(ErpFinanceReportItemDO::getStatus, cn.iocoder.yudao.framework.common.enums.CommonStatusEnum.ENABLE.getStatus())
                .orderByAsc(ErpFinanceReportItemDO::getSort)
                .orderByAsc(ErpFinanceReportItemDO::getId));
        return list == null ? Collections.emptyList() : list;
    }

}
