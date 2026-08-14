package cn.weitee.erp.module.infra.service.file;

import cn.weitee.erp.module.infra.controller.admin.file.vo.folder.FileFolderSaveReqVO;
import cn.weitee.erp.module.infra.dal.dataobject.file.FileFolderDO;

import java.util.List;

/**
 * 文件�?Service 接口
 *
 * @author weitee
 */
public interface FileFolderService {

    /**
     * 创建文件�?
     */
    Long createFileFolder(FileFolderSaveReqVO reqVO);

    /**
     * 更新文件�?
     */
    void updateFileFolder(FileFolderSaveReqVO reqVO);

    /**
     * 删除文件�?
     */
    void deleteFileFolder(Long id);

    /**
     * 获取文件�?
     */
    FileFolderDO getFileFolder(Long id);

    /**
     * 获取文件夹列�?
     */
    List<FileFolderDO> getFileFolderList();

    /**
     * 获取文件夹树
     */
    List<FileFolderDO> getFileFolderTree();

}
