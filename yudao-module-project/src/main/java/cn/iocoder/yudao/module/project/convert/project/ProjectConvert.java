package cn.iocoder.yudao.module.project.convert.project;

import cn.iocoder.yudao.module.project.controller.admin.vo.project.ProjectRespVO;
import cn.iocoder.yudao.module.project.controller.admin.vo.project.ProjectSaveReqVO;
import cn.iocoder.yudao.module.project.dal.dataobject.project.ProjectDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ProjectConvert {
    ProjectConvert INSTANCE = Mappers.getMapper(ProjectConvert.class);
    ProjectDO convert(ProjectSaveReqVO bean);
    ProjectRespVO convert(ProjectDO bean);
}
