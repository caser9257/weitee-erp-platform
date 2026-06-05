package cn.iocoder.yudao.module.erp.service.stock;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.stock.vo.batch.ErpStockBatchRecordPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpStockBatchRecordDO;

import jakarta.validation.Valid;

public interface ErpStockBatchRecordService {

    void createStockBatchRecord(@Valid ErpStockBatchRecordDO record);

    PageResult<ErpStockBatchRecordDO> getStockBatchRecordPage(ErpStockBatchRecordPageReqVO reqVO);

}
