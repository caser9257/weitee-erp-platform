package cn.weitee.erp.module.infra.service.file;

import cn.weitee.erp.module.infra.controller.admin.file.vo.permission.FilePermissionSaveReqVO;
import cn.weitee.erp.module.infra.dal.dataobject.file.FilePermissionDO;

import java.util.List;

/**
 * 文件权限 Service 接口
 *
 * @author ruoyi-vue-pro
 */
public interface FilePermissionService {

    /**
     * 授权文件权限
     *
     * @param reqVO 授权信息
     * @return 权限ID
     */
    Long grantPermission(FilePermissionSaveReqVO reqVO);

    /**
     * 撤销文件权限
     *
     * @param fileId         文件ID
     * @param grantType      授权类型
     * @param grantTargetId  授权目标ID
     */
    void revokePermission(Long fileId, String grantType, Long grantTargetId);

    /**
     * 获取文件的权限列表
     */
    List<FilePermissionDO> getFilePermissions(Long fileId);

    /**
     * 检查用户是否有指定权限
     *
     * @param fileId     文件ID
     * @param userId     用户ID
     * @param permission 权限类型
     * @return 是否有权限
     */
    boolean hasPermission(Long fileId, Long userId, String permission);

    /**
     * 获取用户对指定文件的所有权限
     */
    List<String> getUserPermissions(Long fileId, Long userId, List<Long> roleIds, Long deptId);

    /**
     * 删除文件的所有权限
     */
    void deleteFilePermissions(Long fileId);

}
