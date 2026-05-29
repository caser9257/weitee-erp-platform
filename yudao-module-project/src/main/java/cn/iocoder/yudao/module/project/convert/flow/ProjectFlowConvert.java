package cn.iocoder.yudao.module.project.convert.flow;

import cn.iocoder.yudao.module.project.controller.admin.vo.flow.ProjectFlowRespVO;
import cn.iocoder.yudao.module.project.dal.dataobject.flow.ProjectFlowDO;
import cn.iocoder.yudao.module.project.dal.dataobject.flow.ProjectFlowItemDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ProjectFlowConvert {
    ProjectFlowConvert INSTANCE = Mappers.getMapper(ProjectFlowConvert.class);

    ProjectFlowRespVO convert(ProjectFlowDO bean);

    ProjectFlowRespVO.FlowItemRespVO convertItem(ProjectFlowItemDO bean);

    List<ProjectFlowRespVO.FlowItemRespVO> convertItemList(List<ProjectFlowItemDO> list);
}
