package cn.iocoder.yudao.module.project.convert.column;

import cn.iocoder.yudao.module.project.controller.admin.vo.column.ProjectColumnRespVO;
import cn.iocoder.yudao.module.project.controller.admin.vo.column.ProjectColumnSaveReqVO;
import cn.iocoder.yudao.module.project.dal.dataobject.column.ProjectColumnDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ProjectColumnConvert {
    ProjectColumnConvert INSTANCE = Mappers.getMapper(ProjectColumnConvert.class);
    ProjectColumnDO convert(ProjectColumnSaveReqVO bean);
    ProjectColumnRespVO convert(ProjectColumnDO bean);
}
