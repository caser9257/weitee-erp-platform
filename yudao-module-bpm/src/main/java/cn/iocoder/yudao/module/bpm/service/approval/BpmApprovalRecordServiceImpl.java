package cn.iocoder.yudao.module.bpm.service.approval;

import cn.iocoder.yudao.module.bpm.dal.dataobject.approval.BpmApprovalRecordDO;
import cn.iocoder.yudao.module.bpm.dal.mysql.approval.BpmApprovalRecordMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 审批记录 Service 实现类
 */
@Service
@Validated
public class BpmApprovalRecordServiceImpl implements BpmApprovalRecordService {

    @Resource
    private BpmApprovalRecordMapper approvalRecordMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRecord(BpmApprovalRecordDO record) {
        approvalRecordMapper.insert(record);
        return record.getId();
    }

    @Override
    public List<BpmApprovalRecordDO> getRecordsByApprovalId(String approvalId) {
        return approvalRecordMapper.selectListByApprovalId(approvalId);
    }

    @Override
    public List<BpmApprovalRecordDO> getRecordsByTaskId(String taskId) {
        return approvalRecordMapper.selectListByTaskId(taskId);
    }

}
