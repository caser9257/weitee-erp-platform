package cn.iocoder.yudao.module.bpm.dal.mysql.approval;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.bpm.dal.dataobject.approval.BpmApprovalSchemeVersionDO;
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
