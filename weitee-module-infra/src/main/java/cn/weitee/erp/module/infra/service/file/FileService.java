package cn.weitee.erp.module.infra.service.file;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.infra.controller.admin.file.vo.file.FileCreateReqVO;
import cn.weitee.erp.module.infra.controller.admin.file.vo.file.FilePageReqVO;
import cn.weitee.erp.module.infra.controller.admin.file.vo.file.FilePresignedUrlRespVO;
import cn.weitee.erp.module.infra.dal.dataobject.file.FileDO;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * 文件 Service 接口
 *
 * @author WeTai
 */
public interface FileService {

    /**
     * 获得文件分页
     */
    PageResult<FileDO> getFilePage(FilePageReqVO pageReqVO);

    /**
     * 获得回收站文件分页
     */
    PageResult<FileDO> getRecycleFilePage(FilePageReqVO pageReqVO);

    /**
     * 保存文件，并返回文件的访问路径
     */
    String createFile(@NotEmpty(message = "文件内容不能为空") byte[] content,
                      String name, String directory, String type);

    /**
     * 生成文件预签名地址信息，用于上传
     */
    FilePresignedUrlRespVO presignPutUrl(@NotEmpty(message = "文件名不能为空") String name,
                                         String directory);

    /**
     * 生成文件预签名地址信息，用于读取
     */
    String presignGetUrl(String url, Integer expirationSeconds);

    /**
     * 创建文件
     */
    Long createFile(FileCreateReqVO createReqVO);

    /**
     * 获取文件
     */
    FileDO getFile(Long id);

    /**
     * 软删除文件（移入回收站）
     */
    void softDeleteFile(Long id, String reason);

    /**
     * 批量软删除文件
     */
    void softDeleteFileList(List<Long> ids, String reason);

    /**
     * 恢复文件（从回收站恢复）
     */
    void restoreFile(Long id);

    /**
     * 批量恢复文件
     */
    void restoreFileList(List<Long> ids);

    /**
     * 彻底删除文件（从回收站永久删除）
     */
    void permanentDeleteFile(Long id) throws Exception;

    /**
     * 批量彻底删除文件
     */
    void permanentDeleteFileList(List<Long> ids) throws Exception;

    /**
     * 清空回收站
     */
    void emptyRecycleBin() throws Exception;

    /**
     * 批量获取文件列表
     */
    List<FileDO> getFileListByIds(List<Long> ids);

    /**
     * 获得文件内容
     */
    byte[] getFileContent(Long configId, String path) throws Exception;

    /**
     * 删除文件（兼容旧接口，内部调用softDeleteFile）
     */
    void deleteFile(Long id) throws Exception;

    /**
     * 批量删除文件（兼容旧接口，内部调用softDeleteFileList）
     */
    void deleteFileList(List<Long> ids) throws Exception;

}
