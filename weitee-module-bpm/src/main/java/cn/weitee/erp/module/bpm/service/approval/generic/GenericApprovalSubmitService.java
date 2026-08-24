package cn.weitee.erp.module.bpm.service.approval.generic;

/**
 * 通用审批提交服务
 *
 * 配置驱动的统一提交入口：业务端只需调用 submit(sceneCode, bizId, userId)，
 * 无需编写业务 Service / ContextProvider / ResultHandler。
 * 遵循事务铁律：事务内写 SUBMIT 状态，afterCommit 启动 BPM，失败回写 FAILED。
 */
public interface GenericApprovalSubmitService {

    /**
     * 提交审批
     *
     * @param sceneCode 场景编码（需已启用 generic_config）
     * @param bizId     业务单据 ID
     * @param userId    发起人 ID
     * @return 流程实例 ID；启动异步进行时返回快照批准标识
     */
    String submit(String sceneCode, Long bizId, Long userId);

    /**
     * 撤回审批
     *
     * @param sceneCode 场景编码
     * @param bizId     业务单据 ID
     * @param userId    撤回人 ID
     * @param reason    撤回原因
     */
    void cancel(String sceneCode, Long bizId, Long userId, String reason);

}
