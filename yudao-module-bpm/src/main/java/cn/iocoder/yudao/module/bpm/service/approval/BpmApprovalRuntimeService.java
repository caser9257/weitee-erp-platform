package cn.iocoder.yudao.module.bpm.service.approval;

import cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmApprovalDetailRespVO;

import java.util.List;

/**
 * 统一审批运行时门面接口
 *
 * 收敛审批提交/撤回逻辑，业务模块通过此接口发起审批
 */
public interface BpmApprovalRuntimeService {

    /**
     * 提交审批
     *
     * @param sceneCode 场景编码
     * @param bizId     业务单据 ID
     * @param userId    发起人 ID
     * @return 流程实例 ID
     */
    String submit(String sceneCode, Long bizId, Long userId);

    /**
     * 获取审批详情
     */
    BpmApprovalDetailRespVO getApprovalDetail(String sceneCode, Long bizId);

    /**
     * 获取审批轨迹
     */
    List<BpmApprovalDetailRespVO.ActivityNode> getApprovalTrail(String sceneCode, Long bizId);

    /**
     * 撤回审批
     *
     * @param sceneCode 场景编码
     * @param bizId     业务单据 ID
     * @param userId    撤回人 ID
     * @param reason    撤回原因
     */
    void cancel(String sceneCode, Long bizId, Long userId, String reason);

    /**
     * 分发审批结果事件
     */
    void dispatchResult(BpmProcessInstanceStatusEvent event);

}
