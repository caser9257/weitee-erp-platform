package cn.weitee.erp.module.bpm.service.approval.provider;

/**
 * 审批上下文提供者接口
 *
 * 业务模块需要实现此接口，根据 bizId 提供审批上下文。
 * 每个 Provider 通过 {@link #getSceneCode()} 绑定唯一场景，因此 bizId 足以定位上下文。
 */
public interface ApprovalContextProvider {

    /**
     * 返回此 Provider 支持的场景编码
     *
     * 每个 Provider 只服务一个场景，运行时通过 sceneCode 路由到对应 Provider。
     *
     * @return 场景编码，如 erp.finance.payment.submit
     */
    String getSceneCode();

    /**
     * 根据业务 ID 获取审批上下文
     *
     * 调用方已通过 {@link #getSceneCode()} 确定场景，因此此处只需 bizId。
     *
     * @param bizId 业务单据 ID
     * @return 审批上下文，包含表单变量、标题等信息
     */
    ApprovalContext getContext(Long bizId);

}
