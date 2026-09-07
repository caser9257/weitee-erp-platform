package cn.weitee.erp.module.erp.service.mrp;

/**
 * 制造 BOM 生命周期审批 Service 接口
 *
 * 制造 BOM 的停用/废止必须经过 BPM 申请；结构维护唯一来源为研发 BOM 审批发布。
 */
public interface ErpBomLifecycleBpmService {

    /**
     * 发起制造 BOM 停用（废止）申请
     *
     * @param userId 发起人
     * @param bomId  制造 BOM 编号
     * @param reason 申请原因
     * @return 流程实例编号（异步创建，失败回写快照 FAILED）
     */
    String submitDisableApproval(Long userId, Long bomId, String reason);

    /**
     * 撤回停用申请（仅审批中可撤回；终态由 Handler.onCancel 统一处理）
     */
    void cancelDisableApproval(Long userId, Long bomId, String reason);

}
