package cn.weitee.erp.module.erp.service.finance;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.apstatement.ErpApStatementAgingReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.apstatement.ErpApStatementAgingRespVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.apstatement.ErpApStatementPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.apstatement.ErpApStatementPaymentEnablePageReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.apstatement.ErpApStatementReconciliationReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.apstatement.ErpApStatementReconciliationRespVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.apstatement.ErpApStatementSummaryRespVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.apstatement.ErpApStatementUpdateInvoiceReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpApStatementDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpApStatementItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceExpenseDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpOutsourceFeeDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpOutsourceOrderDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseReturnDO;

import jakarta.validation.Valid;
import java.util.Collection;
import java.util.List;

public interface ErpApStatementService {

    void createStatementForPurchaseIn(ErpPurchaseInDO purchaseIn);

    void createStatementForPurchaseReturn(ErpPurchaseReturnDO purchaseReturn);

    void createStatementForOutsourceFee(ErpOutsourceFeeDO outsourceFee, ErpOutsourceOrderDO outsourceOrder);

    void createStatementForFinanceExpense(ErpFinanceExpenseDO expense);

    void refreshStatementAmountByIds(Collection<Long> statementIds);

    void refreshBizSummaryByStatementIds(Collection<Long> statementIds);

    void closeStatementByBiz(Integer bizType, Long bizId, String remark);

    void updateInvoice(@Valid ErpApStatementUpdateInvoiceReqVO updateReqVO);

    ErpApStatementDO validateApStatement(Long id);

    ErpApStatementDO getApStatement(Long id);

    ErpApStatementDO getApStatementByBizTypeAndBizId(Integer bizType, Long bizId);

    List<ErpApStatementDO> getApStatementListByIds(Collection<Long> ids);

    List<ErpApStatementDO> getApStatementListByBizTypeAndBizIds(Integer bizType, Collection<Long> bizIds);

    List<ErpApStatementItemDO> getApStatementItemListByStatementId(Long statementId);

    PageResult<ErpApStatementDO> getApStatementPage(ErpApStatementPageReqVO reqVO);

    List<ErpApStatementSummaryRespVO> getSummaryList(Long supplierId);

    PageResult<ErpApStatementDO> getPaymentEnablePage(ErpApStatementPaymentEnablePageReqVO reqVO);

    List<ErpApStatementAgingRespVO> getAgingList(ErpApStatementAgingReqVO reqVO);

    PageResult<ErpApStatementReconciliationRespVO> getReconciliationPage(ErpApStatementReconciliationReqVO reqVO);

}
