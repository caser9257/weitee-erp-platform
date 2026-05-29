package cn.iocoder.yudao.module.bpm.dal.mysql.approval;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.bpm.dal.dataobject.approval.BpmApprovalRuleDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 审批规则 Mapper
 */
@Mapper
public interface BpmApprovalRuleMapper extends BaseMapperX<BpmApprovalRuleDO> {

    default List<BpmApprovalRuleDO> selectListBySchemeVersionId(Long schemeVersionId) {
        return selectList(new LambdaQueryWrapperX<BpmApprovalRuleDO>()
                .eq(BpmApprovalRuleDO::getSchemeVersionId, schemeVersionId)
                .orderByDesc(BpmApprovalRuleDO::getDefaultRule)
                .orderByDesc(BpmApprovalRuleDO::getPriority)
                .orderByDesc(BpmApprovalRuleDO::getId));
    }

}
