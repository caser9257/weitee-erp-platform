package cn.weitee.erp.module.erp.dal.mysql.product;

import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductCadenceDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface ErpProductCadenceMapper extends BaseMapperX<ErpProductCadenceDO> {

    default ErpProductCadenceDO selectByProductId(Long productId) {
        return selectOne(ErpProductCadenceDO::getProductId, productId);
    }

    default List<ErpProductCadenceDO> selectListByProductIds(Collection<Long> productIds) {
        return selectList(new LambdaQueryWrapperX<ErpProductCadenceDO>()
                .in(ErpProductCadenceDO::getProductId, productIds));
    }

    default void save(ErpProductCadenceDO cadence) {
        ErpProductCadenceDO existed = selectByProductId(cadence.getProductId());
        if (existed == null) {
            insert(cadence);
            return;
        }
        cadence.setId(existed.getId());
        updateById(cadence);
    }

    default void deleteByProductId(Long productId) {
        delete(new LambdaQueryWrapperX<ErpProductCadenceDO>()
                .eq(ErpProductCadenceDO::getProductId, productId));
    }

}
