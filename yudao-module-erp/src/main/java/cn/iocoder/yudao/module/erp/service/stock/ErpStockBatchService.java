package cn.iocoder.yudao.module.erp.service.stock;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.stock.vo.batch.ErpStockBatchAdjustReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.stock.vo.batch.ErpStockBatchPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpStockBatchDO;
import cn.iocoder.yudao.module.erp.service.stock.bo.ErpStockBatchChangeReqBO;
import cn.iocoder.yudao.module.erp.service.stock.bo.ErpStockBatchInboundReqBO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

public interface ErpStockBatchService {

    ErpStockBatchDO getStockBatch(Long id);

    ErpStockBatchDO validateStockBatch(Long id);

    List<ErpStockBatchDO> getAvailableStockBatchList(Long productId, Long warehouseId);

    PageResult<ErpStockBatchDO> getStockBatchPage(ErpStockBatchPageReqVO reqVO);

    List<ErpStockBatchDO> getStockBatchList(Collection<Long> ids);

    default Map<Long, ErpStockBatchDO> getStockBatchMap(Collection<Long> ids) {
        return convertMap(getStockBatchList(ids), ErpStockBatchDO::getId);
    }

    ErpStockBatchDO createOrIncreaseBatch(@Valid ErpStockBatchInboundReqBO reqBO);

    ErpStockBatchDO adjustBatch(@Valid ErpStockBatchAdjustReqVO reqVO);

    ErpStockBatchDO increaseBatch(@Valid ErpStockBatchChangeReqBO reqBO);

    ErpStockBatchDO decreaseBatch(@Valid ErpStockBatchChangeReqBO reqBO);

    ErpStockBatchDO lockBatch(@Valid ErpStockBatchChangeReqBO reqBO);

    ErpStockBatchDO releaseLockedBatch(@Valid ErpStockBatchChangeReqBO reqBO);

    ErpStockBatchDO deductLockedBatch(@Valid ErpStockBatchChangeReqBO reqBO);

}
