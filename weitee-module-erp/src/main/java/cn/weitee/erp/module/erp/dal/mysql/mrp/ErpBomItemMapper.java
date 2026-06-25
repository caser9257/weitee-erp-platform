package cn.weitee.erp.module.erp.dal.mysql.mrp;

import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpBomItemDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface ErpBomItemMapper extends BaseMapperX<ErpBomItemDO> {

    default List<ErpBomItemDO> selectListByBomId(Long bomId) {
        return selectList(ErpBomItemDO::getBomId, bomId);
    }

    default void deleteByBomId(Long bomId) {
        delete(ErpBomItemDO::getBomId, bomId);
    }

    default List<ErpBomItemDO> selectListByBomIds(Collection<Long> bomIds) {
        return selectList(ErpBomItemDO::getBomId, bomIds);
    }

}
