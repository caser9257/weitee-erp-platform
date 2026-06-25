package cn.weitee.erp.module.erp.dal.mysql.stock;

import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockOutItemDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * ERP 其它出库单项 Mapper
 *
 * @author WeTai
 */
@Mapper
public interface ErpStockOutItemMapper extends BaseMapperX<ErpStockOutItemDO> {

    default List<ErpStockOutItemDO> selectListByOutId(Long outId) {
        return selectList(ErpStockOutItemDO::getOutId, outId);
    }

    default List<ErpStockOutItemDO> selectListByOutIds(Collection<Long> outIds) {
        return selectList(ErpStockOutItemDO::getOutId, outIds);
    }

    default int deleteByOutId(Long outId) {
        return delete(ErpStockOutItemDO::getOutId, outId);
    }

}