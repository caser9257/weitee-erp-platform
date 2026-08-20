package cn.weitee.erp.module.infra.service.file;

import cn.weitee.erp.module.infra.dal.dataobject.file.FileCleanupCompensationDO;
import cn.weitee.erp.module.infra.dal.mysql.file.FileCleanupCompensationMapper;
import cn.weitee.erp.module.infra.framework.file.core.client.FileClient;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class FileCleanupCompensationServiceImpl implements FileCleanupCompensationService {

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_PROCESSING = "PROCESSING";
    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_FAILED = "FAILED";
    private static final int BATCH_SIZE = 100;
    private static final int DEFAULT_MAX_RETRY_COUNT = 10;
    private static final int PROCESSING_TIMEOUT_MINUTES = 10;

    @Resource
    private FileCleanupCompensationMapper compensationMapper;

    @Resource
    private FileConfigService fileConfigService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void record(Long configId, String path, String url, String error) {
        FileCleanupCompensationDO existing = compensationMapper.selectOne(
                new LambdaQueryWrapperX<FileCleanupCompensationDO>()
                        .eq(FileCleanupCompensationDO::getConfigId, configId)
                        .eq(FileCleanupCompensationDO::getPath, path));
        if (existing != null) {
            if (STATUS_SUCCESS.equals(existing.getStatus())) {
                return;
            }
            existing.setStatus(STATUS_PENDING);
            existing.setNextRetryTime(LocalDateTime.now());
            existing.setLastError(error);
            compensationMapper.updateById(existing);
            return;
        }
        compensationMapper.insert(FileCleanupCompensationDO.builder()
                .configId(configId)
                .path(path)
                .url(url)
                .status(STATUS_PENDING)
                .retryCount(0)
                .maxRetryCount(DEFAULT_MAX_RETRY_COUNT)
                .nextRetryTime(LocalDateTime.now())
                .lastError(error)
                .build());
    }

    @Override
    public String retryPendingCleanups() {
        LocalDateTime now = LocalDateTime.now();
        int resetCount = compensationMapper.resetStaleProcessing(
                now.minusMinutes(PROCESSING_TIMEOUT_MINUTES), now);
        List<FileCleanupCompensationDO> records = compensationMapper
                .selectPendingList(now, BATCH_SIZE);
        int successCount = 0;
        int failedCount = 0;
        for (FileCleanupCompensationDO record : records) {
            if (!claim(record.getId(), now)) {
                continue;
            }
            try {
                FileClient client = fileConfigService.getFileClient(record.getConfigId());
                if (client == null) {
                    throw new IllegalStateException("文件客户端不存在");
                }
                client.delete(record.getPath());
                markSuccess(record.getId());
                successCount++;
            } catch (Exception e) {
                markFailure(record, e);
                failedCount++;
            }
        }
        return "清理成功 " + successCount + " 条，失败 " + failedCount + " 条，恢复超时任务 " + resetCount + " 条";
    }

    protected boolean claim(Long id, LocalDateTime now) {
        return compensationMapper.claimPending(id, now) == 1;
    }

    @Transactional(rollbackFor = Exception.class)
    protected void markSuccess(Long id) {
        FileCleanupCompensationDO record = compensationMapper.selectById(id);
        if (record != null) {
            record.setStatus(STATUS_SUCCESS);
            record.setLastError(null);
            compensationMapper.updateById(record);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    protected void markFailure(FileCleanupCompensationDO record, Exception exception) {
        FileCleanupCompensationDO current = compensationMapper.selectById(record.getId());
        if (current == null) {
            return;
        }
        int retryCount = current.getRetryCount() + 1;
        current.setRetryCount(retryCount);
        current.setLastError(exception.getMessage());
        if (retryCount >= current.getMaxRetryCount()) {
            current.setStatus(STATUS_FAILED);
        } else {
            current.setStatus(STATUS_PENDING);
            long delaySeconds = Math.min(3600L, 5L * (1L << Math.min(retryCount - 1, 9)));
            current.setNextRetryTime(LocalDateTime.now().plusSeconds(delaySeconds));
        }
        compensationMapper.updateById(current);
        log.error("[retryPendingCleanups] 远端文件清理失败，id={}, retryCount={}, status={}, path={}",
                current.getId(), retryCount, current.getStatus(), current.getPath(), exception);
    }

}
