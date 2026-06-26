package cn.weitee.erp.module.infra.dal.mysql.file;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.infra.controller.admin.file.vo.log.FileOperationLogPageReqVO;
import cn.weitee.erp.module.infra.dal.dataobject.file.FileOperationLogDO;
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
        List<Map<String, Object>> list = selectMaps(new LambdaQueryWrapperX<FileOperationLogDO>()
                .betweenIfPresent(FileOperationLogDO::getCreateTime, startTime, endTime)
                .select(FileOperationLogDO::getOperation)
                .groupBy(FileOperationLogDO::getOperation));
        java.util.Map<String, Long> result = new java.util.LinkedHashMap<>();
        for (Map<String, Object> map : list) {
            String operation = (String) map.get("operation");
            Long count = ((Number) map.get("count")).longValue();
            result.put(operation, count);
        }
        return result;
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
