package cn.weitee.erp.module.infra.service.file;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.http.HttpUtils;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.framework.security.core.util.SecurityFrameworkUtils;
import cn.weitee.erp.module.infra.controller.admin.file.vo.file.FileCreateReqVO;
import cn.weitee.erp.module.infra.controller.admin.file.vo.file.FilePageReqVO;
import cn.weitee.erp.module.infra.controller.admin.file.vo.file.FilePresignedUrlRespVO;
import cn.weitee.erp.module.infra.dal.dataobject.file.FileDO;
import cn.weitee.erp.module.infra.dal.mysql.file.FileMapper;
import cn.weitee.erp.module.infra.framework.drm.DrmProperties;
import cn.weitee.erp.module.infra.framework.file.core.client.FileClient;
import cn.weitee.erp.module.infra.framework.file.core.utils.FileTypeUtils;
import com.google.common.annotations.VisibleForTesting;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.hutool.core.date.DatePattern.PURE_DATE_PATTERN;
import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.framework.common.exception.enums.GlobalErrorCodeConstants.FORBIDDEN;
import static cn.weitee.erp.module.infra.enums.ErrorCodeConstants.FILE_NOT_EXISTS;

/**
 * 文件 Service 实现类
 *
 * @author WeTai
 */
@Service
@Slf4j
public class FileServiceImpl implements FileService {

    /**
     * 上传文件的前缀，是否包含日期（yyyyMMdd）
     */
    static boolean PATH_PREFIX_DATE_ENABLE = true;

    /**
     * 上传文件的后缀，是否包含时间戳
     */
    static boolean PATH_SUFFIX_TIMESTAMP_ENABLE = true;

    @Resource
    private FileConfigService fileConfigService;

    @Resource
    private FileMapper fileMapper;

    @Resource
    private FileOperationLogService fileOperationLogService;

    @Resource
    private FileVersionService fileVersionService;

    @Resource
    private FileCleanupCompensationService fileCleanupCompensationService;

    @Resource
    private IncomingFileProtectionService incomingFileProtectionService;

    @Resource
    private DrmProperties drmProperties;

    @Resource
    private PlatformTransactionManager transactionManager;

    @Override
    public PageResult<FileDO> getFilePage(FilePageReqVO pageReqVO) {
        return fileMapper.selectPage(pageReqVO);
    }

    @Override
    public PageResult<FileDO> getRecycleFilePage(FilePageReqVO pageReqVO) {
        // 查询回收站文件（deleteTime不为空的文件）
        return fileMapper.selectRecyclePage(pageReqVO);
    }

    @Override
    @SneakyThrows
    public String createFile(byte[] content, String name, String directory, String type) {
        // 文件进入系统后统一转换为明文，供后续存储和业务处理。
        content = incomingFileProtectionService.preparePlainContent(content, name);
        final byte[] uploadContent = content;
        // 1.1 处理 type 为空的情况
        if (StrUtil.isEmpty(type)) {
            type = FileTypeUtils.getMineType(uploadContent, name);
        }
        // 1.2 处理 name 为空的情况
        if (StrUtil.isEmpty(name)) {
            name = DigestUtil.sha256Hex(content);
        }
        if (StrUtil.isEmpty(FileUtil.extName(name))) {
            String extension = FileTypeUtils.getExtension(type);
            if (StrUtil.isNotEmpty(extension)) {
                name = name + extension;
            }
        }

        final String uploadName = name;
        final String uploadType = type;

        // 2.1 生成上传的 path，需要保证唯一
        String path = generateUploadPath(uploadName, directory);
        // 2.2 上传到远端存储（事务外执行，避免事务回滚后远端文件成为孤文件）
        FileClient client = fileConfigService.getMasterFileClient();
        Assert.notNull(client, "客户端(master) 不能为空");
        String url = client.upload(uploadContent, path, uploadType);

        // 3. 事务内保存数据库记录
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        FileDO file;
        try {
            file = transactionTemplate.execute(status -> {
                FileDO fileRecord = new FileDO().setConfigId(client.getId())
                        .setName(uploadName).setPath(path).setUrl(url)
                        .setType(uploadType).setSize((long) uploadContent.length);
                fileMapper.insert(fileRecord);

                // 4. 记录操作日志
                fileOperationLogService.logSuccess(fileRecord.getId(), uploadName, "UPLOAD", "上传文件成功");

                // 5. 保存文件版本
                fileVersionService.saveFileVersion(fileRecord.getId(), uploadName, url,
                        fileRecord.getSize(), uploadType, "初始版本");

                return fileRecord;
            });
        } catch (Exception e) {
            try {
                client.delete(path);
            } catch (Exception cleanupException) {
                log.error("[createFile] 文件数据库保存失败且远端文件清理失败，configId={}, path={}, url={}",
                        client.getId(), path, url, cleanupException);
                try {
                    fileCleanupCompensationService.record(client.getId(), path, url,
                            cleanupException.getMessage());
                } catch (Exception compensationException) {
                    log.error("[createFile] 远端文件清理补偿记录写入失败，configId={}, path={}, url={}",
                            client.getId(), path, url, compensationException);
                }
            }
            log.error("[createFile] 文件数据库保存失败，已尝试清理远端文件，configId={}, path={}, url={}",
                    client.getId(), path, url, e);
            throw e;
        }
        Assert.notNull(file, "文件记录保存失败");

        return url;
    }

    @VisibleForTesting
    String generateUploadPath(String name, String directory) {
        String prefix = null;
        if (PATH_PREFIX_DATE_ENABLE) {
            prefix = LocalDateTimeUtil.format(LocalDateTimeUtil.now(), PURE_DATE_PATTERN);
        }
        String suffix = null;
        if (PATH_SUFFIX_TIMESTAMP_ENABLE) {
            suffix = String.valueOf(System.currentTimeMillis());
        }

        if (StrUtil.isNotEmpty(suffix)) {
            String ext = FileUtil.extName(name);
            if (StrUtil.isNotEmpty(ext)) {
                name = FileUtil.mainName(name) + StrUtil.C_UNDERLINE + suffix + StrUtil.DOT + ext;
            } else {
                name = name + StrUtil.C_UNDERLINE + suffix;
            }
        }
        if (StrUtil.isNotEmpty(prefix)) {
            name = prefix + StrUtil.SLASH + name;
        }
        if (StrUtil.isNotEmpty(directory)) {
            name = directory + StrUtil.SLASH + name;
        }
        return name;
    }

    @Override
    @SneakyThrows
    public FilePresignedUrlRespVO presignPutUrl(String name, String directory) {
        assertPresignedTransferAllowed();
        String path = generateUploadPath(name, directory);
        FileClient fileClient = fileConfigService.getMasterFileClient();
        String uploadUrl = fileClient.presignPutUrl(path);
        String visitUrl = fileClient.presignGetUrl(path, null);
        return new FilePresignedUrlRespVO().setConfigId(fileClient.getId())
                .setPath(path).setUploadUrl(uploadUrl).setUrl(visitUrl);
    }

    @Override
    public String presignGetUrl(String url, Integer expirationSeconds) {
        assertPresignedTransferAllowed();
        FileClient fileClient = fileConfigService.getMasterFileClient();
        return fileClient.presignGetUrl(url, expirationSeconds);
    }

    @Override
    public Long createFile(FileCreateReqVO createReqVO) {
        assertPresignedTransferAllowed();
        createReqVO.setUrl(HttpUtils.removeUrlQuery(createReqVO.getUrl()));
        FileDO file = BeanUtils.toBean(createReqVO, FileDO.class);
        fileMapper.insert(file);
        return file.getId();
    }

    @Override
    public FileDO getFile(Long id) {
        return validateFileExists(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void softDeleteFile(Long id, String reason) {
        FileDO file = validateFileExists(id);

        // 获取当前用户信息
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        String userName = SecurityFrameworkUtils.getLoginUserNickname();

        // 软删除：设置删除信息
        file.setDeleteUserId(userId);
        file.setDeleteUserName(userName);
        file.setDeleteTime(LocalDateTime.now());
        file.setDeleteReason(reason);
        fileMapper.updateById(file);

        // 记录操作日志
        fileOperationLogService.logSuccess(id, file.getName(), "DELETE", "移入回收站，原因：" + reason);

        log.info("[softDeleteFile] 文件移入回收站，fileId={}, fileName={}", id, file.getName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void softDeleteFileList(List<Long> ids, String reason) {
        for (Long id : ids) {
            softDeleteFile(id, reason);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restoreFile(Long id) {
        FileDO file = fileMapper.selectById(id);
        if (file == null) {
            throw exception(FILE_NOT_EXISTS);
        }

        // 检查是否在回收站
        if (file.getDeleteTime() == null) {
            log.warn("[restoreFile] 文件不在回收站，fileId={}", id);
            return;
        }

        // 恢复文件：清除删除信息
        file.setDeleteUserId(null);
        file.setDeleteUserName(null);
        file.setDeleteTime(null);
        file.setDeleteReason(null);
        fileMapper.updateById(file);

        // 记录操作日志
        fileOperationLogService.logSuccess(id, file.getName(), "RESTORE", "从回收站恢复");

        log.info("[restoreFile] 文件从回收站恢复，fileId={}, fileName={}", id, file.getName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restoreFileList(List<Long> ids) {
        for (Long id : ids) {
            restoreFile(id);
        }
    }

    @Override
    @SneakyThrows
    public void permanentDeleteFile(Long id) {
        FileDO file = fileMapper.selectById(id);
        if (file == null) {
            throw exception(FILE_NOT_EXISTS);
        }

        // 事务内删除数据库记录
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.execute(status -> {
            fileMapper.deleteById(id);
            fileOperationLogService.logSuccess(id, file.getName(), "PERMANENT_DELETE", "从回收站永久删除");
            return null;
        });

        // 事务外删除远端文件，失败时写入补偿记录供重试
        try {
            FileClient client = fileConfigService.getFileClient(file.getConfigId());
            Assert.notNull(client, "客户端({}) 不能为空", file.getConfigId());
            client.delete(file.getPath());
        } catch (Exception e) {
            log.error("[permanentDeleteFile] 远端文件删除失败，configId={}, path={}", file.getConfigId(), file.getPath(), e);
            try {
                fileCleanupCompensationService.record(file.getConfigId(), file.getPath(), file.getUrl(),
                        e.getMessage());
            } catch (Exception ce) {
                log.error("[permanentDeleteFile] 写入清理补偿记录失败，fileId={}", id, ce);
            }
        }

        log.info("[permanentDeleteFile] 文件永久删除，fileId={}, fileName={}", id, file.getName());
    }

    @Override
    @SneakyThrows
    public void permanentDeleteFileList(List<Long> ids) {
        for (Long id : ids) {
            permanentDeleteFile(id);
        }
    }

@Override
    @SneakyThrows
    public void emptyRecycleBin() {
        // 1. 获取所有回收站文件（事务外，避免长事务）
        List<FileDO> recycleFiles = fileMapper.selectRecycleFiles();
        if (recycleFiles.isEmpty()) {
            return;
        }

        // 2. 先删除远端文件（事务外），即使部分失败也不影响 DB 清理
        Map<Long, FileClient> clientMap = new HashMap<>();
        for (FileDO file : recycleFiles) {
            try {
                FileClient client = clientMap.computeIfAbsent(file.getConfigId(), fileConfigService::getFileClient);
                if (client != null) {
                    client.delete(file.getPath());
                }
            } catch (Exception e) {
                log.error("[emptyRecycleBin] 删除远端文件失败，fileId={}, path={}", file.getId(), file.getPath(), e);
                // 记录补偿记录，供定时任务重试
                try {
                    fileCleanupCompensationService.record(file.getConfigId(), file.getPath(), file.getUrl(),
                            e.getMessage());
                } catch (Exception ce) {
                    log.error("[emptyRecycleBin] 写入清理补偿记录失败，fileId={}", file.getId(), ce);
                }
            }
        }

        // 3. 事务内删除数据库记录
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.execute(status -> {
            List<Long> ids = recycleFiles.stream().map(FileDO::getId).toList();
            fileMapper.deleteByIds(ids);
            log.info("[emptyRecycleBin] 清空回收站，共删除 {} 个文件", recycleFiles.size());
            return null;
        });
    }

    @Override
    public List<FileDO> getFileListByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return fileMapper.selectBatchIds(ids);
    }

    @Override
    public byte[] getFileContent(Long configId, String path) throws Exception {
        FileClient client = fileConfigService.getFileClient(configId);
        Assert.notNull(client, "客户端({}) 不能为空", configId);
        return client.getContent(path);
    }

    /**
     * 兼容旧接口：软删除
     */
    @Override
    public void deleteFile(Long id) throws Exception {
        softDeleteFile(id, "用户删除");
    }

    /**
     * 兼容旧接口：批量软删除
     */
    @Override
    @SneakyThrows
    public void deleteFileList(List<Long> ids) {
        softDeleteFileList(ids, "用户批量删除");
    }

    private FileDO validateFileExists(Long id) {
        FileDO fileDO = fileMapper.selectById(id);
        if (fileDO == null) {
            throw exception(FILE_NOT_EXISTS);
        }
        return fileDO;
    }

    private void assertPresignedTransferAllowed() {
        if (drmProperties.isEnabled()) {
            throw exception(FORBIDDEN);
        }
    }

}
