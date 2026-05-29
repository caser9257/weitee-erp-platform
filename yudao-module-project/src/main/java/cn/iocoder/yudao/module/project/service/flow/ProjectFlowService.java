package cn.iocoder.yudao.module.project.service.flow;

import cn.iocoder.yudao.module.project.controller.admin.vo.flow.ProjectFlowSaveReqVO;
import cn.iocoder.yudao.module.project.dal.dataobject.flow.ProjectFlowDO;
import cn.iocoder.yudao.module.project.dal.dataobject.flow.ProjectFlowItemDO;

import java.util.List;

public interface ProjectFlowService {

    Long saveFlow(ProjectFlowSaveReqVO saveReqVO);

    ProjectFlowDO getFlow(Long projectId);

    List<ProjectFlowItemDO> getFlowItems(Long flowId);
}
