package cn.iocoder.yudao.module.erp.service.sale;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOrderDO;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpProductionFinishQualityMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.sale.ErpSaleOrderMapper;
import cn.iocoder.yudao.module.erp.enums.sale.ErpSaleOrderDeliveryReadyStatusEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.math.BigDecimal;

@Service
public class ErpSaleOrderDeliveryReadyServiceImpl implements ErpSaleOrderDeliveryReadyService {

    @Resource
    private ErpSaleOrderMapper erpSaleOrderMapper;
    @Resource
    private ErpProductionFinishQualityMapper erpProductionFinishQualityMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recalculate(Long saleOrderId) {
        ErpSaleOrderDO saleOrder = erpSaleOrderMapper.selectById(saleOrderId);
        if (saleOrder == null) {
            return;
        }
        BigDecimal totalCount = ObjectUtil.defaultIfNull(saleOrder.getTotalCount(), BigDecimal.ZERO);
        BigDecimal outCount = ObjectUtil.defaultIfNull(saleOrder.getOutCount(), BigDecimal.ZERO);
        BigDecimal returnCount = ObjectUtil.defaultIfNull(saleOrder.getReturnCount(), BigDecimal.ZERO);
        BigDecimal remainingShipQty = totalCount.subtract(outCount).add(returnCount);
        BigDecimal qualifiedQty = ObjectUtil.defaultIfNull(
                erpProductionFinishQualityMapper.sumQualifiedQtyBySourceOrderId(saleOrderId), BigDecimal.ZERO);
        String status = resolveStatus(remainingShipQty, qualifiedQty);
        if (ObjectUtil.equal(status, saleOrder.getDeliveryReadyStatus())) {
            return;
        }
        erpSaleOrderMapper.updateById(new ErpSaleOrderDO().setId(saleOrderId).setDeliveryReadyStatus(status));
    }

    private String resolveStatus(BigDecimal remainingShipQty, BigDecimal qualifiedQty) {
        if (remainingShipQty.compareTo(BigDecimal.ZERO) <= 0) {
            return ErpSaleOrderDeliveryReadyStatusEnum.NOT_READY.getStatus();
        }
        if (qualifiedQty.compareTo(BigDecimal.ZERO) <= 0) {
            return ErpSaleOrderDeliveryReadyStatusEnum.NOT_READY.getStatus();
        }
        if (qualifiedQty.compareTo(remainingShipQty) >= 0) {
            return ErpSaleOrderDeliveryReadyStatusEnum.READY_TO_SHIP.getStatus();
        }
        return ErpSaleOrderDeliveryReadyStatusEnum.PART_READY.getStatus();
    }

}
