package cn.iocoder.yudao.module.bpm.dal.mysql.approval;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.bpm.dal.dataobject.approval.BpmApprovalRecordDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 审批记录 Mapper
 */
@Mapper
public interface BpmApprovalRecordMapper extends BaseMapperX<BpmApprovalRecordDO> {

    /**
     * 根据审批 ID 获取审批记录列表
     *
     * @param approvalId 审批 ID
     * @return 审批记录列表
     */
    default List<BpmApprovalRecordDO> selectListByApprovalId(String approvalId) {
        return selectList(new LambdaQueryWrapperX<BpmApprovalRecordDO>()
                .eq(BpmApprovalRecordDO::getApprovalId, approvalId)
                .orderByDesc(BpmApprovalRecordDO::getCreateTime));
    }

    /**
     * 根据任务 ID 获取审批记录列表
     *
     * @param taskId 任务 ID
     * @return 审批记录列表
     */
    default List<BpmApprovalRecordDO> selectListByTaskId(String taskId) {
        return selectList(new LambdaQueryWrapperX<BpmApprovalRecordDO>()
                .eq(BpmApprovalRecordDO::getTaskId, taskId)
                .orderByDesc(BpmApprovalRecordDO::getCreateTime));
    }

}
