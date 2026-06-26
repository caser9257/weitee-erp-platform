package cn.weitee.erp.module.infra.service.file;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.security.core.util.SecurityFrameworkUtils;
import cn.weitee.erp.module.infra.controller.admin.file.vo.log.FileOperationLogPageReqVO;
import cn.weitee.erp.module.infra.dal.dataobject.file.FileOperationLogDO;
import cn.weitee.erp.module.infra.dal.mysql.file.FileOperationLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 文件操作日志 Service 实现
 *
 * @author ruoyi-vue-pro
 */
@Service
@Validated
@Slf4j
public class FileOperationLogServiceImpl implements FileOperationLogService {

    @Resource
    private FileOperationLogMapper fileOperationLogMapper;

    @Resource
    private HttpServletRequest request;

    @Override
    public Long log(Long fileId, String fileName, String operation, String description, Integer result, String failReason) {
        // 获取当前用户信息
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        String userName = SecurityFrameworkUtils.getLoginUserNickname();

        // 获取请求信息
        String ip = getClientIp();
        String userAgent = request.getHeader("User-Agent");

        // 构建日志对象
        FileOperationLogDO logDO = FileOperationLogDO.builder()
                .fileId(fileId)
                .fileName(fileName)
                .operation(operation)
                .userId(userId)
                .userName(userName)
                .ip(ip)
                .userAgent(userAgent)
                .description(description)
                .result(result)
                .failReason(failReason)
                .build();

        fileOperationLogMapper.insert(logDO);
        return logDO.getId();
    }

    @Override
    public Long logSuccess(Long fileId, String fileName, String operation, String description) {
        return log(fileId, fileName, operation, description, 0, null);
    }

    @Override
    public Long logFail(Long fileId, String fileName, String operation, String description, String failReason) {
        return log(fileId, fileName, operation, description, 1, failReason);
    }

    @Override
    public PageResult<FileOperationLogDO> getFileOperationLogPage(FileOperationLogPageReqVO reqVO) {
        return fileOperationLogMapper.selectPage(reqVO);
    }

    @Override
    public List<FileOperationLogDO> getFileOperationLogs(Long fileId) {
        return fileOperationLogMapper.selectListByFileId(fileId);
    }

    @Override
    public Map<String, Long> countByOperation(LocalDateTime startTime, LocalDateTime endTime) {
        return fileOperationLogMapper.countByOperation(startTime, endTime);
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp() {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时，第一个IP为客户端真实IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

}
