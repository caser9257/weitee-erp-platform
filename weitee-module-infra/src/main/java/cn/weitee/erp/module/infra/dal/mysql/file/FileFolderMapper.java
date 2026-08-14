package cn.weitee.erp.module.infra.dal.mysql.file;

import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.module.infra.dal.dataobject.file.FileFolderDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 文件澶?Mapper
 *
 * @author weitee
 */
@Mapper
public interface FileFolderMapper extends BaseMapperX<FileFolderDO> {

    default List<FileFolderDO> selectListByParentId(Long parentId) {
        return selectList(FileFolderDO::getParentId, parentId);
    }

}
