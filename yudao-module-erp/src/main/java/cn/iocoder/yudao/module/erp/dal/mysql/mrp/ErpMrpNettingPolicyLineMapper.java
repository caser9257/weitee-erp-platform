package cn.iocoder.yudao.module.erp.dal.mysql.mrp;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpMrpNettingPolicyLineDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface ErpMrpNettingPolicyLineMapper extends BaseMapperX<ErpMrpNettingPolicyLineDO> {

    default List<ErpMrpNettingPolicyLineDO> selectListByPolicyIds(Collection<Long> policyIds) {
        return selectList(new LambdaQueryWrapperX<ErpMrpNettingPolicyLineDO>()
                .inIfPresent(ErpMrpNettingPolicyLineDO::getPolicyId, policyIds)
                .orderByAsc(ErpMrpNettingPolicyLineDO::getSequenceNo)
                .orderByAsc(ErpMrpNettingPolicyLineDO::getId));
    }

    default List<ErpMrpNettingPolicyLineDO> selectListByPolicyId(Long policyId) {
        return selectListByPolicyIds(List.of(policyId));
    }

    default void deleteByPolicyId(Long policyId) {
        delete(ErpMrpNettingPolicyLineDO::getPolicyId, policyId);
    }
}
