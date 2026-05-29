package cn.iocoder.yudao.module.erp.service.finance;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.asset.ErpFinanceAssetCandidateConfirmReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.asset.ErpFinanceAssetCandidatePageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceAssetCandidateDO;

public interface ErpFinanceAssetCandidateService {

    PageResult<ErpFinanceAssetCandidateDO> getFinanceAssetCandidatePage(ErpFinanceAssetCandidatePageReqVO pageReqVO);

    Long confirmFinanceAssetCandidate(ErpFinanceAssetCandidateConfirmReqVO reqVO);

    Long createCandidateFromExpense(Long expenseId);

    Long createCandidateFromPurchaseIn(Long purchaseInId);
}
