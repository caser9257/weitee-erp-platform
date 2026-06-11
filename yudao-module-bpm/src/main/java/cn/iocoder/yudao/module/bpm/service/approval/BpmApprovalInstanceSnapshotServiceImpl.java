package cn.iocoder.yudao.module.bpm.service.approval;

import cn.iocoder.yudao.module.bpm.dal.dataobject.approval.BpmApprovalInstanceSnapshotDO;
import cn.iocoder.yudao.module.bpm.dal.mysql.approval.BpmApprovalInstanceSnapshotMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.APPROVAL_INSTANCE_SNAPSHOT_NOT_EXISTS;

/**
 * 审批运行时快照 Service 实现类
 */
@Service
@Validated
public class BpmApprovalInstanceSnapshotServiceImpl implements BpmApprovalInstanceSnapshotService {

    @Resource
    private BpmApprovalInstanceSnapshotMapper approvalInstanceSnapshotMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSnapshot(BpmApprovalInstanceSnapshotDO snapshot) {
        approvalInstanceSnapshotMapper.insert(snapshot);
        return snapshot.getId();
    }

    @Override
    public BpmApprovalInstanceSnapshotDO getSnapshot(Long id) {
        return approvalInstanceSnapshotMapper.selectById(id);
    }

    @Override
    public BpmApprovalInstanceSnapshotDO getSnapshotByProcessInstanceId(String processInstanceId) {
        return approvalInstanceSnapshotMapper.selectByProcessInstanceId(processInstanceId);
    }

    @Override
    public BpmApprovalInstanceSnapshotDO getSnapshotBySceneCodeAndBizId(String sceneCode, String bizId) {
        return approvalInstanceSnapshotMapper.selectBySceneCodeAndBizId(sceneCode, bizId);
    }

    @Override
    public List<BpmApprovalInstanceSnapshotDO> getSnapshotsByStatus(Integer status) {
        return approvalInstanceSnapshotMapper.selectListByStatus(status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSnapshotStatus(Long id, Integer status, String resultReason) {
        BpmApprovalInstanceSnapshotDO snapshot = approvalInstanceSnapshotMapper.selectById(id);
        if (snapshot == null) {
            throw exception(APPROVAL_INSTANCE_SNAPSHOT_NOT_EXISTS);
        }
        approvalInstanceSnapshotMapper.updateById(new BpmApprovalInstanceSnapshotDO()
                .setId(id)
                .setStatus(status)
                .setResultReason(resultReason));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSnapshotProcessInstanceId(Long id, String processInstanceId) {
        BpmApprovalInstanceSnapshotDO snapshot = approvalInstanceSnapshotMapper.selectById(id);
        if (snapshot == null) {
            throw exception(APPROVAL_INSTANCE_SNAPSHOT_NOT_EXISTS);
        }
        approvalInstanceSnapshotMapper.updateById(new BpmApprovalInstanceSnapshotDO()
                .setId(id)
                .setProcessInstanceId(processInstanceId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSnapshot(Long id) {
        BpmApprovalInstanceSnapshotDO snapshot = approvalInstanceSnapshotMapper.selectById(id);
        if (snapshot == null) {
            throw exception(APPROVAL_INSTANCE_SNAPSHOT_NOT_EXISTS);
        }
        approvalInstanceSnapshotMapper.deleteById(id);
    }

}
