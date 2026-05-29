package cn.iocoder.yudao.module.bpm.service.approval;

import cn.iocoder.yudao.module.bpm.dal.dataobject.approval.BpmApprovalInstanceSnapshotDO;

/**
 * 审批运行时快照 Service 接口
 */
public interface BpmApprovalInstanceSnapshotService {

    /**
     * 创建快照
     */
    Long createSnapshot(BpmApprovalInstanceSnapshotDO snapshot);

    /**
     * 获取快照
     */
    BpmApprovalInstanceSnapshotDO getSnapshot(Long id);

    /**
     * 根据流程实例 ID 获取快照
     */
    BpmApprovalInstanceSnapshotDO getSnapshotByProcessInstanceId(String processInstanceId);

    /**
     * 根据场景编码和业务 ID 获取快照
     */
    BpmApprovalInstanceSnapshotDO getSnapshotBySceneCodeAndBizId(String sceneCode, String bizId);

    /**
     * 更新快照状态
     */
    void updateSnapshotStatus(Long id, Integer status, String resultReason);

    /**
     * 更新快照流程实例 ID
     */
    void updateSnapshotProcessInstanceId(Long id, String processInstanceId);

}
