package cn.iocoder.yudao.module.bpm.dal.mysql.approval;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.bpm.controller.admin.approval.vo.scene.BpmApprovalScenePageReqVO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.approval.BpmApprovalSceneDO;
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
