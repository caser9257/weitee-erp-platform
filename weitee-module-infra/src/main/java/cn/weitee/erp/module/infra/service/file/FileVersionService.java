package cn.weitee.erp.module.infra.service.file;

import cn.weitee.erp.module.infra.dal.dataobject.file.FileVersionDO;

import java.util.List;

/**
 * 文件版本 Service 接口
 *
 * @author weitee
 */
public interface FileVersionService {

    /**
     * 保存文件版本
     */
    Long saveFileVersion(Long fileId, String name, String url, Long size, String type, String description);

    /**
     * 获取文件版本列表
     */
    List<FileVersionDO> getFileVersions(Long fileId);

    /**
     * 获取最新版�?
     */
    Integer getLatestVersion(Long fileId);

    /**
     * 回溯到指定版�?
     */
    void rollbackToVersion(Long fileId, Integer version);

}
