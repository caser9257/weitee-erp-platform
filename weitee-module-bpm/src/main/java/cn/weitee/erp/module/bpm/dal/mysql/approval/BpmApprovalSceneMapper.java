package cn.weitee.erp.module.bpm.dal.mysql.approval;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.bpm.controller.admin.approval.vo.scene.BpmApprovalScenePageReqVO;
import cn.weitee.erp.module.bpm.dal.dataobject.approval.BpmApprovalSceneDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 审批场景 Mapper
 */
@Mapper
public interface BpmApprovalSceneMapper extends BaseMapperX<BpmApprovalSceneDO> {

    default PageResult<BpmApprovalSceneDO> selectPage(BpmApprovalScenePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<BpmApprovalSceneDO>()
                .likeIfPresent(BpmApprovalSceneDO::getName, reqVO.getName())
                .eqIfPresent(BpmApprovalSceneDO::getModuleCode, reqVO.getModuleCode())
                .eqIfPresent(BpmApprovalSceneDO::getBizType, reqVO.getBizType())
                .eqIfPresent(BpmApprovalSceneDO::getStatus, reqVO.getStatus())
                .eqIfPresent(BpmApprovalSceneDO::getOwnerUserId, reqVO.getOwnerUserId())
                .orderByDesc(BpmApprovalSceneDO::getId));
    }

    default BpmApprovalSceneDO selectBySceneCode(String sceneCode) {
        return selectOne(new LambdaQueryWrapperX<BpmApprovalSceneDO>()
                .eq(BpmApprovalSceneDO::getSceneCode, sceneCode)
                .last("LIMIT 1"));
    }

    default Long selectCountByActiveSchemeId(Long schemeId) {
        return selectCount(new LambdaQueryWrapperX<BpmApprovalSceneDO>()
                .eq(BpmApprovalSceneDO::getActiveSchemeId, schemeId));
    }

}
