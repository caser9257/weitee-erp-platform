package cn.weitee.erp.module.project.service.flow;

import cn.weitee.erp.module.project.controller.admin.vo.flow.ProjectFlowSaveReqVO;
import cn.weitee.erp.module.project.dal.dataobject.flow.ProjectFlowDO;
import cn.weitee.erp.module.project.dal.dataobject.flow.ProjectFlowItemDO;

import java.util.List;

public interface ProjectFlowService {

    Long saveFlow(ProjectFlowSaveReqVO saveReqVO);

    ProjectFlowDO getFlow(Long projectId);

    List<ProjectFlowItemDO> getFlowItems(Long flowId);
}
