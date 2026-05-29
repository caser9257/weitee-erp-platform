package cn.iocoder.yudao.module.erp.dal.mysql.rd;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.erp.dal.dataobject.rd.ErpRdBomItemSubstituteDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface ErpRdBomItemSubstituteMapper extends BaseMapperX<ErpRdBomItemSubstituteDO> {

    default List<ErpRdBomItemSubstituteDO> selectListByBomItemIds(Collection<Long> bomItemIds) {
        return selectList(ErpRdBomItemSubstituteDO::getBomItemId, bomItemIds);
    }

    default int deleteByBomItemIds(Collection<Long> bomItemIds) {
        return deleteBatch(ErpRdBomItemSubstituteDO::getBomItemId, bomItemIds);
    }

}
