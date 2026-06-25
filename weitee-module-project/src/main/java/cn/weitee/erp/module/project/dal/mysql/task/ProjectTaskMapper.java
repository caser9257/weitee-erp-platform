package cn.weitee.erp.module.project.dal.mysql.task;

import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.module.project.dal.dataobject.task.ProjectTaskDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProjectTaskMapper extends BaseMapperX<ProjectTaskDO> {
}
