package cn.weitee.erp.module.infra.service.file;

public interface FileCleanupCompensationService {

    void record(Long configId, String path, String url, String error);

    String retryPendingCleanups();

}
