package cn.iocoder.yudao.module.bpm.dal.mysql.approval;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.bpm.controller.admin.approval.vo.scheme.BpmApprovalSchemePageReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.approval.vo.scheme.BpmApprovalSchemeRespVO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.approval.BpmApprovalSchemeDO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.approval.BpmApprovalSchemeVersionDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 审批方案 Mapper
 */
@Mapper
public interface BpmApprovalSchemeMapper extends BaseMapperX<BpmApprovalSchemeDO> {

    default PageResult<BpmApprovalSchemeRespVO> selectPage(BpmApprovalSchemePageReqVO reqVO) {
        MPJLambdaWrapperX<BpmApprovalSchemeDO> query = new MPJLambdaWrapperX<BpmApprovalSchemeDO>()
                .selectAll(BpmApprovalSchemeDO.class)
                .selectAs(BpmApprovalSchemeVersionDO::getId, BpmApprovalSchemeRespVO::getLatestVersionId)
                .selectAs(BpmApprovalSchemeVersionDO::getVersionNo, BpmApprovalSchemeRespVO::getLatestVersionNo)
                .selectAs(BpmApprovalSchemeVersionDO::getStatus, BpmApprovalSchemeRespVO::getLatestVersionStatus)
                .leftJoin(BpmApprovalSchemeVersionDO.class, BpmApprovalSchemeVersionDO::getId, BpmApprovalSchemeDO::getLatestVersionId)
                .likeIfPresent(BpmApprovalSchemeDO::getName, reqVO.getName())
                .eqIfPresent(BpmApprovalSchemeDO::getModuleCode, reqVO.getModuleCode())
                .eqIfPresent(BpmApprovalSchemeDO::getBizType, reqVO.getBizType())
                .eqIfPresent(BpmApprovalSchemeVersionDO::getStatus, reqVO.getLatestVersionStatus())
                .orderByDesc(BpmApprovalSchemeDO::getUpdateTime)
                .orderByDesc(BpmApprovalSchemeDO::getId);
        return selectJoinPage(reqVO, BpmApprovalSchemeRespVO.class, query);
    }

    default BpmApprovalSchemeDO selectByCode(String code) {
        return selectOne(new LambdaQueryWrapperX<BpmApprovalSchemeDO>()
                .eq(BpmApprovalSchemeDO::getCode, code)
                .last("LIMIT 1"));
    }

}
