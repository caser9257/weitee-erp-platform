package cn.weitee.erp.module.bpm.service.approval;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.bpm.controller.admin.approval.vo.scheme.BpmApprovalSchemePageReqVO;
import cn.weitee.erp.module.bpm.controller.admin.approval.vo.scheme.BpmApprovalSchemePublishReqVO;
import cn.weitee.erp.module.bpm.controller.admin.approval.vo.scheme.BpmApprovalSchemeRespVO;
import cn.weitee.erp.module.bpm.controller.admin.approval.vo.scheme.BpmApprovalSchemeSaveReqVO;
import cn.weitee.erp.module.bpm.controller.admin.approval.vo.scheme.BpmApprovalSchemeSubmitReqVO;

import jakarta.validation.Valid;

/**
 * 审批方案 Service
 */
public interface BpmApprovalSchemeService {

    PageResult<BpmApprovalSchemeRespVO> getSchemePage(BpmApprovalSchemePageReqVO pageReqVO);

    BpmApprovalSchemeRespVO getScheme(Long id);

    Long createDraft(@Valid BpmApprovalSchemeSaveReqVO createReqVO);

    void updateDraft(@Valid BpmApprovalSchemeSaveReqVO updateReqVO);

    void submit(@Valid BpmApprovalSchemeSubmitReqVO submitReqVO);

    void publish(@Valid BpmApprovalSchemePublishReqVO publishReqVO);

    void disable(Long versionId);

    /**
     * 切换当前生效版本
     *
     * @param versionId 目标版本编号（必须是 ACTIVE 状态）
     */
    void switchActiveVersion(Long versionId);

}
