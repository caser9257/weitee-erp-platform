package cn.weitee.erp.module.erp.dal.mysql.stock;

import cn.hutool.core.collection.CollUtil;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.module.erp.controller.admin.stock.vo.batch.ErpStockBatchPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockBatchDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper
public interface ErpStockBatchMapper extends BaseMapperX<ErpStockBatchDO> {

    default PageResult<ErpStockBatchDO> selectPage(ErpStockBatchPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapper<ErpStockBatchDO>()
                .eq(reqVO.getProductId() != null, ErpStockBatchDO::getProductId, reqVO.getProductId())
                .eq(reqVO.getWarehouseId() != null, ErpStockBatchDO::getWarehouseId, reqVO.getWarehouseId())
                .like(reqVO.getBatchNo() != null && !reqVO.getBatchNo().isBlank(), ErpStockBatchDO::getBatchNo, reqVO.getBatchNo())
                .orderByAsc(ErpStockBatchDO::getInboundTime)
                .orderByAsc(ErpStockBatchDO::getCreateTime)
                .orderByAsc(ErpStockBatchDO::getId));
    }

    default ErpStockBatchDO selectByProductWarehouseAndBatchNo(Long productId, Long warehouseId, String batchNo) {
        return selectOne(new LambdaQueryWrapper<ErpStockBatchDO>()
                .eq(ErpStockBatchDO::getProductId, productId)
                .eq(ErpStockBatchDO::getWarehouseId, warehouseId)
                .eq(ErpStockBatchDO::getBatchNo, batchNo));
    }

    default List<ErpStockBatchDO> selectAvailableList(Long productId, Long warehouseId) {
        return selectList(new LambdaQueryWrapper<ErpStockBatchDO>()
                .eq(ErpStockBatchDO::getProductId, productId)
                .eq(ErpStockBatchDO::getWarehouseId, warehouseId)
                .gt(ErpStockBatchDO::getAvailableQty, BigDecimal.ZERO)
                .orderByAsc(ErpStockBatchDO::getInboundTime)
                .orderByAsc(ErpStockBatchDO::getCreateTime)
                .orderByAsc(ErpStockBatchDO::getId));
    }

    default List<ErpStockBatchDO> selectListByIds(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return selectByIds(ids);
    }

    default int updateQtyIncrement(Long id, BigDecimal count) {
        return update(null, new LambdaUpdateWrapper<ErpStockBatchDO>()
                .eq(ErpStockBatchDO::getId, id)
                .setSql("total_qty = total_qty + " + count.toPlainString()
                        + ", available_qty = available_qty + " + count.toPlainString()));
    }

    default int updateQtyDecrement(Long id, BigDecimal count) {
        return update(null, new LambdaUpdateWrapper<ErpStockBatchDO>()
                .eq(ErpStockBatchDO::getId, id)
                .ge(ErpStockBatchDO::getAvailableQty, count)
                .ge(ErpStockBatchDO::getTotalQty, count)
                .setSql("total_qty = total_qty - " + count.toPlainString()
                        + ", available_qty = available_qty - " + count.toPlainString()));
    }

    default int updateLockIncrement(Long id, BigDecimal count) {
        return update(null, new LambdaUpdateWrapper<ErpStockBatchDO>()
                .eq(ErpStockBatchDO::getId, id)
                .ge(ErpStockBatchDO::getAvailableQty, count)
                .setSql("available_qty = available_qty - " + count.toPlainString()
                        + ", locked_qty = IFNULL(locked_qty, 0) + " + count.toPlainString()));
    }

    default int updateLockRelease(Long id, BigDecimal count) {
        return update(null, new LambdaUpdateWrapper<ErpStockBatchDO>()
                .eq(ErpStockBatchDO::getId, id)
                .ge(ErpStockBatchDO::getLockedQty, count)
                .setSql("available_qty = available_qty + " + count.toPlainString()
                        + ", locked_qty = locked_qty - " + count.toPlainString()));
    }

    default int updateLockedDeduct(Long id, BigDecimal count) {
        return update(null, new LambdaUpdateWrapper<ErpStockBatchDO>()
                .eq(ErpStockBatchDO::getId, id)
                .ge(ErpStockBatchDO::getLockedQty, count)
                .ge(ErpStockBatchDO::getTotalQty, count)
                .setSql("total_qty = total_qty - " + count.toPlainString()
                        + ", locked_qty = locked_qty - " + count.toPlainString()));
    }

}
