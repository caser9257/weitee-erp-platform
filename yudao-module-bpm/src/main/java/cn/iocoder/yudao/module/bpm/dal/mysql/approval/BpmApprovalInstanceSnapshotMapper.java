package cn.iocoder.yudao.module.bpm.dal.mysql.approval;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.bpm.dal.dataobject.approval.BpmApprovalInstanceSnapshotDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

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

    default BpmApprovalInstanceSnapshotDO selectByApprovalId(String approvalId) {
        return selectOne(new LambdaQueryWrapperX<BpmApprovalInstanceSnapshotDO>()
                .eq(BpmApprovalInstanceSnapshotDO::getApprovalId, approvalId)
                .last("LIMIT 1"));
    }

    /**
     * 按状态统计数量
     */
    @Select("SELECT COUNT(*) FROM bpm_approval_instance_snapshot WHERE status = #{status} AND deleted = 0")
    long selectCountByStatus(@Param("status") Integer status);

    /**
     * 按用户+状态统计数量
     */
    @Select("SELECT COUNT(*) FROM bpm_approval_instance_snapshot WHERE start_user_id = #{startUserId} AND status = #{status} AND deleted = 0")
    long selectCountByStartUserIdAndStatus(@Param("startUserId") Long startUserId, @Param("status") Integer status);

    /**
     * 全局各状态聚合统计
     */
    @Select("SELECT " +
            "SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) AS processingCount, " +
            "SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END) AS approvedCount, " +
            "SUM(CASE WHEN status = 3 THEN 1 ELSE 0 END) AS rejectedCount, " +
            "SUM(CASE WHEN status = 4 THEN 1 ELSE 0 END) AS cancelledCount " +
            "FROM bpm_approval_instance_snapshot WHERE deleted = 0")
    Map<String, Object> selectStatusStatistics();

    /**
     * 按用户各状态聚合统计
     */
    @Select("SELECT " +
            "SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) AS processingCount, " +
            "SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END) AS approvedCount, " +
            "SUM(CASE WHEN status = 3 THEN 1 ELSE 0 END) AS rejectedCount, " +
            "SUM(CASE WHEN status = 4 THEN 1 ELSE 0 END) AS cancelledCount " +
            "FROM bpm_approval_instance_snapshot WHERE start_user_id = #{startUserId} AND deleted = 0")
    Map<String, Object> selectStatusStatisticsByStartUserId(@Param("startUserId") Long startUserId);

    /**
     * 按用户分组统计各状态数量
     */
    @Select("SELECT start_user_id AS startUserId, " +
            "COUNT(*) AS totalCount, " +
            "SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) AS processingCount, " +
            "SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END) AS approvedCount, " +
            "SUM(CASE WHEN status = 3 THEN 1 ELSE 0 END) AS rejectedCount, " +
            "SUM(CASE WHEN status = 4 THEN 1 ELSE 0 END) AS cancelledCount " +
            "FROM bpm_approval_instance_snapshot WHERE deleted = 0 GROUP BY start_user_id")
    List<Map<String, Object>> selectGroupByStartUserId();

}
