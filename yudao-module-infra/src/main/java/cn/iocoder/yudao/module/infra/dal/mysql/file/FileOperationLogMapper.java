package cn.iocoder.yudao.module.infra.dal.mysql.file;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.infra.controller.admin.file.vo.log.FileOperationLogPageReqVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.file.FileOperationLogDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 文件操作日志 Mapper
 *
 * @author ruoyi-vue-pro
 */
@Mapper
public interface FileOperationLogMapper extends BaseMapperX<FileOperationLogDO> {

    default PageResult<FileOperationLogDO> selectPage(FileOperationLogPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<FileOperationLogDO>()
                .eqIfPresent(FileOperationLogDO::getFileId, reqVO.getFileId())
                .eqIfPresent(FileOperationLogDO::getOperation, reqVO.getOperation())
                .eqIfPresent(FileOperationLogDO::getUserId, reqVO.getUserId())
                .eqIfPresent(FileOperationLogDO::getResult, reqVO.getResult())
                .betweenIfPresent(FileOperationLogDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(FileOperationLogDO::getCreateTime));
    }

    /**
     * 统计文件操作次数
     */
    default Map<String, Long> countByOperation(LocalDateTime startTime, LocalDateTime endTime) {
        return selectMaps(new LambdaQueryWrapperX<FileOperationLogDO>()
                .betweenIfPresent(FileOperationLogDO::getCreateTime, startTime, endTime)
                .select(FileOperationLogDO::getOperation)
                .groupBy(FileOperationLogDO::getOperation));
    }

    /**
     * 获取文件的操作日志
     */
    default List<FileOperationLogDO> selectListByFileId(Long fileId) {
        return selectList(new LambdaQueryWrapperX<FileOperationLogDO>()
                .eq(FileOperationLogDO::getFileId, fileId)
                .orderByDesc(FileOperationLogDO::getCreateTime));
    }

}
