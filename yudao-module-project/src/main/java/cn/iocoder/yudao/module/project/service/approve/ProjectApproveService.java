package cn.iocoder.yudao.module.project.service.approve;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.project.controller.admin.vo.approve.ProjectApproveActionReqVO;
import cn.iocoder.yudao.module.project.controller.admin.vo.approve.ProjectApprovePageReqVO;
import cn.iocoder.yudao.module.project.controller.admin.vo.approve.ProjectApproveSaveReqVO;
import cn.iocoder.yudao.module.project.dal.dataobject.approve.ProjectApproveProcDO;

import jakarta.validation.Valid;

public interface ProjectApproveService {
    Long createApprove(@Valid ProjectApproveSaveReqVO createReqVO);
    void approveAction(@Valid ProjectApproveActionReqVO actionReqVO);
    ProjectApproveProcDO getApprove(Long id);
    PageResult<ProjectApproveProcDO> getApprovePage(ProjectApprovePageReqVO pageReqVO);
}
