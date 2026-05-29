package cn.iocoder.yudao.module.erp.dal.mysql.mrp;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionReturnItemDO;
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
