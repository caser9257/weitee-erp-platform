package cn.iocoder.yudao.module.erp.service.finance;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.asset.ErpFinanceAssetDepreciationPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceAssetDepreciationDO;

import java.util.List;

public interface ErpFinanceAssetDepreciationService {

    Integer generateDepreciation(String period);

    Integer generateDepreciationForAssets(String period, List<Long> assetIds);

    ErpFinanceAssetDepreciationDO getFinanceAssetDepreciation(Long id);

    PageResult<ErpFinanceAssetDepreciationDO> getFinanceAssetDepreciationPage(ErpFinanceAssetDepreciationPageReqVO pageReqVO);
}
