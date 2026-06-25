package cn.weitee.erp.module.bpm.dal.mysql.approval;

import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.bpm.dal.dataobject.approval.BpmApprovalUrgeRecordDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 催办记录 Mapper
 */
@Mapper
public interface BpmApprovalUrgeRecordMapper extends BaseMapperX<BpmApprovalUrgeRecordDO> {

    /**
     * 根据审批 ID 获取催办记录列表
     *
     * @param approvalId 审批 ID
     * @return 催办记录列表
     */
    default List<BpmApprovalUrgeRecordDO> selectListByApprovalId(String approvalId) {
        return selectList(new LambdaQueryWrapperX<BpmApprovalUrgeRecordDO>()
                .eq(BpmApprovalUrgeRecordDO::getApprovalId, approvalId)
                .orderByDesc(BpmApprovalUrgeRecordDO::getUrgeTime));
    }

    /**
     * 根据任务 ID 获取催办记录列表
     *
     * @param taskId 任务 ID
     * @return 催办记录列表
     */
    default List<BpmApprovalUrgeRecordDO> selectListByTaskId(String taskId) {
        return selectList(new LambdaQueryWrapperX<BpmApprovalUrgeRecordDO>()
                .eq(BpmApprovalUrgeRecordDO::getTaskId, taskId)
                .orderByDesc(BpmApprovalUrgeRecordDO::getUrgeTime));
    }

    /**
     * 查询指定审批 ID 最近一次催办记录
     */
    default BpmApprovalUrgeRecordDO selectLastByApprovalId(String approvalId) {
        return selectOne(new LambdaQueryWrapperX<BpmApprovalUrgeRecordDO>()
                .eq(BpmApprovalUrgeRecordDO::getApprovalId, approvalId)
                .orderByDesc(BpmApprovalUrgeRecordDO::getUrgeTime)
                .last("LIMIT 1"));
    }

}
