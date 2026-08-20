package cn.weitee.erp.module.erp.dal.mysql.rd;

import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.module.erp.dal.dataobject.rd.ErpRdBomChangeLogDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ErpRdBomChangeLogMapper extends BaseMapperX<ErpRdBomChangeLogDO> {

    default List<ErpRdBomChangeLogDO> selectListByBomId(Long bomId) {
        return selectList(ErpRdBomChangeLogDO::getBomId, bomId);
    }

}
