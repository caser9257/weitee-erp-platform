package cn.weitee.erp.module.infra.service.file;

import cn.weitee.erp.module.infra.controller.admin.file.vo.tag.FileTagSaveReqVO;
import cn.weitee.erp.module.infra.dal.dataobject.file.FileTagDO;

import java.util.List;

/**
 * 文件标签 Service 接口
 *
 * @author ruoyi-vue-pro
 */
public interface FileTagService {

    /**
     * 创建文件标签
     */
    Long createFileTag(FileTagSaveReqVO reqVO);

    /**
     * 更新文件标签
     */
    void updateFileTag(FileTagSaveReqVO reqVO);

    /**
     * 删除文件标签
     */
    void deleteFileTag(Long id);

    /**
     * 获取文件标签列表
     */
    List<FileTagDO> getFileTagList();

    /**
     * 给文件添加标签
     */
    void addFileTag(Long fileId, Long tagId);

    /**
     * 移除文件标签
     */
    void removeFileTag(Long fileId, Long tagId);

    /**
     * 获取文件的标签列表
     */
    List<FileTagDO> getFileTags(Long fileId);

}
