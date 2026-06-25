package cn.weitee.erp.module.erp.dal.mysql.purchase;

import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInQualityDefectDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ErpPurchaseInQualityDefectMapper extends BaseMapperX<ErpPurchaseInQualityDefectDO> {

    default List<ErpPurchaseInQualityDefectDO> selectListByQualityId(Long qualityId) {
        return selectList(new LambdaQueryWrapperX<ErpPurchaseInQualityDefectDO>()
                .eq(ErpPurchaseInQualityDefectDO::getQualityId, qualityId)
                .orderByAsc(ErpPurchaseInQualityDefectDO::getRoundId)
                .orderByAsc(ErpPurchaseInQualityDefectDO::getId));
    }

}
