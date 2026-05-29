package cn.iocoder.yudao.module.bpm.service.approval.provider;

/**
 * 审批上下文提供者接口
 *
 * 业务模块需要实现此接口，根据 sceneCode + bizId 提供审批上下文
 */
public interface ApprovalContextProvider {

    /**
     * 返回此 Provider 支持的场景编码
     *
     * @return 场景编码，如 erp.finance.payment.submit
     */
    String getSceneCode();

    /**
     * 根据业务 ID 获取审批上下文
     *
     * @param bizId 业务单据 ID
     * @return 审批上下文
     */
    ApprovalContext getContext(Long bizId);

}
