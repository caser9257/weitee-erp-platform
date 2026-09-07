package cn.weitee.erp.module.erp.dal.mysql.product;

import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductCodeHistoryDO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;

/**
 * 物料编码沿革 Mapper
 *
 * @author WeTai
 */
@Mapper
public interface ErpProductCodeHistoryMapper extends BaseMapperX<ErpProductCodeHistoryDO> {

    default List<ErpProductCodeHistoryDO> selectListByProductId(Long productId) {
        return selectList(new LambdaQueryWrapperX<ErpProductCodeHistoryDO>()
                .eq(ErpProductCodeHistoryDO::getProductId, productId)
                .orderByDesc(ErpProductCodeHistoryDO::getId));
    }

    default ErpProductCodeHistoryDO selectByOldCode(String oldCode) {
        return selectOne(ErpProductCodeHistoryDO::getOldCode, oldCode);
    }

    default boolean existsByOldCode(String oldCode) {
        return selectCount(ErpProductCodeHistoryDO::getOldCode, oldCode) > 0;
    }

    default List<ErpProductCodeHistoryDO> selectListByOldCodes(Collection<String> oldCodes) {
        return selectList(new LambdaQueryWrapperX<ErpProductCodeHistoryDO>()
                .in(ErpProductCodeHistoryDO::getOldCode, oldCodes));
    }

}
