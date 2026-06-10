package cn.iocoder.yudao.module.bpm.service.approval;

import cn.iocoder.yudao.module.bpm.dal.dataobject.approval.BpmApprovalRecordDO;

import java.util.List;

/**
 * 审批记录 Service 接口
 */
public interface BpmApprovalRecordService {

    /**
     * 创建审批记录
     *
     * @param record 审批记录
     * @return 记录 ID
     */
    Long createRecord(BpmApprovalRecordDO record);

    /**
     * 根据审批 ID 获取审批记录列表
     *
     * @param approvalId 审批 ID
     * @return 审批记录列表
     */
    List<BpmApprovalRecordDO> getRecordsByApprovalId(String approvalId);

    /**
     * 根据任务 ID 获取审批记录列表
     *
     * @param taskId 任务 ID
     * @return 审批记录列表
     */
    List<BpmApprovalRecordDO> getRecordsByTaskId(String taskId);

}
