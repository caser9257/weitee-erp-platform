package cn.weitee.erp.module.erp.service.stock;

import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockCheckSnapshotDO;

import java.util.List;

/**
 * ERP 盘点快照 Service 接口
 *
 * @author ruoyi-vue-pro
 */
public interface ErpStockCheckSnapshotService {

    /**
     * 为盘点单生成快照
     *
     * 根据盘点单明细中的产品和仓库，从 erp_stock 表获取当前库存数据，
     * 生成快照记录并同步账面数量到盘点明细
     *
     * @param checkId 盘点单ID
     * @return 生成的快照数量
     */
    int createSnapshot(Long checkId);

    /**
     * 获取盘点单的快照列表
     *
     * @param checkId 盘点单ID
     * @return 快照列表
     */
    List<ErpStockCheckSnapshotDO> getSnapshotList(Long checkId);

    /**
     * 删除盘点单的快照
     *
     * @param checkId 盘点单ID
     * @return 删除数量
     */
    int deleteSnapshot(Long checkId);

    /**
     * 获取指定产品和仓库的快照
     *
     * @param checkId 盘点单ID
     * @param productId 产品ID
     * @param warehouseId 仓库ID
     * @return 快照记录，不存在返回 null
     */
    ErpStockCheckSnapshotDO getSnapshot(Long checkId, Long productId, Long warehouseId);

}
