package cn.weitee.erp.module.erp.dal.mysql.stock;

import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockBatchAllocationDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collections;
import java.util.List;

@Mapper
public interface ErpStockBatchAllocationMapper extends BaseMapperX<ErpStockBatchAllocationDO> {

    default List<ErpStockBatchAllocationDO> selectListByBiz(Integer bizType, Long bizId) {
        List<ErpStockBatchAllocationDO> list = selectList(new LambdaQueryWrapperX<ErpStockBatchAllocationDO>()
                .eq(ErpStockBatchAllocationDO::getBizType, bizType)
                .eq(ErpStockBatchAllocationDO::getBizId, bizId)
                .orderByAsc(ErpStockBatchAllocationDO::getId));
        return list == null ? Collections.emptyList() : list;
    }

    default List<ErpStockBatchAllocationDO> selectListByBizItem(Integer bizType, Long bizId, Long bizItemId) {
        List<ErpStockBatchAllocationDO> list = selectList(new LambdaQueryWrapperX<ErpStockBatchAllocationDO>()
                .eq(ErpStockBatchAllocationDO::getBizType, bizType)
                .eq(ErpStockBatchAllocationDO::getBizId, bizId)
                .eq(ErpStockBatchAllocationDO::getBizItemId, bizItemId)
                .orderByAsc(ErpStockBatchAllocationDO::getId));
        return list == null ? Collections.emptyList() : list;
    }

    default List<ErpStockBatchAllocationDO> selectListByStockBatchId(Long stockBatchId) {
        List<ErpStockBatchAllocationDO> list = selectList(new LambdaQueryWrapperX<ErpStockBatchAllocationDO>()
                .eq(ErpStockBatchAllocationDO::getStockBatchId, stockBatchId)
                .orderByAsc(ErpStockBatchAllocationDO::getCreateTime)
                .orderByAsc(ErpStockBatchAllocationDO::getId));
        return list == null ? Collections.emptyList() : list;
    }

    default int deleteByBiz(Integer bizType, Long bizId) {
        return delete(new LambdaQueryWrapperX<ErpStockBatchAllocationDO>()
                .eq(ErpStockBatchAllocationDO::getBizType, bizType)
                .eq(ErpStockBatchAllocationDO::getBizId, bizId));
    }
}
