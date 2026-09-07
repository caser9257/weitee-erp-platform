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
        return selectList(new LambdaQueryWrapperX<ErpProductSubstituteDO>()
                .eq(ErpProductSubstituteDO::getProductId, productId)
                .orderByAsc(ErpProductSubstituteDO::getPriority)
                .orderByAsc(ErpProductSubstituteDO::getId));
    }

    default List<ErpProductSubstituteDO> selectListByProductIds(Collection<Long> productIds) {
        return selectList(new LambdaQueryWrapperX<ErpProductSubstituteDO>()
                .in(ErpProductSubstituteDO::getProductId, productIds)
                .orderByAsc(ErpProductSubstituteDO::getPriority));
    }

    default void deleteByProductIdAndSubstituteId(Long productId, Long substituteProductId) {
        delete(new LambdaQueryWrapperX<ErpProductSubstituteDO>()
                .eq(ErpProductSubstituteDO::getProductId, productId)
                .eq(ErpProductSubstituteDO::getSubstituteProductId, substituteProductId));
    }

}
