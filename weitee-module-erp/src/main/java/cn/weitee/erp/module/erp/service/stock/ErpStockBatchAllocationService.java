package cn.weitee.erp.module.erp.service.stock;

import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockBatchAllocationDO;
import cn.weitee.erp.module.erp.service.stock.bo.ErpStockBatchAllocateOutboundReqBO;

import jakarta.validation.Valid;
import java.util.List;

public interface ErpStockBatchAllocationService {

    List<ErpStockBatchAllocationDO> allocateOutbound(@Valid ErpStockBatchAllocateOutboundReqBO reqBO);

    void rollbackOutbound(Integer bizType, Long bizId, Integer rollbackBizType, String remark);

    List<ErpStockBatchAllocationDO> getAllocationListByBiz(Integer bizType, Long bizId);

    List<ErpStockBatchAllocationDO> getAllocationListByStockBatchId(Long stockBatchId);
}
