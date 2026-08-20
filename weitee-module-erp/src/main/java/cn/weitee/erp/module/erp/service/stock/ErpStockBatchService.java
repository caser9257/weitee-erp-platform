package cn.weitee.erp.module.erp.service.stock;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.stock.vo.batch.ErpStockBatchAdjustReqVO;
import cn.weitee.erp.module.erp.controller.admin.stock.vo.batch.ErpStockBatchPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockBatchDO;
import cn.weitee.erp.module.erp.service.stock.bo.ErpStockBatchChangeReqBO;
import cn.weitee.erp.module.erp.service.stock.bo.ErpStockBatchInboundReqBO;

import jakarta.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertMap;

public interface ErpStockBatchService {

    ErpStockBatchDO getStockBatch(Long id);

    ErpStockBatchDO getStockBatchByProductWarehouseAndBatchNo(Long productId, Long warehouseId, String batchNo);

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
