package cn.iocoder.yudao.module.bpm.service.approval;

import cn.iocoder.yudao.module.bpm.dal.dataobject.approval.BpmApprovalUrgeRecordDO;
import cn.iocoder.yudao.module.bpm.dal.mysql.approval.BpmApprovalUrgeRecordMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 催办记录 Service 实现类
 */
@Service
@Validated
public class BpmApprovalUrgeRecordServiceImpl implements BpmApprovalUrgeRecordService {

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

}
