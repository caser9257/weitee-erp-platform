package cn.iocoder.yudao.module.infra.dal.mysql.file;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.infra.dal.dataobject.file.FileFolderDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 文件夹 Mapper
 *
 * @author ruoyi-vue-pro
 */
@Mapper
public interface FileFolderMapper extends BaseMapperX<FileFolderDO> {

    default List<FileFolderDO> selectListByParentId(Long parentId) {
        return selectList(FileFolderDO::getParentId, parentId);
    }

}
