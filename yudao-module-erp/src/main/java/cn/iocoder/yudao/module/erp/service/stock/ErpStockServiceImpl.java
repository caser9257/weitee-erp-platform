package cn.iocoder.yudao.module.erp.service.stock;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.stock.vo.stock.ErpStockPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpStockDO;
import cn.iocoder.yudao.module.erp.dal.mysql.stock.ErpStockMapper;
import cn.iocoder.yudao.module.erp.service.product.ErpProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.STOCK_COUNT_NEGATIVE;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.STOCK_COUNT_NEGATIVE2;

/**
 * ERP 产品库存 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
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
        return stock.getCount().add(count);
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
