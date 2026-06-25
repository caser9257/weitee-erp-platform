package cn.weitee.erp.module.bpm.dal.mysql.approval;

import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.bpm.dal.dataobject.approval.BpmApprovalSchemeVersionDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 审批方案版本 Mapper
 */
@Mapper
public interface BpmApprovalSchemeVersionMapper extends BaseMapperX<BpmApprovalSchemeVersionDO> {

    default BpmApprovalSchemeVersionDO selectLatestBySchemeId(Long schemeId) {
        return selectOne(new LambdaQueryWrapperX<BpmApprovalSchemeVersionDO>()
                .eq(BpmApprovalSchemeVersionDO::getSchemeId, schemeId)
                .orderByDesc(BpmApprovalSchemeVersionDO::getVersionNo)
                .last("LIMIT 1"));
    }

    default BpmApprovalSchemeVersionDO selectBySchemeIdAndStatus(Long schemeId, Integer status) {
        return selectOne(new LambdaQueryWrapperX<BpmApprovalSchemeVersionDO>()
                .eq(BpmApprovalSchemeVersionDO::getSchemeId, schemeId)
                .eq(BpmApprovalSchemeVersionDO::getStatus, status)
                .orderByDesc(BpmApprovalSchemeVersionDO::getVersionNo)
                .last("LIMIT 1"));
    }

}
