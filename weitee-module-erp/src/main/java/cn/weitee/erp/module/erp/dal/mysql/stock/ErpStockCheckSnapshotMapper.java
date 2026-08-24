package cn.weitee.erp.module.erp.dal.mysql.stock;

import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockCheckSnapshotDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * ERP 盘点快照 Mapper
 *
 * @author weitee
 */
@Mapper
public interface ErpStockCheckSnapshotMapper extends BaseMapperX<ErpStockCheckSnapshotDO> {

    /**
     * 根据盘点单ID查询快照列表
     *
     * @param checkId 盘点单ID
     * @return 快照列表
     */
    default List<ErpStockCheckSnapshotDO> selectListByCheckId(Long checkId) {
        return selectList(ErpStockCheckSnapshotDO::getCheckId, checkId);
    }

    /**
     * 根据盘点单ID批量查询快照列表
     *
     * @param checkIds 盘点单ID集合
     * @return 快照列表
     */
    default List<ErpStockCheckSnapshotDO> selectListByCheckIds(Collection<Long> checkIds) {
        return selectList(ErpStockCheckSnapshotDO::getCheckId, checkIds);
    }

    /**
     * 根据盘点单ID删除快照
     *
     * @param checkId 盘点单ID
     * @return 删除数量
     */
    default int deleteByCheckId(Long checkId) {
        return delete(ErpStockCheckSnapshotDO::getCheckId, checkId);
    }

    /**
     * 查询指定盘点单中指定产品和仓库的快照
     *
     * @param checkId 盘点单ID
     * @param productId 产品ID
     * @param warehouseId 仓库ID
     * @return 快照记录
     */
    default ErpStockCheckSnapshotDO selectByCheckIdAndProductAndWarehouse(Long checkId, Long productId, Long warehouseId) {
        return selectOne(ErpStockCheckSnapshotDO::getCheckId, checkId,
                ErpStockCheckSnapshotDO::getProductId, productId,
                ErpStockCheckSnapshotDO::getWarehouseId, warehouseId);
    }

}
