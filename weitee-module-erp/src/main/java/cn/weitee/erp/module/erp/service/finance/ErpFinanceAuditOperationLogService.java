package cn.weitee.erp.module.erp.service.finance;

/**
 * 审计操作日志服务接口
 */
public interface ErpFinanceAuditOperationLogService {

    /**
     * 记录审计操作日志
     *
     * @param userId        操作人ID
     * @param operationType 操作类型
     * @param operationDesc 操作描述
     * @param targetType    操作对象类型
     * @param targetId      操作对象ID
     * @param ledgerId      账簿ID
     * @param queryParams   查询参数
     * @param resultCount   结果数量
     */
    void logOperation(Long userId, String operationType, String operationDesc,
                      String targetType, Long targetId, Long ledgerId,
                      String queryParams, Integer resultCount);

    /**
     * 记录查询操作
     */
    void logQuery(Long userId, String operationDesc, Long ledgerId, String queryParams, Integer resultCount);

    /**
     * 记录导出操作
     */
    void logExport(Long userId, String operationDesc, Long ledgerId, String queryParams, Integer resultCount);

    /**
     * 记录穿透追踪操作
     */
    void logTrace(Long userId, String targetType, Long targetId, Long ledgerId);

}
