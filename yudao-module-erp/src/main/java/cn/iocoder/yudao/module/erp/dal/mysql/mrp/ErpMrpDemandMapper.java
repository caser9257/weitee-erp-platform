package cn.iocoder.yudao.module.erp.dal.mysql.mrp;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpMrpDemandDO;
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
