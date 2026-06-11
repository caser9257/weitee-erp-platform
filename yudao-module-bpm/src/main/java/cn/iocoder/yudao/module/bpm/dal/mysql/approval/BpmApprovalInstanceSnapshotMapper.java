package cn.iocoder.yudao.module.bpm.dal.mysql.approval;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.bpm.dal.dataobject.approval.BpmApprovalInstanceSnapshotDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 审批运行时快照 Mapper
 */
@Mapper
public interface BpmApprovalInstanceSnapshotMapper extends BaseMapperX<BpmApprovalInstanceSnapshotDO> {

    default BpmApprovalInstanceSnapshotDO selectByProcessInstanceId(String processInstanceId) {
        return selectOne(new LambdaQueryWrapperX<BpmApprovalInstanceSnapshotDO>()
                .eq(BpmApprovalInstanceSnapshotDO::getProcessInstanceId, processInstanceId)
                .last("LIMIT 1"));
    }

    default BpmApprovalInstanceSnapshotDO selectBySceneCodeAndBizId(String sceneCode, String bizId) {
        return selectOne(new LambdaQueryWrapperX<BpmApprovalInstanceSnapshotDO>()
                .eq(BpmApprovalInstanceSnapshotDO::getSceneCode, sceneCode)
                .eq(BpmApprovalInstanceSnapshotDO::getBizId, bizId)
                .orderByDesc(BpmApprovalInstanceSnapshotDO::getId)
                .last("LIMIT 1"));
    }

    default List<BpmApprovalInstanceSnapshotDO> selectListByStatus(Integer status) {
        return selectList(new LambdaQueryWrapperX<BpmApprovalInstanceSnapshotDO>()
                .eq(BpmApprovalInstanceSnapshotDO::getStatus, status));
    }

    default List<BpmApprovalInstanceSnapshotDO> selectListByStartUserId(Long startUserId) {
        return selectList(new LambdaQueryWrapperX<BpmApprovalInstanceSnapshotDO>()
                .eq(BpmApprovalInstanceSnapshotDO::getStartUserId, startUserId));
    }

}
