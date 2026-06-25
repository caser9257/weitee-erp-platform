package cn.weitee.erp.module.bpm.service.approval;

import cn.weitee.erp.module.bpm.dal.dataobject.approval.BpmApprovalUrgeRecordDO;
import cn.weitee.erp.module.bpm.dal.mysql.approval.BpmApprovalUrgeRecordMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 催办记录 Service 实现类
 */
@Service
@Validated
public class BpmApprovalUrgeRecordServiceImpl implements BpmApprovalUrgeRecordService {

    /**
     * 催办频率限制：同一审批 5 分钟内不允许重复催办
     */
    private static final long URGE_COOLDOWN_MILLIS = 5 * 60 * 1000L;

    @Resource
    private BpmApprovalUrgeRecordMapper approvalUrgeRecordMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRecord(BpmApprovalUrgeRecordDO record) {
        approvalUrgeRecordMapper.insert(record);
        return record.getId();
    }

    @Override
    public List<BpmApprovalUrgeRecordDO> getRecordsByApprovalId(String approvalId) {
        return approvalUrgeRecordMapper.selectListByApprovalId(approvalId);
    }

    @Override
    public List<BpmApprovalUrgeRecordDO> getRecordsByTaskId(String taskId) {
        return approvalUrgeRecordMapper.selectListByTaskId(taskId);
    }

    @Override
    public boolean isUrgeAllowed(String approvalId) {
        BpmApprovalUrgeRecordDO lastRecord = approvalUrgeRecordMapper.selectLastByApprovalId(approvalId);
        if (lastRecord == null) {
            return true;
        }
        long elapsed = new Date().getTime() - lastRecord.getUrgeTime().getTime();
        return elapsed >= URGE_COOLDOWN_MILLIS;
    }

}
