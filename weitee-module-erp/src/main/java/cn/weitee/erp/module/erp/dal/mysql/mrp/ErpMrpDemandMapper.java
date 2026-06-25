package cn.weitee.erp.module.erp.dal.mysql.mrp;

import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpMrpDemandDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ErpMrpDemandMapper extends BaseMapperX<ErpMrpDemandDO> {

    default void deleteByPlanId(Long planId) {
        delete(ErpMrpDemandDO::getPlanId, planId);
    }

    default List<ErpMrpDemandDO> selectListByPlanId(Long planId) {
        return selectList(ErpMrpDemandDO::getPlanId, planId);
    }

}
