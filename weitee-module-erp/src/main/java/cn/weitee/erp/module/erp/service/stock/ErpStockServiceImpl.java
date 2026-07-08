package cn.weitee.erp.module.erp.service.stock;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.stock.vo.stock.ErpStockPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockDO;
import cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockMapper;
import cn.weitee.erp.module.erp.framework.event.StockBelowSafetyEvent;
import cn.weitee.erp.module.erp.service.mrp.ErpMaterialPlanRuleService;
import cn.weitee.erp.module.erp.service.product.ErpProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.STOCK_COUNT_NEGATIVE;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.STOCK_COUNT_NEGATIVE2;

/**
 * ERP 产品库存 Service 实现类
 *
 * @author WeTai
 */
@Service
@Validated
@Slf4j
public class ErpStockServiceImpl implements ErpStockService {

    /**
     * 允许库存为负数
     *
     * TODO 芋艿：后续做成 db 配置
     */
    private static final Boolean NEGATIVE_STOCK_COUNT_ENABLE = false;

    @Resource
    private ErpProductService productService;
    @Resource
    private ErpWarehouseService warehouseService;
    @Resource
    private ErpMaterialPlanRuleService materialPlanRuleService;
    @Resource
    private ApplicationEventPublisher eventPublisher;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ErpStockMapper erpStockMapper;

    @Override
    public ErpStockDO getStock(Long id) {
        return erpStockMapper.selectById(id);
    }

    @Override
    public ErpStockDO getStock(Long productId, Long warehouseId) {
        return erpStockMapper.selectByProductIdAndWarehouseId(productId, warehouseId);
    }

    @Override
    public List<ErpStockDO> getStockListByProductIds(Collection<Long> productIds) {
        return erpStockMapper.selectListByProductIdIn(productIds);
    }

    @Override
    public List<ErpStockDO> getStockListByProductAndWarehouseIds(Collection<Long> productIds, Collection<Long> warehouseIds) {
        return erpStockMapper.selectListByProductIdsAndWarehouseIds(productIds, warehouseIds);
    }

    @Override
    public Map<String, ErpStockDO> getStockMapByProductAndWarehouseIds(Collection<Long> productIds, Collection<Long> warehouseIds) {
        List<ErpStockDO> stockList = getStockListByProductAndWarehouseIds(productIds, warehouseIds);
        Map<String, ErpStockDO> stockMap = new HashMap<>(stockList.size());
        for (ErpStockDO stock : stockList) {
            stockMap.put(ErpStockService.buildProductWarehouseKey(stock.getProductId(), stock.getWarehouseId()), stock);
        }
        return stockMap;
    }

    @Override
    public BigDecimal getStockCount(Long productId) {
        BigDecimal count = erpStockMapper.selectSumByProductId(productId);
        return count != null ? count : BigDecimal.ZERO;
    }

    @Override
    public Map<Long, BigDecimal> getStockCountMap(Collection<Long> productIds) {
        return erpStockMapper.selectSumMapByProductIds(productIds);
    }

    @Override
    public PageResult<ErpStockDO> getStockPage(ErpStockPageReqVO pageReqVO) {
        return erpStockMapper.selectPage(pageReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BigDecimal updateStockCountIncrement(Long productId, Long warehouseId, BigDecimal count) {
        return updateStockCountIncrement(productId, warehouseId, count, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BigDecimal updateStockCountIncrement(Long productId, Long warehouseId, BigDecimal count, BigDecimal price) {
        // 1.1 查询当前库存
        ErpStockDO stock = erpStockMapper.selectByProductIdAndWarehouseId(productId, warehouseId);
        if (stock == null) {
            stock = new ErpStockDO().setProductId(productId).setWarehouseId(warehouseId)
                    .setCount(BigDecimal.ZERO).setAverageCost(BigDecimal.ZERO).setTotalCost(BigDecimal.ZERO);
            erpStockMapper.insert(stock);
        }
        // 1.2 校验库存是否充足
        if (!NEGATIVE_STOCK_COUNT_ENABLE && stock.getCount().add(count).compareTo(BigDecimal.ZERO) < 0) {
            throw exception(STOCK_COUNT_NEGATIVE, productService.getProduct(productId).getName(),
                    warehouseService.getWarehouse(warehouseId).getName(), stock.getCount(), count);
        }

        // 2. 库存变更
        int updateCount = erpStockMapper.updateCountIncrement(stock.getId(), count, NEGATIVE_STOCK_COUNT_ENABLE);
        if (updateCount == 0) {
            throw exception(STOCK_COUNT_NEGATIVE2, productService.getProduct(productId).getName(),
                    warehouseService.getWarehouse(warehouseId).getName());
        }

        // 3. 成本计算
        if (count.compareTo(BigDecimal.ZERO) > 0 && price != null && price.compareTo(BigDecimal.ZERO) > 0) {
            // 入库：重算加权平均成本
            BigDecimal newAverageCost = calculateWeightedAverageCost(productId, warehouseId, count, price);
            BigDecimal newTotalCount = stock.getCount().add(count);
            BigDecimal newTotalCost = newAverageCost.multiply(newTotalCount).setScale(2, java.math.RoundingMode.HALF_UP);
            erpStockMapper.updateById(new ErpStockDO().setId(stock.getId())
                    .setAverageCost(newAverageCost).setTotalCost(newTotalCost));
        } else if (count.compareTo(BigDecimal.ZERO) < 0 && stock.getAverageCost() != null) {
            // 出库：更新总金额
            BigDecimal newTotalCount = stock.getCount().add(count);
            BigDecimal newTotalCost = stock.getAverageCost().multiply(newTotalCount).setScale(2, java.math.RoundingMode.HALF_UP);
            erpStockMapper.updateById(new ErpStockDO().setId(stock.getId()).setTotalCost(newTotalCost));
        }

        // 4. 返回最新库存
        BigDecimal newCount = stock.getCount().add(count);

        // 5. 检查是否低于安全库存（仅在库存减少时检查）
        if (count.compareTo(BigDecimal.ZERO) < 0) {
            checkAndPublishStockAlert(productId, warehouseId, newCount);
        }

        return newCount;
    }

    /**
     * 库存预警冷却期 Redis key 前缀
     * 同一产品+仓库在冷却期内不重复发布预警事件
     */
    private static final String STOCK_ALERT_COOLDOWN_KEY_PREFIX = "erp:stock-alert:cooldown:";
    private static final Duration STOCK_ALERT_COOLDOWN_DURATION = Duration.ofMinutes(30);

    /**
     * 检查并发布库存预警事件
     */
    private void checkAndPublishStockAlert(Long productId, Long warehouseId, BigDecimal newCount) {
        try {
            // 查询安全库存规则
            cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpMaterialPlanRuleDO rule =
                    materialPlanRuleService.getRuleByProductId(productId);
            if (rule == null || rule.getSafetyStock() == null || rule.getSafetyStock().compareTo(BigDecimal.ZERO) <= 0) {
                return; // 没有设置安全库存，不做预警
            }

            BigDecimal safetyStock = rule.getSafetyStock();

            // 检查是否低于安全库存
            if (newCount.compareTo(safetyStock) < 0) {
                // 冷却期检查：避免高频出入库操作产生大量重复事件
                String cooldownKey = STOCK_ALERT_COOLDOWN_KEY_PREFIX + productId + ":" + warehouseId;
                Boolean exists = stringRedisTemplate.hasKey(cooldownKey);
                if (Boolean.TRUE.equals(exists)) {
                    log.debug("[checkAndPublishStockAlert] 冷却期内跳过，productId={}, warehouseId={}", productId, warehouseId);
                    return;
                }

                BigDecimal shortage = safetyStock.subtract(newCount);
                // 发布预警事件（同步处理，由监听器在事务提交后执行）
                eventPublisher.publishEvent(new StockBelowSafetyEvent(
                        productId, warehouseId, newCount, safetyStock, shortage));

                // 设置冷却期
                stringRedisTemplate.opsForValue().set(cooldownKey, "1", STOCK_ALERT_COOLDOWN_DURATION);

                log.info("[checkAndPublishStockAlert] 库存低于安全库存，已发布预警事件，productId={}, currentCount={}, safetyStock={}, shortage={}",
                        productId, newCount, safetyStock, shortage);
            }
        } catch (Exception e) {
            log.error("[checkAndPublishStockAlert] 检查库存预警失败，productId={}", productId, e);
        }
    }

    @Override
    public BigDecimal calculateWeightedAverageCost(Long productId, Long warehouseId, BigDecimal inCount, BigDecimal inPrice) {
        ErpStockDO stock = erpStockMapper.selectByProductIdAndWarehouseIdForUpdate(productId, warehouseId);
        if (stock == null || stock.getAverageCost() == null || stock.getCount() == null
                || stock.getCount().compareTo(BigDecimal.ZERO) <= 0) {
            return inPrice;
        }
        // 加权平均 = (现有库存 * 现有平均成本 + 新入库数量 * 新入库单价) / (现有库存 + 新入库数量)
        BigDecimal existingTotalCost = stock.getAverageCost().multiply(stock.getCount());
        BigDecimal newTotalCost = inPrice.multiply(inCount);
        BigDecimal newTotalCount = stock.getCount().add(inCount);
        if (newTotalCount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return existingTotalCost.add(newTotalCost).divide(newTotalCount, 6, java.math.RoundingMode.HALF_UP);
    }

}
