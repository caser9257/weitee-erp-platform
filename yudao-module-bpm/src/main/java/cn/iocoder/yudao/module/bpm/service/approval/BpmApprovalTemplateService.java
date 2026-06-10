package cn.iocoder.yudao.module.bpm.service.approval;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.bpm.controller.admin.approval.vo.template.BpmApprovalTemplatePageReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.approval.vo.template.BpmApprovalTemplateRespVO;

/**
 * 审批模板 Service 接口
 */
public interface BpmApprovalTemplateService {

    /**
     * 获取审批模板分页
     */
    PageResult<BpmApprovalTemplateRespVO> getTemplatePage(BpmApprovalTemplatePageReqVO pageReqVO);

    /**
     * 获取审批模板详情
     */
    BpmApprovalTemplateRespVO getTemplate(Long id);

    /**
     * 根据模板编码获取审批模板
     */
    BpmApprovalTemplateRespVO getTemplateByCode(String code);

    /**
     * 使用模板创建审批场景和方案
     *
     * @param templateId 模板 ID
     * @param userId 用户 ID
     * @return 场景 ID
     */
    Long useTemplate(Long templateId, Long userId);

}
