package cn.weitee.erp.module.infra.service.file;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.infra.controller.admin.file.vo.log.FileOperationLogPageReqVO;
import cn.weitee.erp.module.infra.dal.dataobject.file.FileOperationLogDO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 文件操作日志 Service 接口
 *
 * @author weitee
 */
public interface FileOperationLogService {

    /**
     * 记录文件操作日志
     *
     * @param fileId      文件ID
     * @param fileName    文件名称
     * @param operation   操作类型
     * @param description 操作说明
     * @param result      操作结果�?-成功, 1-失败
     * @param failReason  失败原因
     * @return 日志ID
     */
    Long log(Long fileId, String fileName, String operation, String description, Integer result, String failReason);

    /**
     * 记录成功的文件操作日�?
     */
    Long logSuccess(Long fileId, String fileName, String operation, String description);

    /**
     * 记录失败的文件操作日�?
     */
    Long logFail(Long fileId, String fileName, String operation, String description, String failReason);

    /**
     * 获取文件操作日志分页
     */
    PageResult<FileOperationLogDO> getFileOperationLogPage(FileOperationLogPageReqVO reqVO);

    /**
     * 获取文件的操作日志列�?
     */
    List<FileOperationLogDO> getFileOperationLogs(Long fileId);

    /**
     * 统计文件操作次数
     */
    Map<String, Long> countByOperation(LocalDateTime startTime, LocalDateTime endTime);

}
