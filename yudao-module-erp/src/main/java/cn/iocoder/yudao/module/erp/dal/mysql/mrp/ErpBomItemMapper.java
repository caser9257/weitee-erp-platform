package cn.iocoder.yudao.module.erp.dal.mysql.mrp;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpBomItemDO;
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
