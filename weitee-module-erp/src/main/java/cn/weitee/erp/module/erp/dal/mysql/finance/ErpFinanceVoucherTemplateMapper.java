package cn.weitee.erp.module.erp.dal.mysql.finance;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.voucher.ErpFinanceVoucherTemplatePageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceVoucherTemplateDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ErpFinanceVoucherTemplateMapper extends BaseMapperX<ErpFinanceVoucherTemplateDO> {

    default PageResult<ErpFinanceVoucherTemplateDO> selectPage(ErpFinanceVoucherTemplatePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpFinanceVoucherTemplateDO>()
                .eqIfPresent(ErpFinanceVoucherTemplateDO::getLedgerId, reqVO.getLedgerId())
                .eqIfPresent(ErpFinanceVoucherTemplateDO::getBizType, reqVO.getBizType())
                .likeIfPresent(ErpFinanceVoucherTemplateDO::getName, reqVO.getName())
                .eqIfPresent(ErpFinanceVoucherTemplateDO::getStatus, reqVO.getStatus())
                .eqIfPresent(ErpFinanceVoucherTemplateDO::getAutoGenerate, reqVO.getAutoGenerate())
                .orderByDesc(ErpFinanceVoucherTemplateDO::getId));
    }

    default PageResult<ErpFinanceVoucherTemplateDO> selectPageByVisibleLedgerIds(ErpFinanceVoucherTemplatePageReqVO reqVO, List<Long> ledgerIds) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpFinanceVoucherTemplateDO>()
                .in(ErpFinanceVoucherTemplateDO::getLedgerId, ledgerIds)
                .eqIfPresent(ErpFinanceVoucherTemplateDO::getLedgerId, reqVO.getLedgerId())
                .eqIfPresent(ErpFinanceVoucherTemplateDO::getBizType, reqVO.getBizType())
                .likeIfPresent(ErpFinanceVoucherTemplateDO::getName, reqVO.getName())
                .eqIfPresent(ErpFinanceVoucherTemplateDO::getStatus, reqVO.getStatus())
                .eqIfPresent(ErpFinanceVoucherTemplateDO::getAutoGenerate, reqVO.getAutoGenerate())
                .orderByDesc(ErpFinanceVoucherTemplateDO::getId));
    }

    default List<ErpFinanceVoucherTemplateDO> selectListByLedgerIdAndBizType(Long ledgerId, Integer bizType) {
        return selectList(new LambdaQueryWrapperX<ErpFinanceVoucherTemplateDO>()
                .eq(ErpFinanceVoucherTemplateDO::getLedgerId, ledgerId)
                .eq(ErpFinanceVoucherTemplateDO::getBizType, bizType)
                .orderByDesc(ErpFinanceVoucherTemplateDO::getId));
    }
}
