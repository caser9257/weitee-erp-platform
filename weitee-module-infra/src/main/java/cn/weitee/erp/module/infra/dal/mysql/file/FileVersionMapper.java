package cn.weitee.erp.module.infra.dal.mysql.file;

import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.infra.dal.dataobject.file.FileVersionDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 文件版本 Mapper
 *
 * @author ruoyi-vue-pro
 */
@Mapper
public interface FileVersionMapper extends BaseMapperX<FileVersionDO> {

    default List<FileVersionDO> selectListByFileId(Long fileId) {
        return selectList(FileVersionDO::getFileId, fileId);
    }

    default Integer getMaxVersion(Long fileId) {
        FileVersionDO version = selectOne(new LambdaQueryWrapperX<FileVersionDO>()
                .eq(FileVersionDO::getFileId, fileId)
                .orderByDesc(FileVersionDO::getVersion)
                .last("LIMIT 1"));
        return version != null ? version.getVersion() : null;
    }

    /**
     * 获取最大版本号（使用 FOR UPDATE 锁定，防止并发竞争）
     */
    @Select("SELECT MAX(version) FROM infra_file_version WHERE file_id = #{fileId} FOR UPDATE")
    Integer getMaxVersionForUpdate(Long fileId);

}
