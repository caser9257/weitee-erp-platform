package cn.iocoder.yudao.module.erp.service.finance;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apstatement.ErpApStatementAgingReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apstatement.ErpApStatementAgingRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apstatement.ErpApStatementPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apstatement.ErpApStatementPaymentEnablePageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apstatement.ErpApStatementReconciliationReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apstatement.ErpApStatementReconciliationRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apstatement.ErpApStatementSummaryRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apstatement.ErpApStatementUpdateInvoiceReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpApStatementDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpApStatementItemDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceExpenseDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpOutsourceFeeDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpOutsourceOrderDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseReturnDO;

import javax.validation.Valid;
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
