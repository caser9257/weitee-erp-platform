package cn.weitee.erp.module.erp.dal.mysql.product;

import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductSubstituteDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface ErpProductSubstituteMapper extends BaseMapperX<ErpProductSubstituteDO> {

    default List<ErpProductSubstituteDO> selectListByProductId(Long productId) {
        return selectList(ErpProductSubstituteDO::getProductId, productId);
    }

    default List<ErpProductSubstituteDO> selectListByProductIds(Collection<Long> productIds) {
        return selectList(ErpProductSubstituteDO::getProductId, productIds);
    }

    default void deleteByProductIdAndSubstituteId(Long productId, Long substituteProductId) {
        delete(new LambdaQueryWrapperX<ErpProductSubstituteDO>()
                .eq(ErpProductSubstituteDO::getProductId, productId)
                .eq(ErpProductSubstituteDO::getSubstituteProductId, substituteProductId));
    }

}
