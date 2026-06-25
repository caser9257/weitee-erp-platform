package cn.weitee.erp.module.project.service.approve;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.project.controller.admin.vo.approve.ProjectApproveActionReqVO;
import cn.weitee.erp.module.project.controller.admin.vo.approve.ProjectApprovePageReqVO;
import cn.weitee.erp.module.project.controller.admin.vo.approve.ProjectApproveSaveReqVO;
import cn.weitee.erp.module.project.dal.dataobject.approve.ProjectApproveProcDO;

import jakarta.validation.Valid;

public interface ProjectApproveService {
    Long createApprove(@Valid ProjectApproveSaveReqVO createReqVO);
    void approveAction(@Valid ProjectApproveActionReqVO actionReqVO);
    ProjectApproveProcDO getApprove(Long id);
    PageResult<ProjectApproveProcDO> getApprovePage(ProjectApprovePageReqVO pageReqVO);
}
