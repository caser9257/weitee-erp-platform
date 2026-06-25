package cn.weitee.erp.module.project.convert.column;

import cn.weitee.erp.module.project.controller.admin.vo.column.ProjectColumnRespVO;
import cn.weitee.erp.module.project.controller.admin.vo.column.ProjectColumnSaveReqVO;
import cn.weitee.erp.module.project.dal.dataobject.column.ProjectColumnDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ProjectColumnConvert {
    ProjectColumnConvert INSTANCE = Mappers.getMapper(ProjectColumnConvert.class);
    ProjectColumnDO convert(ProjectColumnSaveReqVO bean);
    ProjectColumnRespVO convert(ProjectColumnDO bean);
}
