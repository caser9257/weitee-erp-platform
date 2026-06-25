package cn.weitee.erp.module.bpm.dal.mysql.approval;

import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.bpm.dal.dataobject.approval.BpmApprovalRuleDO;
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
