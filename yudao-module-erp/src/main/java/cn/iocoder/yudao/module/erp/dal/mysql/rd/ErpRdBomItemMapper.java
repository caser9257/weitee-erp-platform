package cn.iocoder.yudao.module.erp.dal.mysql.rd;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.erp.dal.dataobject.rd.ErpRdBomItemDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ErpRdBomItemMapper extends BaseMapperX<ErpRdBomItemDO> {

    default List<ErpRdBomItemDO> selectListByBomId(Long bomId) {
        return selectList(ErpRdBomItemDO::getBomId, bomId);
    }

    default void deleteByBomId(Long bomId) {
        delete(ErpRdBomItemDO::getBomId, bomId);
    }

}
