package cn.weitee.erp.module.erp.dal.mysql.mrp;

import cn.hutool.core.collection.CollUtil;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpOutsourceReturnItemDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper
public interface ErpOutsourceReturnItemMapper extends BaseMapperX<ErpOutsourceReturnItemDO> {
    default List<ErpOutsourceReturnItemDO> selectListByReturnId(Long returnId) {
        return selectList(ErpOutsourceReturnItemDO::getReturnId, returnId);
    }
    default List<ErpOutsourceReturnItemDO> selectListByReturnIds(Collection<Long> returnIds) {
        if (CollUtil.isEmpty(returnIds)) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapper<ErpOutsourceReturnItemDO>()
                .in(ErpOutsourceReturnItemDO::getReturnId, returnIds)
                .orderByAsc(ErpOutsourceReturnItemDO::getId));
    }
}
