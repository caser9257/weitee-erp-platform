package cn.weitee.erp.module.erp.dal.mysql.stock;

import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockAssembleItemDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface ErpStockAssembleItemMapper extends BaseMapperX<ErpStockAssembleItemDO> {

    default List<ErpStockAssembleItemDO> selectListByAssembleId(Long assembleId) {
        return selectList(new LambdaQueryWrapperX<ErpStockAssembleItemDO>()
                .eq(ErpStockAssembleItemDO::getAssembleId, assembleId)
                .orderByAsc(ErpStockAssembleItemDO::getId));
    }

    default List<ErpStockAssembleItemDO> selectListByAssembleIds(Collection<Long> assembleIds) {
        return selectList(ErpStockAssembleItemDO::getAssembleId, assembleIds);
    }

    default void deleteByAssembleId(Long assembleId) {
        delete(ErpStockAssembleItemDO::getAssembleId, assembleId);
    }
}
