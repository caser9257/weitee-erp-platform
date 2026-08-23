package cn.weitee.erp.module.erp.dal.mysql.rd;

import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.module.erp.dal.dataobject.rd.ErpRdBomItemDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ErpRdBomItemMapper extends BaseMapperX<ErpRdBomItemDO> {

    default List<ErpRdBomItemDO> selectListByBomId(Long bomId) {
        return selectList(ErpRdBomItemDO::getBomId, bomId);
    }

    default List<ErpRdBomItemDO> selectListByMaterialId(Long materialId) {
        return selectList(ErpRdBomItemDO::getMaterialId, materialId);
    }

    default List<ErpRdBomItemDO> selectListByMaterialIds(java.util.Collection<Long> materialIds) {
        return selectList(ErpRdBomItemDO::getMaterialId, materialIds);
    }

    default List<ErpRdBomItemDO> selectListByBomIds(java.util.Collection<Long> bomIds) {
        return selectList(ErpRdBomItemDO::getBomId, bomIds);
    }

    default void deleteByBomId(Long bomId) {
        delete(ErpRdBomItemDO::getBomId, bomId);
    }

}
