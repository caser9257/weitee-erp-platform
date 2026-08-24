package cn.weitee.erp.module.infra.dal.mysql.file;

import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.infra.dal.dataobject.file.FileAccessStatsDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.List;

/**
 * 文件访问统计 Mapper
 *
 * @author weitee
 */
@Mapper
public interface FileAccessStatsMapper extends BaseMapperX<FileAccessStatsDO> {

    /**
     * 获取文件的访问统璁?
     */
    default List<FileAccessStatsDO> selectListByFileId(Long fileId) {
        return selectList(new LambdaQueryWrapperX<FileAccessStatsDO>()
                .eq(FileAccessStatsDO::getFileId, fileId)
                .orderByDesc(FileAccessStatsDO::getStatsDate));
    }

    /**
     * 获取文件在指定日期的统计
     */
    default FileAccessStatsDO selectByFileIdAndDate(Long fileId, LocalDate statsDate) {
        return selectOne(new LambdaQueryWrapperX<FileAccessStatsDO>()
                .eq(FileAccessStatsDO::getFileId, fileId)
                .eq(FileAccessStatsDO::getStatsDate, statsDate));
    }

    /**
     * 获取指定日期范围内的统计
     */
    default List<FileAccessStatsDO> selectListByDateRange(LocalDate startDate, LocalDate endDate) {
        return selectList(new LambdaQueryWrapperX<FileAccessStatsDO>()
                .geIfPresent(FileAccessStatsDO::getStatsDate, startDate)
                .leIfPresent(FileAccessStatsDO::getStatsDate, endDate)
                .orderByDesc(FileAccessStatsDO::getStatsDate));
    }

    /**
     * 获取访问量最多的文件
     */
    default List<FileAccessStatsDO> selectTopFiles(int limit, LocalDate startDate, LocalDate endDate) {
        return selectList(new LambdaQueryWrapperX<FileAccessStatsDO>()
                .geIfPresent(FileAccessStatsDO::getStatsDate, startDate)
                .leIfPresent(FileAccessStatsDO::getStatsDate, endDate)
                .groupBy(FileAccessStatsDO::getFileId)
                .last("ORDER BY SUM(view_count + download_count) DESC LIMIT " + limit));
    }

}
