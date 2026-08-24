package cn.weitee.erp.module.erp.service.finance;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.asset.ErpFinanceAssetDepreciationPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceAssetDepreciationDO;

import java.util.List;

public interface ErpFinanceAssetDepreciationService {

    Integer generateDepreciation(String period);

    Integer generateDepreciationForAssets(String period, List<Long> assetIds);

    ErpFinanceAssetDepreciationDO getFinanceAssetDepreciation(Long id);

    void bindVoucher(Long id, Long voucherId);

    PageResult<ErpFinanceAssetDepreciationDO> getFinanceAssetDepreciationPage(ErpFinanceAssetDepreciationPageReqVO pageReqVO);
}
