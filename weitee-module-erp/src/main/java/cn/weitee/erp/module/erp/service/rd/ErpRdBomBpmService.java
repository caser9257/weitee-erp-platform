package cn.weitee.erp.module.erp.service.rd;

/**
 * 研发 BOM 审批 Service
 */
public interface ErpRdBomBpmService {

    /**
     * 提交审批
     *
     * @param userId 当前用户
     * @param bomId  研发 BOM 编号
     * @return 占位（实际流程实例在 afterCommit 中异步创建，返回 null）
     */
    String submitRdBom(Long userId, Long bomId);

    /**
     * 撤回审批
     *
     * @param userId 当前用户
     * @param bomId  研发 BOM 编号
     * @param reason 撤回原因
     */
    void cancelRdBomApproval(Long userId, Long bomId, String reason);

}
