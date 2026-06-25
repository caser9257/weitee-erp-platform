package cn.weitee.erp.module.project.service.report.impl;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.security.core.util.SecurityFrameworkUtils;
import cn.weitee.erp.module.project.controller.admin.vo.report.ProjectReportPageReqVO;
import cn.weitee.erp.module.project.controller.admin.vo.report.ProjectReportSaveReqVO;
import cn.weitee.erp.module.project.dal.dataobject.report.ProjectReportDO;
import cn.weitee.erp.module.project.dal.dataobject.report.ProjectReportLinkDO;
import cn.weitee.erp.module.project.dal.dataobject.report.ProjectReportReceiveDO;
import cn.weitee.erp.module.project.dal.mysql.report.ProjectReportLinkMapper;
import cn.weitee.erp.module.project.dal.mysql.report.ProjectReportMapper;
import cn.weitee.erp.module.project.dal.mysql.report.ProjectReportReceiveMapper;
import cn.weitee.erp.module.project.service.report.ProjectReportService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.project.enums.ErrorCodeConstants.REPORT_NOT_FOUND;

@Service
@Validated
public class ProjectReportServiceImpl implements ProjectReportService {

    @Resource
    private ProjectReportMapper projectReportMapper;
    @Resource
    private ProjectReportReceiveMapper projectReportReceiveMapper;
    @Resource
    private ProjectReportLinkMapper projectReportLinkMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createReport(@Valid ProjectReportSaveReqVO createReqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        ProjectReportDO report = ProjectReportDO.builder()
                .userId(userId)
                .type(createReqVO.getType())
                .content(createReqVO.getContent())
                .sign(createReqVO.getSign())
                .build();
        projectReportMapper.insert(report);
        return report.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateReport(@Valid ProjectReportSaveReqVO updateReqVO) {
        validateReport(updateReqVO.getId());
        ProjectReportDO updateObj = ProjectReportDO.builder()
                .id(updateReqVO.getId())
                .type(updateReqVO.getType())
                .content(updateReqVO.getContent())
                .sign(updateReqVO.getSign())
                .build();
        projectReportMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteReport(Long id) {
        validateReport(id);
        projectReportMapper.deleteById(id);
        projectReportReceiveMapper.delete(ProjectReportReceiveDO::getReportId, id);
        projectReportLinkMapper.delete(ProjectReportLinkDO::getReportId, id);
    }

    @Override
    public ProjectReportDO getReport(Long id) {
        return projectReportMapper.selectById(id);
    }

    @Override
    public PageResult<ProjectReportDO> getReportPage(ProjectReportPageReqVO pageReqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return projectReportMapper.selectPage(pageReqVO, new LambdaQueryWrapper<ProjectReportDO>()
                .eq(ProjectReportDO::getUserId, userId)
                .like(ProjectReportDO::getType, pageReqVO.getType())
                .orderByDesc(ProjectReportDO::getId));
    }

    @Override
    public PageResult<ProjectReportDO> getReceiveReportPage(ProjectReportPageReqVO pageReqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return projectReportMapper.selectPage(pageReqVO, new LambdaQueryWrapper<ProjectReportDO>()
                .inSql(ProjectReportDO::getId,
                        "SELECT report_id FROM project_report_receive WHERE user_id = " + userId)
                .like(ProjectReportDO::getType, pageReqVO.getType())
                .orderByDesc(ProjectReportDO::getId));
    }

    private ProjectReportDO validateReport(Long id) {
        ProjectReportDO report = projectReportMapper.selectById(id);
        if (report == null) {
            throw exception(REPORT_NOT_FOUND);
        }
        return report;
    }
}
