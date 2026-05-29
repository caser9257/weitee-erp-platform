package cn.iocoder.yudao.module.project.dal.mysql.file;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.project.dal.dataobject.file.ProjectFileDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProjectFileMapper extends BaseMapperX<ProjectFileDO> {
}
