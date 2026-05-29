package cn.iocoder.yudao.module.erp.dal.mysql.mrp;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.policy.ErpMrpNettingPolicyPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpMrpNettingPolicyDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;

@Mapper
public interface ErpMrpNettingPolicyMapper extends BaseMapperX<ErpMrpNettingPolicyDO> {

    default PageResult<ErpMrpNettingPolicyDO> selectPage(ErpMrpNettingPolicyPageReqVO reqVO,
                                                         Collection<Long> policyIds) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpMrpNettingPolicyDO>()
                .likeIfPresent(ErpMrpNettingPolicyDO::getCode, reqVO.getCode())
                .likeIfPresent(ErpMrpNettingPolicyDO::getName, reqVO.getName())
                .eqIfPresent(ErpMrpNettingPolicyDO::getEnableFlag, reqVO.getEnableFlag())
                .inIfPresent(ErpMrpNettingPolicyDO::getId, policyIds)
                .orderByDesc(ErpMrpNettingPolicyDO::getDefaultFlag)
                .orderByDesc(ErpMrpNettingPolicyDO::getId));
    }

    default ErpMrpNettingPolicyDO selectByCode(String code) {
        return selectOne(ErpMrpNettingPolicyDO::getCode, code);
    }

    default ErpMrpNettingPolicyDO selectDefaultPolicy() {
        return selectOne(new LambdaQueryWrapperX<ErpMrpNettingPolicyDO>()
                .eq(ErpMrpNettingPolicyDO::getDefaultFlag, Boolean.TRUE)
                .eq(ErpMrpNettingPolicyDO::getEnableFlag, Boolean.TRUE)
                .orderByDesc(ErpMrpNettingPolicyDO::getId)
                .last("LIMIT 1"));
    }
}
