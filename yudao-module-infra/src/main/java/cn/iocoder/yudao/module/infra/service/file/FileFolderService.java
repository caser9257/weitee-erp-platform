package cn.iocoder.yudao.module.infra.service.file;

import cn.iocoder.yudao.module.infra.controller.admin.file.vo.folder.FileFolderSaveReqVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.file.FileFolderDO;

import java.util.List;

/**
 * 文件夹 Service 接口
 *
 * @author ruoyi-vue-pro
 */
public interface FileFolderService {

    /**
     * 创建文件夹
     */
    Long createFileFolder(FileFolderSaveReqVO reqVO);

    /**
     * 更新文件夹
     */
    void updateFileFolder(FileFolderSaveReqVO reqVO);

    /**
     * 删除文件夹
     */
    void deleteFileFolder(Long id);

    /**
     * 获取文件夹
     */
    FileFolderDO getFileFolder(Long id);

    /**
     * 获取文件夹列表
     */
    List<FileFolderDO> getFileFolderList();

    /**
     * 获取文件夹树
     */
    List<FileFolderDO> getFileFolderTree();

}
