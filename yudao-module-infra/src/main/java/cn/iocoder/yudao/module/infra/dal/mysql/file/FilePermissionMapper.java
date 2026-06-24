package cn.iocoder.yudao.module.infra.dal.mysql.file;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.infra.dal.dataobject.file.FilePermissionDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文件权限 Mapper
 *
 * @author ruoyi-vue-pro
 */
@Mapper
public interface FilePermissionMapper extends BaseMapperX<FilePermissionDO> {

    /**
     * 获取文件的权限列表
     */
    default List<FilePermissionDO> selectListByFileId(Long fileId) {
        return selectList(new LambdaQueryWrapperX<FilePermissionDO>()
                .eq(FilePermissionDO::getFileId, fileId)
                .orderByDesc(FilePermissionDO::getCreateTime));
    }

    /**
     * 获取用户对指定文件的权限
     */
    default FilePermissionDO selectByFileIdAndUserId(Long fileId, Long userId) {
        return selectOne(new LambdaQueryWrapperX<FilePermissionDO>()
                .eq(FilePermissionDO::getFileId, fileId)
                .eq(FilePermissionDO::getGrantType, "USER")
                .eq(FilePermissionDO::getGrantTargetId, userId)
                .le(FilePermissionDO::getExpireTime, LocalDateTime.now()));
    }

    /**
     * 获取用户对指定文件的所有权限（包括角色和部门权限）
     */
    default List<FilePermissionDO> selectListByFileIdAndUserId(Long fileId, Long userId, List<Long> roleIds, Long deptId) {
        return selectList(new LambdaQueryWrapperX<FilePermissionDO>()
                .eq(FilePermissionDO::getFileId, fileId)
                .and(wrapper -> wrapper
                        .and(w -> w
                                .eq(FilePermissionDO::getGrantType, "USER")
                                .eq(FilePermissionDO::getGrantTargetId, userId))
                        .or(w -> w
                                .eq(FilePermissionDO::getGrantType, "ROLE")
                                .in(FilePermissionDO::getGrantTargetId, roleIds))
                        .or(w -> w
                                .eq(FilePermissionDO::getGrantType, "DEPT")
                                .eq(FilePermissionDO::getGrantTargetId, deptId)))
                .le(FilePermissionDO::getExpireTime, LocalDateTime.now()));
    }

    /**
     * 删除文件的所有权限
     */
    default int deleteByFileId(Long fileId) {
        return delete(new LambdaQueryWrapperX<FilePermissionDO>()
                .eq(FilePermissionDO::getFileId, fileId));
    }

    /**
     * 删除指定权限
     */
    default int deleteByFileIdAndGrantTarget(Long fileId, String grantType, Long grantTargetId) {
        return delete(new LambdaQueryWrapperX<FilePermissionDO>()
                .eq(FilePermissionDO::getFileId, fileId)
                .eq(FilePermissionDO::getGrantType, grantType)
                .eq(FilePermissionDO::getGrantTargetId, grantTargetId));
    }

}
