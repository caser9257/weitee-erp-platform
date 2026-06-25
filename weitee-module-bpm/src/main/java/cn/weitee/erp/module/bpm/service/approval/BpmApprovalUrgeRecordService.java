package cn.weitee.erp.module.bpm.service.approval;

import cn.weitee.erp.module.bpm.dal.dataobject.approval.BpmApprovalUrgeRecordDO;

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

    /**
     * 检查催办是否允许（频率限制：同一审批 5 分钟内不允许重复催办）
     *
     * @param approvalId 审批 ID
     * @return true=允许催办，false=过于频繁
     */
    boolean isUrgeAllowed(String approvalId);

}
