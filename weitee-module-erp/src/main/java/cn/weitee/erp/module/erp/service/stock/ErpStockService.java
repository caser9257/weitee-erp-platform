package cn.weitee.erp.module.erp.service.stock;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.stock.vo.stock.ErpStockPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockDO;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

/**
 * ERP 产品库存 Service 接口
 *
 * @author WeTai
 */
public interface ErpStockService {

    /**
     * 获得产品库存
     *
     * @param id 编号
     * @return 库存
     */
    ErpStockDO getStock(Long id);

    /**
     * 基于产品 + 仓库，获得产品库存
     *
     * @param productId 产品编号
     * @param warehouseId 仓库编号
     * @return 产品库存
     */
    ErpStockDO getStock(Long productId, Long warehouseId);

    /**
     * 获得产品库存数量
     *
     * 如果不存在库存记录，则返回 0
     *
     * @param productId 产品编号
     * @return 产品库存数量
     */
    BigDecimal getStockCount(Long productId);

    Map<Long, BigDecimal> getStockCountMap(Collection<Long> productIds);

    /**
     * 获得产品库存分页
     *
     * @param pageReqVO 分页查询
     * @return 库存分页
     */
    PageResult<ErpStockDO> getStockPage(ErpStockPageReqVO pageReqVO);

    /**
     * 增量更新产品库存数量
     *
     * @param productId 产品编号
     * @param warehouseId 仓库编号
     * @param count 增量数量：正数，表示增加；负数，表示减少
     * @return 更新后的库存
     */
    BigDecimal updateStockCountIncrement(Long productId, Long warehouseId, BigDecimal count);

    /**
     * 增量更新产品库存数量（带成本追踪）
     *
     * @param productId 产品编号
     * @param warehouseId 仓库编号
     * @param count 增量数量：正数，表示增加；负数，表示减少
     * @param price 单价（入库时传入，出库时可为 null）
     * @return 更新后的库存
     */
    BigDecimal updateStockCountIncrement(Long productId, Long warehouseId, BigDecimal count, BigDecimal price);

    /**
     * 计算移动加权平均成本
     *
     * @param productId 产品编号
     * @param warehouseId 仓库编号
     * @param inCount 入库数量
     * @param inPrice 入库单价
     * @return 新的加权平均成本
     */
    BigDecimal calculateWeightedAverageCost(Long productId, Long warehouseId, BigDecimal inCount, BigDecimal inPrice);

}
