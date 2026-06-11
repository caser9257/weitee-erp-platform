package cn.iocoder.yudao.module.bpm.service.approval;

import cn.iocoder.yudao.module.bpm.dal.dataobject.approval.BpmApprovalAuditLogDO;
import cn.iocoder.yudao.module.bpm.dal.mysql.approval.BpmApprovalAuditLogMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 审批审计日志 Service 实现类
 */
@Service
@Validated
public class BpmApprovalAuditLogServiceImpl implements BpmApprovalAuditLogService {

    @Resource
    private BpmApprovalAuditLogMapper approvalAuditLogMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createAuditLog(BpmApprovalAuditLogDO auditLog) {
        approvalAuditLogMapper.insert(auditLog);
        return auditLog.getId();
    }

    @Override
    public List<BpmApprovalAuditLogDO> getAuditLogsByApprovalId(String approvalId) {
        return approvalAuditLogMapper.selectListByApprovalId(approvalId);
    }

    @Override
    public List<BpmApprovalAuditLogDO> getAuditLogsByTaskId(String taskId) {
        return approvalAuditLogMapper.selectListByTaskId(taskId);
    }

    @Override
    public List<BpmApprovalAuditLogDO> getAuditLogsByOperatorUserId(Long operatorUserId) {
        return approvalAuditLogMapper.selectListByOperatorUserId(operatorUserId);
    }

}
