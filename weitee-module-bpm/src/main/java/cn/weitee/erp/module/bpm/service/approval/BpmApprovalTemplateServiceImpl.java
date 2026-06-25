package cn.weitee.erp.module.bpm.service.approval;

import cn.hutool.json.JSONUtil;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.bpm.controller.admin.approval.vo.template.BpmApprovalTemplatePageReqVO;
import cn.weitee.erp.module.bpm.controller.admin.approval.vo.template.BpmApprovalTemplateRespVO;
import cn.weitee.erp.module.bpm.dal.dataobject.approval.BpmApprovalSceneDO;
import cn.weitee.erp.module.bpm.dal.dataobject.approval.BpmApprovalSchemeDO;
import cn.weitee.erp.module.bpm.dal.dataobject.approval.BpmApprovalSchemeVersionDO;
import cn.weitee.erp.module.bpm.dal.dataobject.approval.BpmApprovalTemplateDO;
import cn.weitee.erp.module.bpm.dal.mysql.approval.BpmApprovalSceneMapper;
import cn.weitee.erp.module.bpm.dal.mysql.approval.BpmApprovalSchemeMapper;
import cn.weitee.erp.module.bpm.dal.mysql.approval.BpmApprovalSchemeVersionMapper;
import cn.weitee.erp.module.bpm.dal.mysql.approval.BpmApprovalTemplateMapper;
import cn.weitee.erp.module.bpm.enums.approval.BpmApprovalSchemeStatusEnum;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Map;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.bpm.enums.ErrorCodeConstants.APPROVAL_TEMPLATE_NOT_EXISTS;

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
            throw exception(APPROVAL_TEMPLATE_NOT_EXISTS);
        }
        return BeanUtils.toBean(template, BpmApprovalTemplateRespVO.class);
    }

    @Override
    public BpmApprovalTemplateRespVO getTemplateByCode(String code) {
        BpmApprovalTemplateDO template = approvalTemplateMapper.selectByCode(code);
        if (template == null) {
            throw exception(APPROVAL_TEMPLATE_NOT_EXISTS);
        }
        return BeanUtils.toBean(template, BpmApprovalTemplateRespVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long useTemplate(Long templateId, Long userId) {
        // 获取模板
        BpmApprovalTemplateDO template = approvalTemplateMapper.selectById(templateId);
        if (template == null) {
            throw exception(APPROVAL_TEMPLATE_NOT_EXISTS);
        }
        return doUseTemplate(template, template.getFlowConfig(), userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long useTemplateWithFlow(Long templateId, Map<String, Object> flowConfig, Long userId) {
        // 获取模板
        BpmApprovalTemplateDO template = approvalTemplateMapper.selectById(templateId);
        if (template == null) {
            throw exception(APPROVAL_TEMPLATE_NOT_EXISTS);
        }
        return doUseTemplate(template, flowConfig, userId);
    }

    /**
     * 使用模板创建审批场景、方案和版本的公共逻辑
     *
     * @param template   模板
     * @param flowConfig 流程配置（模板内置或用户自定义）
     * @param userId     操作人
     * @return 创建的场景 ID
     */
    private Long doUseTemplate(BpmApprovalTemplateDO template, Object flowConfig, Long userId) {
        // 1. 生成场景编码（使用模板编码 + 时间戳）
        String sceneCode = template.getCode().toLowerCase() + "." + System.currentTimeMillis();

        // 2. 创建审批场景
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

        // 3. 创建审批方案
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

        // 4. 创建审批版本（状态：草稿）
        BpmApprovalSchemeVersionDO version = BpmApprovalSchemeVersionDO.builder()
                .schemeId(scheme.getId())
                .versionNo(1)
                .status(BpmApprovalSchemeStatusEnum.DRAFT.getStatus())
                .sourceType("TEMPLATE")
                .designJson(JSONUtil.toJsonStr(flowConfig))
                .notifyJson(JSONUtil.toJsonStr(template.getNotifyConfig()))
                .build();
        approvalSchemeVersionMapper.insert(version);

        // 5. 更新方案的最新版本
        approvalSchemeMapper.updateById(new BpmApprovalSchemeDO()
                .setId(scheme.getId())
                .setLatestVersionId(version.getId()));

        // 6. 自动提交方案（草稿 → 待发布）
        approvalSchemeVersionMapper.updateById(new BpmApprovalSchemeVersionDO()
                .setId(version.getId())
                .setStatus(BpmApprovalSchemeStatusEnum.PENDING_PUBLISH.getStatus()));

        // 7. 自动发布方案（待发布 → 生效中）
        approvalSchemeVersionMapper.updateById(new BpmApprovalSchemeVersionDO()
                .setId(version.getId())
                .setStatus(BpmApprovalSchemeStatusEnum.ACTIVE.getStatus())
                .setPublishedBy(String.valueOf(userId))
                .setPublishedTime(LocalDateTime.now()));
        approvalSchemeMapper.updateById(new BpmApprovalSchemeDO()
                .setId(scheme.getId())
                .setActiveVersionId(version.getId())
                .setLatestVersionId(version.getId()));

        // 8. 自动绑定方案到场景
        approvalSceneMapper.update(null, new LambdaUpdateWrapper<BpmApprovalSceneDO>()
                .eq(BpmApprovalSceneDO::getId, scene.getId())
                .set(BpmApprovalSceneDO::getActiveSchemeId, scheme.getId()));

        // 9. 更新模板使用次数
        approvalTemplateMapper.updateById(new BpmApprovalTemplateDO()
                .setId(template.getId())
                .setUseCount(template.getUseCount() == null ? 1 : template.getUseCount() + 1));

        return scene.getId();
    }

}
