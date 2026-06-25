package cn.weitee.erp.module.erp.dal.mysql.stock;

import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockBatchReservationDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collections;
import java.util.List;

@Mapper
public interface ErpStockBatchReservationMapper extends BaseMapperX<ErpStockBatchReservationDO> {

    default List<ErpStockBatchReservationDO> selectListByBiz(Integer bizType, Long bizId) {
        List<ErpStockBatchReservationDO> list = selectList(new LambdaQueryWrapperX<ErpStockBatchReservationDO>()
                .eq(ErpStockBatchReservationDO::getBizType, bizType)
                .eq(ErpStockBatchReservationDO::getBizId, bizId)
                .orderByAsc(ErpStockBatchReservationDO::getId));
        return list == null ? Collections.emptyList() : list;
    }

    default List<ErpStockBatchReservationDO> selectListByBizItem(Integer bizType, Long bizId, Long bizItemId) {
        List<ErpStockBatchReservationDO> list = selectList(new LambdaQueryWrapperX<ErpStockBatchReservationDO>()
                .eq(ErpStockBatchReservationDO::getBizType, bizType)
                .eq(ErpStockBatchReservationDO::getBizId, bizId)
                .eq(ErpStockBatchReservationDO::getBizItemId, bizItemId)
                .orderByAsc(ErpStockBatchReservationDO::getId));
        return list == null ? Collections.emptyList() : list;
    }

    default List<ErpStockBatchReservationDO> selectListByStockBatchId(Long stockBatchId) {
        List<ErpStockBatchReservationDO> list = selectList(new LambdaQueryWrapperX<ErpStockBatchReservationDO>()
                .eq(ErpStockBatchReservationDO::getStockBatchId, stockBatchId)
                .orderByAsc(ErpStockBatchReservationDO::getCreateTime)
                .orderByAsc(ErpStockBatchReservationDO::getId));
        return list == null ? Collections.emptyList() : list;
    }

    @Delete("DELETE FROM erp_stock_batch_reservation WHERE biz_type = #{bizType} AND biz_id = #{bizId}")
    int deleteByBiz(@Param("bizType") Integer bizType, @Param("bizId") Long bizId);

}
