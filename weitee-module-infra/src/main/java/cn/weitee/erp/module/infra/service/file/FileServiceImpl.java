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
import cn.weitee.erp.module.infra.framework.file.core.client.FileClient;
import cn.weitee.erp.module.infra.framework.file.core.utils.FileTypeUtils;
import com.google.common.annotations.VisibleForTesting;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static cn.hutool.core.date.DatePattern.PURE_DATE_PATTERN;
import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
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
        // 1.1 处理 type 为空的情况
        if (StrUtil.isEmpty(type)) {
            type = FileTypeUtils.getMineType(content, name);
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

        // 2.1 生成上传的 path，需要保证唯一
        String path = generateUploadPath(name, directory);
        // 2.2 上传到文件存储器
        FileClient client = fileConfigService.getMasterFileClient();
        Assert.notNull(client, "客户端(master) 不能为空");
        String url = client.upload(content, path, type);

        // 3. 保存到数据库
        FileDO file = new FileDO().setConfigId(client.getId())
                .setName(name).setPath(path).setUrl(url)
                .setType(type).setSize((long) content.length);
        fileMapper.insert(file);

        // 4. 记录操作日志
        fileOperationLogService.logSuccess(file.getId(), name, "UPLOAD", "上传文件成功");

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
        String path = generateUploadPath(name, directory);
        FileClient fileClient = fileConfigService.getMasterFileClient();
        String uploadUrl = fileClient.presignPutUrl(path);
        String visitUrl = fileClient.presignGetUrl(path, null);
        return new FilePresignedUrlRespVO().setConfigId(fileClient.getId())
                .setPath(path).setUploadUrl(uploadUrl).setUrl(visitUrl);
    }

    @Override
    public String presignGetUrl(String url, Integer expirationSeconds) {
        FileClient fileClient = fileConfigService.getMasterFileClient();
        return fileClient.presignGetUrl(url, expirationSeconds);
    }

    @Override
    public Long createFile(FileCreateReqVO createReqVO) {
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
    @Transactional(rollbackFor = Exception.class)
    public void permanentDeleteFile(Long id) {
        FileDO file = fileMapper.selectById(id);
        if (file == null) {
            throw exception(FILE_NOT_EXISTS);
        }

        // 从文件存储器中删除
        FileClient client = fileConfigService.getFileClient(file.getConfigId());
        Assert.notNull(client, "客户端({}) 不能为空", file.getConfigId());
        client.delete(file.getPath());

        // 删除记录
        fileMapper.deleteById(id);

        // 记录操作日志
        fileOperationLogService.logSuccess(id, file.getName(), "PERMANENT_DELETE", "从回收站永久删除");

        log.info("[permanentDeleteFile] 文件永久删除，fileId={}, fileName={}", id, file.getName());
    }

    @Override
    @SneakyThrows
    @Transactional(rollbackFor = Exception.class)
    public void permanentDeleteFileList(List<Long> ids) {
        for (Long id : ids) {
            permanentDeleteFile(id);
        }
    }

    @Override
    @SneakyThrows
    @Transactional(rollbackFor = Exception.class)
    public void emptyRecycleBin() {
        // 获取所有回收站文件
        List<FileDO> recycleFiles = fileMapper.selectRecycleFiles();
        if (recycleFiles.isEmpty()) {
            return;
        }

        // 批量删除
        for (FileDO file : recycleFiles) {
            try {
                FileClient client = fileConfigService.getFileClient(file.getConfigId());
                if (client != null) {
                    client.delete(file.getPath());
                }
            } catch (Exception e) {
                log.error("[emptyRecycleBin] 删除文件存储失败，fileId={}, path={}", file.getId(), file.getPath(), e);
            }
        }

        // 删除记录
        List<Long> ids = recycleFiles.stream().map(FileDO::getId).toList();
        fileMapper.deleteByIds(ids);

        log.info("[emptyRecycleBin] 清空回收站，共删除 {} 个文件", recycleFiles.size());
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

}
