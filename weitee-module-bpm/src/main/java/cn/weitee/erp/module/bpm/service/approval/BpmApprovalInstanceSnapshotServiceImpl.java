package cn.weitee.erp.module.bpm.service.approval;

import cn.weitee.erp.module.bpm.dal.dataobject.approval.BpmApprovalInstanceSnapshotDO;
import cn.weitee.erp.module.bpm.dal.mysql.approval.BpmApprovalInstanceSnapshotMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.List;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.bpm.enums.ErrorCodeConstants.APPROVAL_INSTANCE_SNAPSHOT_NOT_EXISTS;
import static cn.weitee.erp.module.bpm.enums.ErrorCodeConstants.APPROVAL_INSTANCE_SNAPSHOT_STATUS_CONFLICT;

/**
 * 审批运行时快照 Service 实现类
 */
@Service
@Validated
@Slf4j
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
    public BpmApprovalInstanceSnapshotDO getEffectiveSnapshotBySceneCodeAndBizId(String sceneCode, String bizId) {
        return approvalInstanceSnapshotMapper.selectEffectiveBySceneCodeAndBizId(sceneCode, bizId);
    }

    @Override
    public BpmApprovalInstanceSnapshotDO getSnapshotByApprovalId(String approvalId) {
        return approvalInstanceSnapshotMapper.selectByApprovalId(approvalId);
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
        // CAS 更新：只有当前状态与预期一致时才更新
        int updateCount = approvalInstanceSnapshotMapper.updateByIdAndStatus(id, snapshot.getStatus(),
                new BpmApprovalInstanceSnapshotDO()
                        .setId(id)
                        .setStatus(status)
                        .setResultReason(resultReason));
        if (updateCount == 0) {
            log.warn("[updateSnapshotStatus] 快照状态已被其他事务修改，略过，id={}, expectedStatus={}", id, snapshot.getStatus());
            throw exception(APPROVAL_INSTANCE_SNAPSHOT_STATUS_CONFLICT);
        }
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
        approvalInstanceSnapshotMapper.hardDeleteById(id);
    }

}
