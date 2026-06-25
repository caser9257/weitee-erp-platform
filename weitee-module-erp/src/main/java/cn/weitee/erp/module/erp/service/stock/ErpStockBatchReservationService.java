package cn.weitee.erp.module.erp.service.stock;

import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockBatchAllocationDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockBatchReservationDO;
import cn.weitee.erp.module.erp.service.stock.bo.ErpStockBatchAllocateOutboundReqBO;

import jakarta.validation.Valid;
import java.util.List;

public interface ErpStockBatchReservationService {

    List<ErpStockBatchReservationDO> reserveOutbound(@Valid ErpStockBatchAllocateOutboundReqBO reqBO);

    void releaseReservation(Integer bizType, Long bizId, String remark);

    List<ErpStockBatchAllocationDO> deductReservation(Integer bizType, Long bizId, String remark);

    List<ErpStockBatchReservationDO> getReservationListByBiz(Integer bizType, Long bizId);

    List<ErpStockBatchReservationDO> getReservationListByStockBatchId(Long stockBatchId);

}
