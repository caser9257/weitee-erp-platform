package cn.iocoder.yudao.module.erp.service.finance;

import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceAuditOperationLogDO;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinanceAuditOperationLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

/**
 * 审计操作日志服务实现
 */
@Slf4j
@Service
public class ErpFinanceAuditOperationLogServiceImpl implements ErpFinanceAuditOperationLogService {

    @Resource
    private ErpFinanceAuditOperationLogMapper auditOperationLogMapper;

    @Override
    public void logOperation(Long userId, String operationType, String operationDesc,
                             String targetType, Long targetId, Long ledgerId,
                             String queryParams, Integer resultCount) {
        ErpFinanceAuditOperationLogDO logDO = ErpFinanceAuditOperationLogDO.builder()
                .userId(userId)
                .operationType(operationType)
                .operationDesc(operationDesc)
                .targetType(targetType)
                .targetId(targetId)
                .ledgerId(ledgerId)
                .queryParams(queryParams)
                .resultCount(resultCount)
                .build();
        auditOperationLogMapper.insert(logDO);
        log.info("审计操作日志记录。userId={}, type={}, desc={}", userId, operationType, operationDesc);
    }

    @Override
    public void logQuery(Long userId, String operationDesc, Long ledgerId, String queryParams, Integer resultCount) {
        logOperation(userId, "query", operationDesc, null, null, ledgerId, queryParams, resultCount);
    }

    @Override
    public void logExport(Long userId, String operationDesc, Long ledgerId, String queryParams, Integer resultCount) {
        logOperation(userId, "export", operationDesc, null, null, ledgerId, queryParams, resultCount);
    }

    @Override
    public void logTrace(Long userId, String targetType, Long targetId, Long ledgerId) {
        logOperation(userId, "trace", "穿透追踪", targetType, targetId, ledgerId, null, null);
    }

}
