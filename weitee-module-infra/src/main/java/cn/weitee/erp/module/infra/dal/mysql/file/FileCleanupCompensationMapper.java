package cn.weitee.erp.module.infra.dal.mysql.file;

import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.infra.dal.dataobject.file.FileCleanupCompensationDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface FileCleanupCompensationMapper extends BaseMapperX<FileCleanupCompensationDO> {

    default List<FileCleanupCompensationDO> selectPendingList(LocalDateTime now, int limit) {
        return selectList(new LambdaQueryWrapperX<FileCleanupCompensationDO>()
                .eq(FileCleanupCompensationDO::getStatus, "PENDING")
                .le(FileCleanupCompensationDO::getNextRetryTime, now)
                .orderByAsc(FileCleanupCompensationDO::getNextRetryTime)
                .last("LIMIT " + limit));
    }

    default int resetStaleProcessing(LocalDateTime deadline, LocalDateTime now) {
        FileCleanupCompensationDO update = new FileCleanupCompensationDO();
        update.setStatus("PENDING");
        update.setNextRetryTime(now);
        return update(update, new LambdaUpdateWrapper<FileCleanupCompensationDO>()
                .eq(FileCleanupCompensationDO::getStatus, "PROCESSING")
                .lt(FileCleanupCompensationDO::getUpdateTime, deadline));
    }

    default int claimPending(Long id, LocalDateTime now) {
        FileCleanupCompensationDO update = new FileCleanupCompensationDO();
        update.setStatus("PROCESSING");
        return update(update, new LambdaUpdateWrapper<FileCleanupCompensationDO>()
                .eq(FileCleanupCompensationDO::getId, id)
                .eq(FileCleanupCompensationDO::getStatus, "PENDING")
                .le(FileCleanupCompensationDO::getNextRetryTime, now));
    }

}
