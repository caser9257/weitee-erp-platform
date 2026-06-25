package cn.weitee.erp.module.erp.service.purchase;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.sourcebatch.ErpPurchaseSourceBatchPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.sourcebatch.ErpPurchaseSourceBatchSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseSourceBatchDO;

import jakarta.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertMap;

public interface ErpPurchaseSourceBatchService {

    Long createPurchaseSourceBatch(@Valid ErpPurchaseSourceBatchSaveReqVO createReqVO);

    void updatePurchaseSourceBatch(@Valid ErpPurchaseSourceBatchSaveReqVO updateReqVO);

    void closePurchaseSourceBatch(Long id);

    ErpPurchaseSourceBatchDO getPurchaseSourceBatch(Long id);

    ErpPurchaseSourceBatchDO validatePurchaseSourceBatch(Long id);

    List<ErpPurchaseSourceBatchDO> getPurchaseSourceBatchList(Collection<Long> ids);

    default Map<Long, ErpPurchaseSourceBatchDO> getPurchaseSourceBatchMap(Collection<Long> ids) {
        return convertMap(getPurchaseSourceBatchList(ids), ErpPurchaseSourceBatchDO::getId);
    }

    PageResult<ErpPurchaseSourceBatchDO> getPurchaseSourceBatchPage(ErpPurchaseSourceBatchPageReqVO pageReqVO);
}
