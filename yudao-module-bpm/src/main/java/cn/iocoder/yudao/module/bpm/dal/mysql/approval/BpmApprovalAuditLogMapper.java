package cn.iocoder.yudao.module.bpm.dal.mysql.approval;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.bpm.dal.dataobject.approval.BpmApprovalAuditLogDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 审批审计日志 Mapper
 */
@Mapper
public interface BpmApprovalAuditLogMapper extends BaseMapperX<BpmApprovalAuditLogDO> {

    /**
     * 根据审批 ID 获取审计日志列表
     *
     * @param approvalId 审批 ID
     * @return 审计日志列表
     */
    default List<BpmApprovalAuditLogDO> selectListByApprovalId(String approvalId) {
        return selectList(new LambdaQueryWrapperX<BpmApprovalAuditLogDO>()
                .eq(BpmApprovalAuditLogDO::getApprovalId, approvalId)
                .orderByDesc(BpmApprovalAuditLogDO::getCreateTime));
    }

    /**
     * 根据任务 ID 获取审计日志列表
     *
     * @param taskId 任务 ID
     * @return 审计日志列表
     */
    default List<BpmApprovalAuditLogDO> selectListByTaskId(String taskId) {
        return selectList(new LambdaQueryWrapperX<BpmApprovalAuditLogDO>()
                .eq(BpmApprovalAuditLogDO::getTaskId, taskId)
                .orderByDesc(BpmApprovalAuditLogDO::getCreateTime));
    }

    /**
     * 根据操作人 ID 获取审计日志列表
     *
     * @param operatorUserId 操作人 ID
     * @return 审计日志列表
     */
    default List<BpmApprovalAuditLogDO> selectListByOperatorUserId(Long operatorUserId) {
        return selectList(new LambdaQueryWrapperX<BpmApprovalAuditLogDO>()
                .eq(BpmApprovalAuditLogDO::getOperatorUserId, operatorUserId)
                .orderByDesc(BpmApprovalAuditLogDO::getCreateTime));
    }

}
