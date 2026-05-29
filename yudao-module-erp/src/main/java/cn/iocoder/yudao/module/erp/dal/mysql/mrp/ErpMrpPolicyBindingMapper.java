package cn.iocoder.yudao.module.erp.dal.mysql.mrp;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpMrpPolicyBindingDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface ErpMrpPolicyBindingMapper extends BaseMapperX<ErpMrpPolicyBindingDO> {

    default List<ErpMrpPolicyBindingDO> selectListByPolicyIds(Collection<Long> policyIds) {
        return selectList(new LambdaQueryWrapperX<ErpMrpPolicyBindingDO>()
                .inIfPresent(ErpMrpPolicyBindingDO::getPolicyId, policyIds)
                .orderByAsc(ErpMrpPolicyBindingDO::getId));
    }

    default List<ErpMrpPolicyBindingDO> selectListByBusinessTypes(Collection<String> businessTypes) {
        return selectList(new LambdaQueryWrapperX<ErpMrpPolicyBindingDO>()
                .inIfPresent(ErpMrpPolicyBindingDO::getBusinessType, businessTypes)
                .orderByAsc(ErpMrpPolicyBindingDO::getId));
    }

    default ErpMrpPolicyBindingDO selectByBusinessType(String businessType) {
        return selectOne(new LambdaQueryWrapperX<ErpMrpPolicyBindingDO>()
                .eq(ErpMrpPolicyBindingDO::getBusinessType, businessType)
                .eq(ErpMrpPolicyBindingDO::getEnableFlag, Boolean.TRUE)
                .orderByDesc(ErpMrpPolicyBindingDO::getId)
                .last("LIMIT 1"));
    }

    default void deleteByPolicyId(Long policyId) {
        delete(ErpMrpPolicyBindingDO::getPolicyId, policyId);
    }
}
