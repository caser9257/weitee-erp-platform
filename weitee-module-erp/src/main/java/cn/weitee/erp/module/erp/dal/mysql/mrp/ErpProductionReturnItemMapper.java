package cn.weitee.erp.module.erp.dal.mysql.mrp;

import cn.hutool.core.collection.CollUtil;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionReturnItemDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper
public interface ErpProductionReturnItemMapper extends BaseMapperX<ErpProductionReturnItemDO> {

    default List<ErpProductionReturnItemDO> selectListByProductionMaterialIds(Collection<Long> productionMaterialIds) {
        if (CollUtil.isEmpty(productionMaterialIds)) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapper<ErpProductionReturnItemDO>()
                .in(ErpProductionReturnItemDO::getProductionMaterialId, productionMaterialIds));
    }

}
