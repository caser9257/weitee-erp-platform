package cn.weitee.erp.module.erp.service.finance;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.asset.ErpFinanceAssetCandidateConfirmReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.asset.ErpFinanceAssetCandidatePageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceAssetCandidateDO;

public interface ErpFinanceAssetCandidateService {

    PageResult<ErpFinanceAssetCandidateDO> getFinanceAssetCandidatePage(ErpFinanceAssetCandidatePageReqVO pageReqVO);

    Long confirmFinanceAssetCandidate(ErpFinanceAssetCandidateConfirmReqVO reqVO);

    Long createCandidateFromExpense(Long expenseId);

    Long createCandidateFromPurchaseIn(Long purchaseInId);
}
