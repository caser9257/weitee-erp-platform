package cn.weitee.erp.module.erp.service.stock;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.stock.vo.batch.ErpStockBatchRecordPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockBatchRecordDO;

import jakarta.validation.Valid;

public interface ErpStockBatchRecordService {

    void createStockBatchRecord(@Valid ErpStockBatchRecordDO record);

    PageResult<ErpStockBatchRecordDO> getStockBatchRecordPage(ErpStockBatchRecordPageReqVO reqVO);

}
