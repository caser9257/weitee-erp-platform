package cn.iocoder.yudao.module.project.service.approve.impl;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.project.controller.admin.vo.approve.ProjectApproveActionReqVO;
import cn.iocoder.yudao.module.project.controller.admin.vo.approve.ProjectApprovePageReqVO;
import cn.iocoder.yudao.module.project.controller.admin.vo.approve.ProjectApproveSaveReqVO;
import cn.iocoder.yudao.module.project.dal.dataobject.approve.ProjectApproveMsgDO;
import cn.iocoder.yudao.module.project.dal.dataobject.approve.ProjectApproveProcDO;
import cn.iocoder.yudao.module.project.dal.mysql.approve.ProjectApproveMsgMapper;
import cn.iocoder.yudao.module.project.dal.mysql.approve.ProjectApproveProcMapper;
import cn.iocoder.yudao.module.project.service.approve.ProjectApproveService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.project.enums.ErrorCodeConstants.APPROVE_NOT_FOUND;

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
