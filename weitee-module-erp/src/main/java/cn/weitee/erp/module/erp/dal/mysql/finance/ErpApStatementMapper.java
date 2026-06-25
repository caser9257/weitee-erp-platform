package cn.weitee.erp.module.erp.dal.mysql.finance;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.apstatement.ErpApStatementAgingReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.apstatement.ErpApStatementAgingRespVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.apstatement.ErpApStatementPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.apstatement.ErpApStatementPaymentEnablePageReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.apstatement.ErpApStatementReconciliationReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.apstatement.ErpApStatementReconciliationRespVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.apstatement.ErpApStatementSummaryRespVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpApStatementDO;
import cn.weitee.erp.module.erp.enums.ErpApStatementStatusEnum;
import cn.weitee.erp.module.erp.enums.ErpFinancePrepaymentAllocateStatusEnum;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

@Mapper
public interface ErpApStatementMapper extends BaseMapperX<ErpApStatementDO> {

    default PageResult<ErpApStatementDO> selectPage(ErpApStatementPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpApStatementDO>()
                .likeIfPresent(ErpApStatementDO::getStatementNo, reqVO.getStatementNo())
                .eqIfPresent(ErpApStatementDO::getBizType, reqVO.getBizType())
                .likeIfPresent(ErpApStatementDO::getBizNo, reqVO.getBizNo())
                .eqIfPresent(ErpApStatementDO::getSupplierId, reqVO.getSupplierId())
                .eqIfPresent(ErpApStatementDO::getAccountId, reqVO.getAccountId())
                .eqIfPresent(ErpApStatementDO::getCurrencyCode, reqVO.getCurrencyCode())
                .eqIfPresent(ErpApStatementDO::getInvoiceStatus, reqVO.getInvoiceStatus())
                .eqIfPresent(ErpApStatementDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(ErpApStatementDO::getBizDate, reqVO.getBizDate())
                .betweenIfPresent(ErpApStatementDO::getDueDate, reqVO.getDueDate())
                .apply(buildExcludeFullyPrepaidSql())
                .orderByDesc(ErpApStatementDO::getId));
    }

    default PageResult<ErpApStatementDO> selectPaymentEnablePage(ErpApStatementPaymentEnablePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpApStatementDO>()
                .eqIfPresent(ErpApStatementDO::getSupplierId, reqVO.getSupplierId())
                .eqIfPresent(ErpApStatementDO::getBizType, reqVO.getBizType())
                .likeIfPresent(ErpApStatementDO::getStatementNo, reqVO.getStatementNo())
                .likeIfPresent(ErpApStatementDO::getBizNo, reqVO.getBizNo())
                .eqIfPresent(ErpApStatementDO::getAccountId, reqVO.getAccountId())
                .ne(ErpApStatementDO::getStatus, ErpApStatementStatusEnum.CLOSED.getStatus())
                .gt(ErpApStatementDO::getRemainAmount, BigDecimal.ZERO)
                .orderByDesc(ErpApStatementDO::getId));
    }

    default ErpApStatementDO selectByBizTypeAndBizId(Integer bizType, Long bizId) {
        return selectOne(ErpApStatementDO::getBizType, bizType, ErpApStatementDO::getBizId, bizId);
    }

    default List<ErpApStatementDO> selectListByBizTypeAndBizIds(Integer bizType, Collection<Long> bizIds) {
        return selectList(new LambdaQueryWrapperX<ErpApStatementDO>()
                .eq(ErpApStatementDO::getBizType, bizType)
                .inIfPresent(ErpApStatementDO::getBizId, bizIds)
                .orderByAsc(ErpApStatementDO::getId));
    }

    default int updateInvoiceById(Long id, Integer invoiceStatus, String invoiceNo, BigDecimal invoiceAmount) {
        return update(null, new LambdaUpdateWrapper<ErpApStatementDO>()
                .eq(ErpApStatementDO::getId, id)
                .set(ErpApStatementDO::getInvoiceStatus, invoiceStatus)
                .set(ErpApStatementDO::getInvoiceNo, invoiceNo)
                .set(ErpApStatementDO::getInvoiceAmount, invoiceAmount));
    }

    List<ErpApStatementSummaryRespVO> selectSummaryList(@Param("supplierId") Long supplierId);

    List<ErpApStatementAgingRespVO> selectAgingList(@Param("reqVO") ErpApStatementAgingReqVO reqVO);

    Page<ErpApStatementReconciliationRespVO> selectReconciliationPage(
            Page<ErpApStatementReconciliationRespVO> page,
            @Param("reqVO") ErpApStatementReconciliationReqVO reqVO);

    static String buildExcludeFullyPrepaidSql() {
        return "(amount <= 0 OR IFNULL((SELECT SUM(allocate_amount) FROM erp_finance_prepayment_allocate "
                + "WHERE ap_statement_id = id AND status = "
                + ErpFinancePrepaymentAllocateStatusEnum.APPROVED.getStatus()
                + "), 0) < amount)";
    }

}
