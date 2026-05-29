package cn.iocoder.yudao.module.erp.service.mrp.support;

import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpBomDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpBomItemDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpMaterialPlanRuleDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductUnitDO;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class ErpMrpSupplyContext {

    private final Map<Long, ErpMaterialPlanRuleDO> ruleMap = new HashMap<>();
    private final Map<Long, ErpBomDO> bomMap = new HashMap<>();
    private final Map<Long, List<ErpBomItemDO>> bomItemMap = new HashMap<>();
    private final Map<Long, ErpProductDO> productMap = new HashMap<>();
    private final Map<Long, ErpProductUnitDO> productUnitMap = new HashMap<>();
    private final Map<Long, BigDecimal> availableStockMap = new HashMap<>();
    private final Map<Long, BigDecimal> reservedStockMap = new HashMap<>();
    private final Map<ErpMrpMaterialProjectKey, BigDecimal> projectIncomingMap = new HashMap<>();
    private final Map<ErpMrpMaterialProjectKey, BigDecimal> projectWipMap = new HashMap<>();
    private final Map<ErpMrpMaterialProjectKey, BigDecimal> remainingIncomingMap = new HashMap<>();
    private final Map<ErpMrpMaterialProjectKey, BigDecimal> remainingWipMap = new HashMap<>();
    private final Map<Long, BigDecimal> remainingStockMap = new HashMap<>();

    public BigDecimal getRemainingIncoming(ErpMrpMaterialProjectKey key) {
        return remainingIncomingMap.computeIfAbsent(key, item -> projectIncomingMap.getOrDefault(item, BigDecimal.ZERO));
    }

    public BigDecimal getRemainingWip(ErpMrpMaterialProjectKey key) {
        return remainingWipMap.computeIfAbsent(key, item -> projectWipMap.getOrDefault(item, BigDecimal.ZERO));
    }

    public BigDecimal getRemainingStock(Long materialId) {
        return remainingStockMap.computeIfAbsent(materialId, item -> availableStockMap.getOrDefault(item, BigDecimal.ZERO));
    }

    public void consumeIncoming(ErpMrpMaterialProjectKey key, BigDecimal qty) {
        remainingIncomingMap.put(key, getRemainingIncoming(key).subtract(qty));
    }

    public void consumeWip(ErpMrpMaterialProjectKey key, BigDecimal qty) {
        remainingWipMap.put(key, getRemainingWip(key).subtract(qty));
    }

    public void consumeStock(Long materialId, BigDecimal qty) {
        remainingStockMap.put(materialId, getRemainingStock(materialId).subtract(qty));
    }
}
