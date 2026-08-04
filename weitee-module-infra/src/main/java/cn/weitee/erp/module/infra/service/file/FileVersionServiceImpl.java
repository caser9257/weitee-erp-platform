package cn.weitee.erp.module.infra.service.file;

import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.infra.dal.dataobject.file.FileDO;
import cn.weitee.erp.module.infra.dal.dataobject.file.FileVersionDO;
import cn.weitee.erp.module.infra.dal.mysql.file.FileMapper;
import cn.weitee.erp.module.infra.dal.mysql.file.FileVersionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.List;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.infra.enums.ErrorCodeConstants.FILE_NOT_EXISTS;
import static cn.weitee.erp.module.infra.enums.ErrorCodeConstants.FILE_VERSION_NOT_EXISTS;

/**
 * 文件版本 Service 实现
 *
 * @author ruoyi-vue-pro
 */
@Service
@Validated
@Slf4j
public class FileVersionServiceImpl implements FileVersionService {

    @Resource
    private FileVersionMapper fileVersionMapper;

    @Resource
    private FileMapper fileMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveFileVersion(Long fileId, String name, String url, Long size, String type, String description) {
        // 锁定主文件记录，防止并发版本号竞争（主文件记录在首次版本写入前已存在，保证锁生效）
        FileDO file = fileMapper.selectOneForUpdate(new LambdaQueryWrapperX<FileDO>().eq(FileDO::getId, fileId));
        if (file == null) {
            throw exception(FILE_NOT_EXISTS);
        }

        // 获取当前最大版本号
        Integer maxVersion = fileVersionMapper.getMaxVersion(fileId);
        int newVersion = (maxVersion == null ? 0 : maxVersion) + 1;

        FileVersionDO version = FileVersionDO.builder()
                .fileId(fileId)
                .version(newVersion)
                .name(name)
                .url(url)
                .size(size)
                .type(type)
                .description(description)
                .build();
        fileVersionMapper.insert(version);

        log.info("[saveFileVersion] 保存文件版本，fileId={}, version={}", fileId, newVersion);
        return version.getId();
    }

    @Override
    public List<FileVersionDO> getFileVersions(Long fileId) {
        return fileVersionMapper.selectListByFileId(fileId);
    }

    @Override
    public Integer getLatestVersion(Long fileId) {
        return fileVersionMapper.getMaxVersion(fileId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rollbackToVersion(Long fileId, Integer version) {
        // 锁定主文件记录，防止并发版本号竞争
        FileDO lockedFile = fileMapper.selectOneForUpdate(
                new LambdaQueryWrapperX<FileDO>().eq(FileDO::getId, fileId));
        if (lockedFile == null) {
            throw exception(FILE_NOT_EXISTS);
        }

        // 1. 校验文件版本是否存在
        FileVersionDO fileVersion = fileVersionMapper.selectOne(
                new LambdaQueryWrapperX<FileVersionDO>()
                        .eq(FileVersionDO::getFileId, fileId)
                        .eq(FileVersionDO::getVersion, version)
        );
        if (fileVersion == null) {
            throw exception(FILE_VERSION_NOT_EXISTS);
        }

        // 2. 查询主文件记录
        FileDO file = fileMapper.selectById(fileId);
        if (file == null) {
            throw exception(FILE_NOT_EXISTS);
        }

        // 3. 更新主文件表为指定版本的信息
        file.setName(fileVersion.getName());
        file.setUrl(fileVersion.getUrl());
        file.setSize(fileVersion.getSize());
        file.setType(fileVersion.getType());
        fileMapper.updateById(file);

        // 4. 创建新的版本记录（标记为回滚操作）
        Integer maxVersion = fileVersionMapper.getMaxVersion(fileId);
        int newVersion = (maxVersion == null ? 0 : maxVersion) + 1;
        FileVersionDO rollbackVersion = FileVersionDO.builder()
                .fileId(fileId)
                .version(newVersion)
                .name(fileVersion.getName())
                .url(fileVersion.getUrl())
                .size(fileVersion.getSize())
                .type(fileVersion.getType())
                .description("回滚到版本 " + version)
                .build();
        fileVersionMapper.insert(rollbackVersion);

        log.info("[rollbackToVersion] 回溯文件版本成功，fileId={}, fromVersion={}, toVersion={}, url={}",
                fileId, newVersion - 1, version, fileVersion.getUrl());
    }

}
