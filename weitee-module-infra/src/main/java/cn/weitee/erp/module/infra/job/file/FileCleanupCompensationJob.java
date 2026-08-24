package cn.weitee.erp.module.infra.job.file;

import cn.weitee.erp.framework.quartz.core.handler.JobHandler;
import cn.weitee.erp.module.infra.service.file.FileCleanupCompensationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

@Component
@Slf4j
public class FileCleanupCompensationJob implements JobHandler {

    @Resource
    private FileCleanupCompensationService compensationService;

    @Override
    public String execute(String param) {
        String result = compensationService.retryPendingCleanups();
        log.info("[execute][文件清理补偿任务完成，{}]", result);
        return result;
    }

}
