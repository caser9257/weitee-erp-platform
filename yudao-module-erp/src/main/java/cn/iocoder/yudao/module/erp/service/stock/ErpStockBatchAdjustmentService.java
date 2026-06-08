package cn.iocoder.yudao.module.erp.service.stock;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.stock.vo.batch.ErpStockBatchAdjustReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.stock.vo.batch.ErpStockBatchAdjustmentPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpStockBatchAdjustmentDO;

import jakarta.validation.Valid;

public interface ErpStockBatchAdjustmentService {

    ErpStockBatchAdjustmentDO adjustBatch(@Valid ErpStockBatchAdjustReqVO reqVO);

    PageResult<ErpStockBatchAdjustmentDO> getStockBatchAdjustmentPage(ErpStockBatchAdjustmentPageReqVO reqVO);

}
