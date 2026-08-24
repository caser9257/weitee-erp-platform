package cn.weitee.erp.module.infra.service.file;

import cn.weitee.erp.module.infra.dal.dataobject.file.FileAccessStatsDO;

import java.time.LocalDate;
import java.util.List;

/**
 * 文件访问统计 Service 接口
 *
 * @author weitee
 */
public interface FileAccessStatsService {

    /**
     * 记录文件查看
     */
    void recordView(Long fileId);

    /**
     * 记录文件下载
     */
    void recordDownload(Long fileId);

    /**
     * 获取文件的访问统璁?
     */
    List<FileAccessStatsDO> getFileAccessStats(Long fileId);

    /**
     * 获取文件在指定日期的统计
     */
    FileAccessStatsDO getFileAccessStatsByDate(Long fileId, LocalDate statsDate);

    /**
     * 获取指定日期范围内的统计
     */
    List<FileAccessStatsDO> getAccessStatsByDateRange(LocalDate startDate, LocalDate endDate);

    /**
     * 获取访问量最多的文件
     */
    List<FileAccessStatsDO> getTopFiles(int limit, LocalDate startDate, LocalDate endDate);

}
