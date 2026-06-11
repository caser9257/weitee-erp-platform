package cn.iocoder.yudao.module.bpm.service.approval;

import cn.iocoder.yudao.module.bpm.dal.dataobject.approval.BpmApprovalAuditLogDO;

import java.util.List;

/**
 * 审批审计日志 Service 接口
 */
public interface BpmApprovalAuditLogService {

    /**
     * 创建审计日志
     *
     * @param auditLog 审计日志
     * @return 日志 ID
     */
    Long createAuditLog(BpmApprovalAuditLogDO auditLog);

    /**
     * 根据审批 ID 获取审计日志列表
     *
     * @param approvalId 审批 ID
     * @return 审计日志列表
     */
    List<BpmApprovalAuditLogDO> getAuditLogsByApprovalId(String approvalId);

    /**
     * 根据任务 ID 获取审计日志列表
     *
     * @param taskId 任务 ID
     * @return 审计日志列表
     */
    List<BpmApprovalAuditLogDO> getAuditLogsByTaskId(String taskId);

    /**
     * 根据操作人 ID 获取审计日志列表
     *
     * @param operatorUserId 操作人 ID
     * @return 审计日志列表
     */
    List<BpmApprovalAuditLogDO> getAuditLogsByOperatorUserId(Long operatorUserId);

}
