package cn.iocoder.yudao.module.erp.service.purchase;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.sourcebatch.ErpPurchaseSourceBatchPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.sourcebatch.ErpPurchaseSourceBatchSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseSourceBatchDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

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
