package cn.weitee.erp.module.infra.service.file;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.weitee.erp.framework.security.core.util.SecurityFrameworkUtils;
import cn.weitee.erp.module.infra.controller.admin.file.vo.permission.FilePermissionSaveReqVO;
import cn.weitee.erp.module.infra.dal.dataobject.file.FilePermissionDO;
import cn.weitee.erp.module.infra.dal.mysql.file.FilePermissionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件权限 Service 实现
 *
 * @author weitee
 */
@Service
@Validated
@Slf4j
public class FilePermissionServiceImpl implements FilePermissionService {

    @Resource
    private FilePermissionMapper filePermissionMapper;

    @Override
    public Long grantPermission(FilePermissionSaveReqVO reqVO) {
        // 获取当前用户信息
        Long grantUserId = SecurityFrameworkUtils.getLoginUserId();
        String grantUserName = SecurityFrameworkUtils.getLoginUsername();

        // 构建权限对象
        FilePermissionDO permission = FilePermissionDO.builder()
                .fileId(reqVO.getFileId())
                .grantType(reqVO.getGrantType())
                .grantTargetId(reqVO.getGrantTargetId())
                .grantTargetName(reqVO.getGrantTargetName())
                .permissions(reqVO.getPermissions())
                .expireTime(reqVO.getExpireTime())
                .grantUserId(grantUserId)
                .grantUserName(grantUserName)
                .remark(reqVO.getRemark())
                .build();

        filePermissionMapper.insert(permission);
        return permission.getId();
    }

    @Override
    public void revokePermission(Long fileId, String grantType, Long grantTargetId) {
        filePermissionMapper.deleteByFileIdAndGrantTarget(fileId, grantType, grantTargetId);
        log.info("[revokePermission] 撤销文件权限，fileId={}, grantType={}, grantTargetId={}", fileId, grantType, grantTargetId);
    }

    @Override
    public List<FilePermissionDO> getFilePermissions(Long fileId) {
        return filePermissionMapper.selectListByFileId(fileId);
    }

    @Override
    public boolean hasPermission(Long fileId, Long userId, String permission) {
        // 获取用户的所有权�?
        List<Long> roleIds = SecurityFrameworkUtils.getLoginUser().getRoleIds();
        Long deptId = SecurityFrameworkUtils.getLoginUser().getDeptId();

        List<FilePermissionDO> permissions = filePermissionMapper.selectListByFileIdAndUserId(fileId, userId, roleIds, deptId);

        // 检查是否有指定权限
        for (FilePermissionDO perm : permissions) {
            if (StrUtil.isNotEmpty(perm.getPermissions())) {
                List<String> permList = Arrays.asList(perm.getPermissions().split(","));
                if (permList.contains(permission)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public List<String> getUserPermissions(Long fileId, Long userId, List<Long> roleIds, Long deptId) {
        List<FilePermissionDO> permissions = filePermissionMapper.selectListByFileIdAndUserId(fileId, userId, roleIds, deptId);

        // 合并所有权�?
        return permissions.stream()
                .filter(perm -> StrUtil.isNotEmpty(perm.getPermissions()))
                .flatMap(perm -> Arrays.stream(perm.getPermissions().split(",")))
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public void deleteFilePermissions(Long fileId) {
        filePermissionMapper.deleteByFileId(fileId);
        log.info("[deleteFilePermissions] 删除文件的所有权限，fileId={}", fileId);
    }

}
