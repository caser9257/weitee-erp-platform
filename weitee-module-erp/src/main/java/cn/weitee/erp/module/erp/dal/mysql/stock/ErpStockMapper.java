package cn.weitee.erp.module.erp.dal.mysql.stock;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.controller.admin.stock.vo.stock.ErpStockPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ERP 产品库存 Mapper
 *
 * @author WeTai
 */
@Mapper
public interface ErpStockMapper extends BaseMapperX<ErpStockDO> {

    default PageResult<ErpStockDO> selectPage(ErpStockPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpStockDO>()
                .eqIfPresent(ErpStockDO::getProductId, reqVO.getProductId())
                .eqIfPresent(ErpStockDO::getWarehouseId, reqVO.getWarehouseId())
                .orderByDesc(ErpStockDO::getId));
    }

    default ErpStockDO selectByProductIdAndWarehouseId(Long productId, Long warehouseId) {
        return selectOne(ErpStockDO::getProductId, productId,
                ErpStockDO::getWarehouseId, warehouseId);
    }

    /**
     * 使用行锁查询库存（用于加权平均成本计算）
     */
    default ErpStockDO selectByProductIdAndWarehouseIdForUpdate(Long productId, Long warehouseId) {
        return selectOne(new LambdaQueryWrapperX<ErpStockDO>()
                .eq(ErpStockDO::getProductId, productId)
                .eq(ErpStockDO::getWarehouseId, warehouseId)
                .last("FOR UPDATE"));
    }

    default int updateCountIncrement(Long id, BigDecimal count, boolean negativeEnable) {
        LambdaUpdateWrapper<ErpStockDO> updateWrapper = new LambdaUpdateWrapper<ErpStockDO>()
                .eq(ErpStockDO::getId, id);
        if (count.compareTo(BigDecimal.ZERO) > 0) {
            updateWrapper.setSql("count = count + " + count);
        } else if (count.compareTo(BigDecimal.ZERO) < 0) {
            if (!negativeEnable) {
                updateWrapper.ge(ErpStockDO::getCount, count.abs());
            }
            updateWrapper.setSql("count = count - " + count.abs());
        }
        return update(null, updateWrapper);
    }

    default BigDecimal selectSumByProductId(Long productId) {
        // SQL sum 查询
        List<Map<String, Object>> result = selectMaps(new QueryWrapper<ErpStockDO>()
                .select("SUM(count) AS sum_count")
                .eq("product_id", productId));
        // 获得数量
        if (CollUtil.isEmpty(result)) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(MapUtil.getDouble(result.get(0), "sum_count", 0D));
    }

    /**
     * 根据产品编号批量查询库存（用于消除 N+1）
     *
     * @param productIds 产品编号集合
     * @return 库存列表
     */
    default List<ErpStockDO> selectListByProductIdIn(Collection<Long> productIds) {
        if (CollUtil.isEmpty(productIds)) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<ErpStockDO>()
                .in(ErpStockDO::getProductId, productIds));
    }

    /**
     * 根据产品编号和仓库编号批量查询库存。
     *
     * 查询条件按产品集合和仓库集合收敛，调用方再按产品+仓库组合键取值，避免循环内单条查询。
     */
    default List<ErpStockDO> selectListByProductIdsAndWarehouseIds(Collection<Long> productIds,
                                                                   Collection<Long> warehouseIds) {
        if (CollUtil.isEmpty(productIds) || CollUtil.isEmpty(warehouseIds)) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<ErpStockDO>()
                .in(ErpStockDO::getProductId, productIds)
                .in(ErpStockDO::getWarehouseId, warehouseIds));
    }

    default Map<Long, BigDecimal> selectSumMapByProductIds(Collection<Long> productIds) {
        if (CollUtil.isEmpty(productIds)) {
            return Collections.emptyMap();
        }
        List<Map<String, Object>> result = selectMaps(new QueryWrapper<ErpStockDO>()
                .select("product_id", "SUM(count) AS sum_count")
                .in("product_id", productIds)
                .groupBy("product_id"));
        Map<Long, BigDecimal> stockCountMap = new HashMap<>();
        for (Map<String, Object> row : result) {
            stockCountMap.put(MapUtil.getLong(row, "product_id"),
                    BigDecimal.valueOf(MapUtil.getDouble(row, "sum_count", 0D)));
        }
        return stockCountMap;
    }

    /**
     * 批量查询产品库存（基于产品ID + 仓库ID）
     *
     * @param productIds 产品编号集合
     * @param warehouseId 仓库编号
     * @return 产品库存列表
     */
    default List<ErpStockDO> selectListByProductIdsAndWarehouseId(Collection<Long> productIds, Long warehouseId) {
        if (CollUtil.isEmpty(productIds)) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<ErpStockDO>()
                .in(ErpStockDO::getProductId, productIds)
                .eq(ErpStockDO::getWarehouseId, warehouseId));
    }

    /**
     * 批量查询产品库存（基于产品ID + 仓库ID列表）
     *
     * @param productId 产品编号
     * @param warehouseIds 仓库编号集合
     * @return 产品库存列表
     */
    default List<ErpStockDO> selectListByProductIdAndWarehouseIds(Long productId, Collection<Long> warehouseIds) {
        if (CollUtil.isEmpty(warehouseIds)) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<ErpStockDO>()
                .eq(ErpStockDO::getProductId, productId)
                .in(ErpStockDO::getWarehouseId, warehouseIds));
    }

}
