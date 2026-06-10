package cn.iocoder.yudao.module.bpm.service.approval;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.bpm.controller.admin.approval.vo.template.BpmApprovalTemplatePageReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.approval.vo.template.BpmApprovalTemplateRespVO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.approval.BpmApprovalSceneDO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.approval.BpmApprovalSchemeDO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.approval.BpmApprovalSchemeVersionDO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.approval.BpmApprovalTemplateDO;
import cn.iocoder.yudao.module.bpm.dal.mysql.approval.BpmApprovalSceneMapper;
import cn.iocoder.yudao.module.bpm.dal.mysql.approval.BpmApprovalSchemeMapper;
import cn.iocoder.yudao.module.bpm.dal.mysql.approval.BpmApprovalSchemeVersionMapper;
import cn.iocoder.yudao.module.bpm.dal.mysql.approval.BpmApprovalTemplateMapper;
import cn.iocoder.yudao.module.bpm.enums.approval.BpmApprovalSchemeStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.APPROVAL_SCENE_NOT_EXISTS;

/**
 * 审批模板 Service 实现类
 */
@Service
@Validated
@Slf4j
public class BpmApprovalTemplateServiceImpl implements BpmApprovalTemplateService {

    @Resource
    private BpmApprovalTemplateMapper approvalTemplateMapper;

    @Resource
    private BpmApprovalSceneMapper approvalSceneMapper;

    @Resource
    private BpmApprovalSchemeMapper approvalSchemeMapper;

    @Resource
    private BpmApprovalSchemeVersionMapper approvalSchemeVersionMapper;

    @Override
    public PageResult<BpmApprovalTemplateRespVO> getTemplatePage(BpmApprovalTemplatePageReqVO pageReqVO) {
        PageResult<BpmApprovalTemplateDO> pageResult = approvalTemplateMapper.selectPage(pageReqVO);
        return BeanUtils.toBean(pageResult, BpmApprovalTemplateRespVO.class);
    }

    @Override
    public BpmApprovalTemplateRespVO getTemplate(Long id) {
        BpmApprovalTemplateDO template = approvalTemplateMapper.selectById(id);
        if (template == null) {
            throw exception(APPROVAL_SCENE_NOT_EXISTS);
        }
        return BeanUtils.toBean(template, BpmApprovalTemplateRespVO.class);
    }

    @Override
    public BpmApprovalTemplateRespVO getTemplateByCode(String code) {
        BpmApprovalTemplateDO template = approvalTemplateMapper.selectByCode(code);
        if (template == null) {
            throw exception(APPROVAL_SCENE_NOT_EXISTS);
        }
        return BeanUtils.toBean(template, BpmApprovalTemplateRespVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long useTemplate(Long templateId, Long userId) {
        // 1. 获取模板
        BpmApprovalTemplateDO template = approvalTemplateMapper.selectById(templateId);
        if (template == null) {
            throw exception(APPROVAL_SCENE_NOT_EXISTS);
        }

        // 2. 生成场景编码（使用模板编码 + 时间戳）
        String sceneCode = template.getCode().toLowerCase() + "." + System.currentTimeMillis();

        // 3. 创建审批场景
        BpmApprovalSceneDO scene = BpmApprovalSceneDO.builder()
                .sceneCode(sceneCode)
                .name(template.getName())
                .moduleCode(template.getCategory())
                .bizType(template.getCode())
                .actionCode("submit")
                .ownerUserId(userId)
                .status(1) // 启用
                .remark(template.getDescription())
                .build();
        approvalSceneMapper.insert(scene);

        // 4. 创建审批方案
        BpmApprovalSchemeDO scheme = BpmApprovalSchemeDO.builder()
                .code(sceneCode + ".scheme")
                .name(template.getName() + "方案")
                .moduleCode(template.getCategory())
                .bizType(template.getCode())
                .sceneId(scene.getId())
                .ownerUserId(userId)
                .remark(template.getDescription())
                .build();
        approvalSchemeMapper.insert(scheme);

        // 5. 创建审批版本
        BpmApprovalSchemeVersionDO version = BpmApprovalSchemeVersionDO.builder()
                .schemeId(scheme.getId())
                .versionNo(1)
                .status(BpmApprovalSchemeStatusEnum.DRAFT.getStatus())
                .sourceType("TEMPLATE")
                .designJson(JSONUtil.toJsonStr(template.getFlowConfig()))
                .notifyJson(JSONUtil.toJsonStr(template.getNotifyConfig()))
                .build();
        approvalSchemeVersionMapper.insert(version);

        // 6. 更新方案的最新版本
        approvalSchemeMapper.updateById(new BpmApprovalSchemeDO()
                .setId(scheme.getId())
                .setLatestVersionId(version.getId()));

        // 7. 更新模板使用次数
        approvalTemplateMapper.updateById(new BpmApprovalTemplateDO()
                .setId(templateId)
                .setUseCount(template.getUseCount() == null ? 1 : template.getUseCount() + 1));

        return scene.getId();
    }

}
