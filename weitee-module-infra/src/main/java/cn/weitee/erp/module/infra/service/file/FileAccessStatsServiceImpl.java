package cn.weitee.erp.module.infra.service.file;

import cn.weitee.erp.module.infra.dal.dataobject.file.FileAccessStatsDO;
import cn.weitee.erp.module.infra.dal.dataobject.file.FileDO;
import cn.weitee.erp.module.infra.dal.mysql.file.FileAccessStatsMapper;
import cn.weitee.erp.module.infra.dal.mysql.file.FileMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 文件访问统计 Service 实现
 *
 * @author weitee
 */
@Service
@Validated
@Slf4j
public class FileAccessStatsServiceImpl implements FileAccessStatsService {

    @Resource
    private FileAccessStatsMapper fileAccessStatsMapper;

    @Resource
    private FileMapper fileMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordView(Long fileId) {
        // 1. 更新文件表的查看次数
        FileDO file = fileMapper.selectById(fileId);
        if (file == null) {
            return;
        }
        file.setViewCount((file.getViewCount() == null ? 0 : file.getViewCount()) + 1);
        file.setLastAccessTime(LocalDateTime.now());
        fileMapper.updateById(file);

        // 2. 更新或创建每日统计记�?
        LocalDate today = LocalDate.now();
        FileAccessStatsDO stats = fileAccessStatsMapper.selectByFileIdAndDate(fileId, today);
        if (stats == null) {
            stats = FileAccessStatsDO.builder()
                    .fileId(fileId)
                    .statsDate(today)
                    .viewCount(1)
                    .downloadCount(0)
                    .uniqueVisitorCount(1)
                    .build();
            fileAccessStatsMapper.insert(stats);
        } else {
            stats.setViewCount(stats.getViewCount() + 1);
            fileAccessStatsMapper.updateById(stats);
        }

        log.debug("[recordView] 记录文件查看，fileId={}, viewCount={}", fileId, file.getViewCount());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordDownload(Long fileId) {
        // 1. 更新文件表的下载次数
        FileDO file = fileMapper.selectById(fileId);
        if (file == null) {
            return;
        }
        file.setDownloadCount((file.getDownloadCount() == null ? 0 : file.getDownloadCount()) + 1);
        file.setLastAccessTime(LocalDateTime.now());
        fileMapper.updateById(file);

        // 2. 更新或创建每日统计记�?
        LocalDate today = LocalDate.now();
        FileAccessStatsDO stats = fileAccessStatsMapper.selectByFileIdAndDate(fileId, today);
        if (stats == null) {
            stats = FileAccessStatsDO.builder()
                    .fileId(fileId)
                    .statsDate(today)
                    .viewCount(0)
                    .downloadCount(1)
                    .uniqueVisitorCount(1)
                    .build();
            fileAccessStatsMapper.insert(stats);
        } else {
            stats.setDownloadCount(stats.getDownloadCount() + 1);
            fileAccessStatsMapper.updateById(stats);
        }

        log.debug("[recordDownload] 记录文件下载，fileId={}, downloadCount={}", fileId, file.getDownloadCount());
    }

    @Override
    public List<FileAccessStatsDO> getFileAccessStats(Long fileId) {
        return fileAccessStatsMapper.selectListByFileId(fileId);
    }

    @Override
    public FileAccessStatsDO getFileAccessStatsByDate(Long fileId, LocalDate statsDate) {
        return fileAccessStatsMapper.selectByFileIdAndDate(fileId, statsDate);
    }

    @Override
    public List<FileAccessStatsDO> getAccessStatsByDateRange(LocalDate startDate, LocalDate endDate) {
        return fileAccessStatsMapper.selectListByDateRange(startDate, endDate);
    }

    @Override
    public List<FileAccessStatsDO> getTopFiles(int limit, LocalDate startDate, LocalDate endDate) {
        return fileAccessStatsMapper.selectTopFiles(limit, startDate, endDate);
    }

}
