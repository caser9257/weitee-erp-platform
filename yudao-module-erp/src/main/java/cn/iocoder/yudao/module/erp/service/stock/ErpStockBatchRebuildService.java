package cn.iocoder.yudao.module.erp.service.stock;

import cn.iocoder.yudao.module.erp.controller.admin.stock.vo.batch.ErpStockBatchRebuildOutboundReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.stock.vo.batch.ErpStockBatchRebuildOutboundRespVO;

import jakarta.validation.Valid;

public interface ErpStockBatchRebuildService {

    ErpStockBatchRebuildOutboundRespVO previewOutbound(@Valid ErpStockBatchRebuildOutboundReqVO reqVO);

    ErpStockBatchRebuildOutboundRespVO rebuildOutbound(@Valid ErpStockBatchRebuildOutboundReqVO reqVO);

}
