package cn.weitee.erp.module.erp.service.stock;

import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockCheckItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockCheckSnapshotDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockDO;
import cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockCheckItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockCheckSnapshotMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ERP 盘点快照 Service 实现�?
 *
 * @author weitee
 */
@Service
@Validated
@Slf4j
public class ErpStockCheckSnapshotServiceImpl implements ErpStockCheckSnapshotService {

    @Resource
    private ErpStockCheckSnapshotMapper snapshotMapper;

    @Resource
    private ErpStockCheckItemMapper checkItemMapper;

    @Resource
    private ErpStockService stockService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int createSnapshot(Long checkId) {
        // 1. 获取盘点单明细
        List<ErpStockCheckItemDO> checkItems = checkItemMapper.selectListByCheckId(checkId);
        if (checkItems == null || checkItems.isEmpty()) {
            log.warn("[createSnapshot] 盘点单明细为空，checkId={}", checkId);
            return 0;
        }

        // 2. 删除已有快照（防止重复生成）
        snapshotMapper.deleteByCheckId(checkId);

        // 3. 批量查询库存（避免 N+1 查询）
        // 按仓库分组，批量查询每个仓库的库存
        Map<Long, List<ErpStockCheckItemDO>> itemsByWarehouse = checkItems.stream()
                .collect(Collectors.groupingBy(ErpStockCheckItemDO::getWarehouseId));
        
        // 存储库存信息：key = "productId:warehouseId", value = ErpStockDO
        Map<String, ErpStockDO> stockMap = new java.util.HashMap<>();
        
        for (Map.Entry<Long, List<ErpStockCheckItemDO>> entry : itemsByWarehouse.entrySet()) {
            Long warehouseId = entry.getKey();
            List<ErpStockCheckItemDO> items = entry.getValue();
            List<Long> productIds = items.stream()
                    .map(ErpStockCheckItemDO::getProductId)
                    .distinct()
                    .collect(Collectors.toList());
            
            // 批量查询该仓库下所有产品的库存
            List<ErpStockDO> stocks = stockService.getStockListByProductIdsAndWarehouseId(productIds, warehouseId);
            for (ErpStockDO stock : stocks) {
                String key = stock.getProductId() + ":" + stock.getWarehouseId();
                stockMap.put(key, stock);
            }
        }

        // 4. 生成快照
        LocalDateTime snapshotTime = LocalDateTime.now();
        int count = 0;

        for (ErpStockCheckItemDO item : checkItems) {
            // 从批量查询结果中获取库存
            String key = item.getProductId() + ":" + item.getWarehouseId();
            ErpStockDO stock = stockMap.get(key);

            // 构建快照记录
            ErpStockCheckSnapshotDO snapshot = ErpStockCheckSnapshotDO.builder()
                    .checkId(checkId)
                    .productId(item.getProductId())
                    .warehouseId(item.getWarehouseId())
                    .bookQty(stock != null ? stock.getCount() : BigDecimal.ZERO)
                    .bookAmount(stock != null ? stock.getTotalCost() : BigDecimal.ZERO)
                    .averageCost(stock != null ? stock.getAverageCost() : null)
                    .snapshotTime(snapshotTime)
                    .build();

            snapshotMapper.insert(snapshot);

            // 同步账面数量到盘点明细
            BigDecimal bookQty = snapshot.getBookQty();
            checkItemMapper.updateById(new ErpStockCheckItemDO()
                    .setId(item.getId())
                    .setStockCount(bookQty));

            count++;
        }

        log.info("[createSnapshot] 快照生成完成，checkId={}, 快照数量={}", checkId, count);
        return count;
    }

    @Override
    public List<ErpStockCheckSnapshotDO> getSnapshotList(Long checkId) {
        return snapshotMapper.selectListByCheckId(checkId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteSnapshot(Long checkId) {
        return snapshotMapper.deleteByCheckId(checkId);
    }

    @Override
    public ErpStockCheckSnapshotDO getSnapshot(Long checkId, Long productId, Long warehouseId) {
        return snapshotMapper.selectByCheckIdAndProductAndWarehouse(checkId, productId, warehouseId);
    }

}
