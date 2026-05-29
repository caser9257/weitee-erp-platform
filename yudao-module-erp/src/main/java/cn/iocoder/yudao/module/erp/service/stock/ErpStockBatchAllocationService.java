package cn.iocoder.yudao.module.erp.service.stock;

import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpStockBatchAllocationDO;
import cn.iocoder.yudao.module.erp.service.stock.bo.ErpStockBatchAllocateOutboundReqBO;

import javax.validation.Valid;
import java.util.List;

public interface ErpStockBatchAllocationService {

    List<ErpStockBatchAllocationDO> allocateOutbound(@Valid ErpStockBatchAllocateOutboundReqBO reqBO);

    void rollbackOutbound(Integer bizType, Long bizId, Integer rollbackBizType, String remark);

    List<ErpStockBatchAllocationDO> getAllocationListByBiz(Integer bizType, Long bizId);

    List<ErpStockBatchAllocationDO> getAllocationListByStockBatchId(Long stockBatchId);
}
