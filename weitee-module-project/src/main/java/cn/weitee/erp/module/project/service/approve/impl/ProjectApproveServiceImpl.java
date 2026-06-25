package cn.weitee.erp.module.project.service.approve.impl;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.security.core.util.SecurityFrameworkUtils;
import cn.weitee.erp.module.project.controller.admin.vo.approve.ProjectApproveActionReqVO;
import cn.weitee.erp.module.project.controller.admin.vo.approve.ProjectApprovePageReqVO;
import cn.weitee.erp.module.project.controller.admin.vo.approve.ProjectApproveSaveReqVO;
import cn.weitee.erp.module.project.dal.dataobject.approve.ProjectApproveMsgDO;
import cn.weitee.erp.module.project.dal.dataobject.approve.ProjectApproveProcDO;
import cn.weitee.erp.module.project.dal.mysql.approve.ProjectApproveMsgMapper;
import cn.weitee.erp.module.project.dal.mysql.approve.ProjectApproveProcMapper;
import cn.weitee.erp.module.project.service.approve.ProjectApproveService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.project.enums.ErrorCodeConstants.APPROVE_NOT_FOUND;

@Service
@Validated
public class ProjectApproveServiceImpl implements ProjectApproveService {

    @Resource
    private ProjectApproveProcMapper projectApproveProcMapper;
    @Resource
    private ProjectApproveMsgMapper projectApproveMsgMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createApprove(@Valid ProjectApproveSaveReqVO createReqVO) {
        ProjectApproveProcDO approve = ProjectApproveProcDO.builder()
                .name(createReqVO.getName())
                .status(0)
                .build();
        projectApproveProcMapper.insert(approve);
        return approve.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveAction(@Valid ProjectApproveActionReqVO actionReqVO) {
        validateApprove(actionReqVO.getId());
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        ProjectApproveMsgDO msg = ProjectApproveMsgDO.builder()
                .approveId(actionReqVO.getId())
                .userId(userId)
                .action(actionReqVO.getAction())
                .content(actionReqVO.getContent())
                .build();
        projectApproveMsgMapper.insert(msg);
    }

    @Override
    public ProjectApproveProcDO getApprove(Long id) {
        return projectApproveProcMapper.selectById(id);
    }

    @Override
    public PageResult<ProjectApproveProcDO> getApprovePage(ProjectApprovePageReqVO pageReqVO) {
        return projectApproveProcMapper.selectPage(pageReqVO, new LambdaQueryWrapper<ProjectApproveProcDO>()
                .like(ProjectApproveProcDO::getName, pageReqVO.getName())
                .eq(ProjectApproveProcDO::getStatus, pageReqVO.getStatus())
                .orderByDesc(ProjectApproveProcDO::getId));
    }

    private ProjectApproveProcDO validateApprove(Long id) {
        ProjectApproveProcDO approve = projectApproveProcMapper.selectById(id);
        if (approve == null) {
            throw exception(APPROVE_NOT_FOUND);
        }
        return approve;
    }
}
