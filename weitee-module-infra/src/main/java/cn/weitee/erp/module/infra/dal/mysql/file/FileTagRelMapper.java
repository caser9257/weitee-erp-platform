package cn.weitee.erp.module.infra.dal.mysql.file;

import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.module.infra.dal.dataobject.file.FileTagRelDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 文件-标签关联 Mapper
 *
 * @author ruoyi-vue-pro
 */
@Mapper
public interface FileTagRelMapper extends BaseMapperX<FileTagRelDO> {

    default List<FileTagRelDO> selectListByFileId(Long fileId) {
        return selectList(FileTagRelDO::getFileId, fileId);
    }

    default List<FileTagRelDO> selectListByTagId(Long tagId) {
        return selectList(FileTagRelDO::getTagId, tagId);
    }

    default int deleteByFileIdAndTagId(Long fileId, Long tagId) {
        return delete(new cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX<FileTagRelDO>()
                .eq(FileTagRelDO::getFileId, fileId)
                .eq(FileTagRelDO::getTagId, tagId));
    }

    default int deleteByFileId(Long fileId) {
        return delete(FileTagRelDO::getFileId, fileId);
    }

    default int deleteByTagId(Long tagId) {
        return delete(FileTagRelDO::getTagId, tagId);
    }

}
