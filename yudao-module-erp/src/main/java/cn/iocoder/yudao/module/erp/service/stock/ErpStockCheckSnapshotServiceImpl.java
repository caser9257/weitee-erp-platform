package cn.iocoder.yudao.module.erp.service.stock;

import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpStockCheckItemDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpStockCheckSnapshotDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpStockDO;
import cn.iocoder.yudao.module.erp.dal.mysql.stock.ErpStockCheckItemMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.stock.ErpStockCheckSnapshotMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ERP 盘点快照 Service 实现类
 *
 * @author ruoyi-vue-pro
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

        // 3. 生成快照
        LocalDateTime snapshotTime = LocalDateTime.now();
        int count = 0;

        for (ErpStockCheckItemDO item : checkItems) {
            // 获取当前库存
            ErpStockDO stock = stockService.getStock(item.getProductId(), item.getWarehouseId());

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
