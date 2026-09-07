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

    /**
     * AP 台账核销互斥锁 key。锁必须绑定在资源（应付台账）上，
     * 付款审批、付款作废、预付款核销、预付款核销回滚等所有"校验余额-写入核销事实"的路径必须共用同一 key，
     * 否则不同子系统之间无法互斥，会出现并发超额核销。
     */
    String ALLOCATE_LOCK_KEY_PREFIX = "erp:ap-statement:allocate:";

    static String allocateLockKey(Long statementId) {
        return ALLOCATE_LOCK_KEY_PREFIX + statementId;
    }

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
