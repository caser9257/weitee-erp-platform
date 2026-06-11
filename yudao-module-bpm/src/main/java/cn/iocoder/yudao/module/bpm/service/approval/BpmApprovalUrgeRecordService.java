package cn.iocoder.yudao.module.bpm.service.approval;

import cn.iocoder.yudao.module.bpm.dal.dataobject.approval.BpmApprovalUrgeRecordDO;

import java.util.List;

/**
 * 催办记录 Service 接口
 */
public interface BpmApprovalUrgeRecordService {

    /**
     * 创建催办记录
     *
     * @param record 催办记录
     * @return 记录 ID
     */
    Long createRecord(BpmApprovalUrgeRecordDO record);

    /**
     * 根据审批 ID 获取催办记录列表
     *
     * @param approvalId 审批 ID
     * @return 催办记录列表
     */
    List<BpmApprovalUrgeRecordDO> getRecordsByApprovalId(String approvalId);

    /**
     * 根据任务 ID 获取催办记录列表
     *
     * @param taskId 任务 ID
     * @return 催办记录列表
     */
    List<BpmApprovalUrgeRecordDO> getRecordsByTaskId(String taskId);

}
