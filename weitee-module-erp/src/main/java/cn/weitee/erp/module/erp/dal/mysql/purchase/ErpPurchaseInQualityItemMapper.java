package cn.weitee.erp.module.erp.dal.mysql.purchase;

import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInQualityItemDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ErpPurchaseInQualityItemMapper extends BaseMapperX<ErpPurchaseInQualityItemDO> {

    default List<ErpPurchaseInQualityItemDO> selectListByQualityId(Long qualityId) {
        return selectList(ErpPurchaseInQualityItemDO::getQualityId, qualityId);
    }

    default int deleteByQualityId(Long qualityId) {
        return delete(ErpPurchaseInQualityItemDO::getQualityId, qualityId);
    }

}
