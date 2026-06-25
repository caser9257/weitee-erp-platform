package cn.weitee.erp.module.erp.service.stock;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.stock.vo.batch.ErpStockBatchAdjustReqVO;
import cn.weitee.erp.module.erp.controller.admin.stock.vo.batch.ErpStockBatchAdjustmentPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockBatchAdjustmentDO;

import jakarta.validation.Valid;

public interface ErpStockBatchAdjustmentService {

    ErpStockBatchAdjustmentDO adjustBatch(@Valid ErpStockBatchAdjustReqVO reqVO);

    PageResult<ErpStockBatchAdjustmentDO> getStockBatchAdjustmentPage(ErpStockBatchAdjustmentPageReqVO reqVO);

}
