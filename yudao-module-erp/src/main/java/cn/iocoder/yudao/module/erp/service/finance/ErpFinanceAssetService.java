package cn.iocoder.yudao.module.erp.service.finance;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.asset.ErpFinanceAssetPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.asset.ErpFinanceAssetSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceAssetCandidateDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceAssetDO;

import jakarta.validation.Valid;
import java.util.List;

public interface ErpFinanceAssetService {

    Long createFinanceAsset(@Valid ErpFinanceAssetSaveReqVO createReqVO);

    void updateFinanceAsset(@Valid ErpFinanceAssetSaveReqVO updateReqVO);

    void deleteFinanceAsset(List<Long> ids);

    void updateFinanceAssetStatus(Long id, Integer status);

    ErpFinanceAssetDO getFinanceAsset(Long id);

    ErpFinanceAssetCandidateDO getCandidateByAssetSource(Long candidateId, Integer sourceType, Long sourceBizId,
                                                         Long sourceItemId, String sourceBizNo, String assetName);

    List<Long> getActiveResearchCapitalizeAssetIdsByPeriod(String period);

    PageResult<ErpFinanceAssetDO> getFinanceAssetPage(ErpFinanceAssetPageReqVO pageReqVO);
}
