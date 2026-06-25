package cn.weitee.erp.module.erp.dal.mysql.purchase;

import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInQualityRoundDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ErpPurchaseInQualityRoundMapper extends BaseMapperX<ErpPurchaseInQualityRoundDO> {

    default List<ErpPurchaseInQualityRoundDO> selectListByQualityId(Long qualityId) {
        return selectList(new LambdaQueryWrapperX<ErpPurchaseInQualityRoundDO>()
                .eq(ErpPurchaseInQualityRoundDO::getQualityId, qualityId)
                .orderByAsc(ErpPurchaseInQualityRoundDO::getRoundNo)
                .orderByAsc(ErpPurchaseInQualityRoundDO::getId));
    }

    default List<ErpPurchaseInQualityRoundDO> selectListByQualityIdAndRoundNo(Long qualityId, Integer roundNo) {
        return selectList(new LambdaQueryWrapperX<ErpPurchaseInQualityRoundDO>()
                .eq(ErpPurchaseInQualityRoundDO::getQualityId, qualityId)
                .eq(ErpPurchaseInQualityRoundDO::getRoundNo, roundNo)
                .orderByAsc(ErpPurchaseInQualityRoundDO::getId));
    }

    default boolean existsByQualityIdAndRoundNo(Long qualityId, Integer roundNo) {
        return selectCount(new LambdaQueryWrapperX<ErpPurchaseInQualityRoundDO>()
                .eq(ErpPurchaseInQualityRoundDO::getQualityId, qualityId)
                .eq(ErpPurchaseInQualityRoundDO::getRoundNo, roundNo)) > 0;
    }

}
