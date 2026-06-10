package cn.iocoder.yudao.module.bpm.service.approval;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.bpm.controller.admin.approval.vo.scheme.BpmApprovalSchemePageReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.approval.vo.scheme.BpmApprovalSchemePublishReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.approval.vo.scheme.BpmApprovalSchemeRespVO;
import cn.iocoder.yudao.module.bpm.controller.admin.approval.vo.scheme.BpmApprovalSchemeSaveReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.approval.vo.scheme.BpmApprovalSchemeSubmitReqVO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.approval.BpmApprovalRuleDO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.approval.BpmApprovalSchemeDO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.approval.BpmApprovalSchemeVersionDO;
import cn.iocoder.yudao.module.bpm.dal.mysql.approval.BpmApprovalRuleMapper;
import cn.iocoder.yudao.module.bpm.dal.mysql.approval.BpmApprovalSceneMapper;
import cn.iocoder.yudao.module.bpm.dal.mysql.approval.BpmApprovalSchemeMapper;
import cn.iocoder.yudao.module.bpm.dal.mysql.approval.BpmApprovalSchemeVersionMapper;
import cn.iocoder.yudao.module.bpm.enums.approval.BpmApprovalSchemeStatusEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.*;

/**
 * 审批方案 Service 实现类
 */
@Service
@Validated
public class BpmApprovalSchemeServiceImpl implements BpmApprovalSchemeService {

    private static final String SOURCE_TYPE_CREATE = "CREATE";
    private static final String SOURCE_TYPE_COPY = "COPY";

    @Resource
    private BpmApprovalSchemeMapper bpmApprovalSchemeMapper;

    @Resource
    private BpmApprovalSchemeVersionMapper bpmApprovalSchemeVersionMapper;

    @Resource
    private BpmApprovalRuleMapper bpmApprovalRuleMapper;

    @Resource
    private BpmApprovalSceneMapper bpmApprovalSceneMapper;

    @Override
    public PageResult<BpmApprovalSchemeRespVO> getSchemePage(BpmApprovalSchemePageReqVO pageReqVO) {
        return bpmApprovalSchemeMapper.selectPage(pageReqVO);
    }

    @Override
    public BpmApprovalSchemeRespVO getScheme(Long id) {
        BpmApprovalSchemeDO scheme = validateSchemeExists(id);
        BpmApprovalSchemeRespVO respVO = BeanUtils.toBean(scheme, BpmApprovalSchemeRespVO.class);
        if (scheme.getLatestVersionId() == null) {
            respVO.setRules(Collections.emptyList());
            return respVO;
        }
        BpmApprovalSchemeVersionDO latestVersion = validateVersionExists(scheme.getLatestVersionId());
        respVO.setLatestVersionId(latestVersion.getId());
        respVO.setLatestVersionNo(latestVersion.getVersionNo());
        respVO.setLatestVersionStatus(latestVersion.getStatus());
        respVO.setDesignJson(latestVersion.getDesignJson());
        respVO.setRules(convertRules(bpmApprovalRuleMapper.selectListBySchemeVersionId(latestVersion.getId())));
        return respVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDraft(BpmApprovalSchemeSaveReqVO createReqVO) {
        if (createReqVO.getId() == null) {
            validateSchemeCodeUnique(null, createReqVO.getCode());
            BpmApprovalSchemeDO scheme = BeanUtils.toBean(createReqVO, BpmApprovalSchemeDO.class);
            // 设置归属用户ID为当前登录用户
            scheme.setOwnerUserId(SecurityFrameworkUtils.getLoginUserId());
            scheme.setActiveVersionId(null);
            scheme.setLatestVersionId(null);
            bpmApprovalSchemeMapper.insert(scheme);
            Long versionId = createDraftVersion(scheme.getId(), null, 1, createReqVO);
            bpmApprovalSchemeMapper.updateById(new BpmApprovalSchemeDO()
                    .setId(scheme.getId())
                    .setLatestVersionId(versionId));
            return versionId;
        }

        BpmApprovalSchemeDO scheme = validateSchemeExists(createReqVO.getId());
        validateSchemeOwnership(scheme);
        validateSchemeCodeUnique(scheme.getId(), createReqVO.getCode());
        if (createReqVO.getVersionId() != null) {
            BpmApprovalSchemeVersionDO sourceVersion = validateVersionExists(createReqVO.getVersionId());
            if (!ObjUtil.equal(sourceVersion.getSchemeId(), scheme.getId())) {
                throw exception(APPROVAL_SCHEME_VERSION_NOT_EXISTS);
            }
        }
        updateSchemeBase(scheme.getId(), createReqVO);
        BpmApprovalSchemeVersionDO latestVersion = bpmApprovalSchemeVersionMapper.selectLatestBySchemeId(scheme.getId());
        Integer nextVersionNo = latestVersion == null ? 1 : latestVersion.getVersionNo() + 1;
        Long versionId = createDraftVersion(scheme.getId(), createReqVO.getVersionId(), nextVersionNo, createReqVO);
        bpmApprovalSchemeMapper.updateById(new BpmApprovalSchemeDO()
                .setId(scheme.getId())
                .setLatestVersionId(versionId));
        return versionId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDraft(BpmApprovalSchemeSaveReqVO updateReqVO) {
        BpmApprovalSchemeDO scheme = validateSchemeExists(updateReqVO.getId());
        validateSchemeOwnership(scheme);
        BpmApprovalSchemeVersionDO version = validateVersionExists(updateReqVO.getVersionId());
        if (!ObjUtil.equal(version.getSchemeId(), scheme.getId())) {
            throw exception(APPROVAL_SCHEME_VERSION_NOT_EXISTS);
        }
        validateVersionStatus(version, BpmApprovalSchemeStatusEnum.DRAFT, APPROVAL_SCHEME_VERSION_NOT_DRAFT);
        validateSchemeCodeUnique(scheme.getId(), updateReqVO.getCode());

        updateSchemeBase(scheme.getId(), updateReqVO);
        bpmApprovalSchemeVersionMapper.updateById(new BpmApprovalSchemeVersionDO()
                .setId(version.getId())
                .setDesignJson(updateReqVO.getDesignJson())
                .setNotifyJson(updateReqVO.getNotifyJson()));
        replaceRules(version.getId(), updateReqVO.getRules());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submit(BpmApprovalSchemeSubmitReqVO submitReqVO) {
        BpmApprovalSchemeVersionDO version = validateVersionExists(submitReqVO.getVersionId());
        BpmApprovalSchemeDO scheme = validateSchemeExists(version.getSchemeId());
        validateSchemeOwnership(scheme);
        validateVersionStatus(version, BpmApprovalSchemeStatusEnum.DRAFT, APPROVAL_SCHEME_VERSION_NOT_DRAFT);
        validateDefaultRule(version.getId());
        bpmApprovalSchemeVersionMapper.updateById(new BpmApprovalSchemeVersionDO()
                .setId(version.getId())
                .setStatus(BpmApprovalSchemeStatusEnum.PENDING_PUBLISH.getStatus()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publish(BpmApprovalSchemePublishReqVO publishReqVO) {
        BpmApprovalSchemeVersionDO version = validateVersionExists(publishReqVO.getVersionId());
        validateVersionStatus(version, BpmApprovalSchemeStatusEnum.PENDING_PUBLISH, APPROVAL_SCHEME_VERSION_NOT_PENDING_PUBLISH);
        validateDefaultRule(version.getId());
        BpmApprovalSchemeDO scheme = validateSchemeExists(version.getSchemeId());
        validateSchemeOwnership(scheme);

        if (scheme.getActiveVersionId() != null && !ObjUtil.equal(scheme.getActiveVersionId(), version.getId())) {
            bpmApprovalSchemeVersionMapper.updateById(new BpmApprovalSchemeVersionDO()
                    .setId(scheme.getActiveVersionId())
                    .setStatus(BpmApprovalSchemeStatusEnum.DISABLED.getStatus()));
        }

        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        bpmApprovalSchemeVersionMapper.updateById(new BpmApprovalSchemeVersionDO()
                .setId(version.getId())
                .setStatus(BpmApprovalSchemeStatusEnum.ACTIVE.getStatus())
                .setPublishedBy(loginUserId == null ? null : String.valueOf(loginUserId))
                .setPublishedTime(LocalDateTime.now()));
        bpmApprovalSchemeMapper.updateById(new BpmApprovalSchemeDO()
                .setId(scheme.getId())
                .setActiveVersionId(version.getId())
                .setLatestVersionId(version.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disable(Long versionId) {
        BpmApprovalSchemeVersionDO version = validateVersionExists(versionId);
        validateVersionStatus(version, BpmApprovalSchemeStatusEnum.ACTIVE, APPROVAL_SCHEME_VERSION_NOT_ACTIVE);
        BpmApprovalSchemeDO scheme = validateSchemeExists(version.getSchemeId());
        validateSchemeOwnership(scheme);
        validateSchemeNotBoundToScene(scheme.getId());

        bpmApprovalSchemeVersionMapper.updateById(new BpmApprovalSchemeVersionDO()
                .setId(versionId)
                .setStatus(BpmApprovalSchemeStatusEnum.DISABLED.getStatus()));

        if (ObjUtil.equal(scheme.getActiveVersionId(), versionId)) {
            bpmApprovalSchemeMapper.updateById(new BpmApprovalSchemeDO()
                    .setId(scheme.getId())
                    .setActiveVersionId(null));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void switchActiveVersion(Long versionId) {
        BpmApprovalSchemeVersionDO version = validateVersionExists(versionId);
        validateVersionStatus(version, BpmApprovalSchemeStatusEnum.ACTIVE, APPROVAL_SCHEME_VERSION_NOT_ACTIVE);
        BpmApprovalSchemeDO scheme = validateSchemeExists(version.getSchemeId());
        validateSchemeOwnership(scheme);

        bpmApprovalSchemeMapper.updateById(new BpmApprovalSchemeDO()
                .setId(scheme.getId())
                .setActiveVersionId(versionId));
    }

    private Long createDraftVersion(Long schemeId, Long sourceVersionId, Integer versionNo,
                                    BpmApprovalSchemeSaveReqVO reqVO) {
        BpmApprovalSchemeVersionDO version = new BpmApprovalSchemeVersionDO()
                .setSchemeId(schemeId)
                .setVersionNo(versionNo)
                .setStatus(BpmApprovalSchemeStatusEnum.DRAFT.getStatus())
                .setSourceVersionId(sourceVersionId)
                .setSourceType(sourceVersionId == null ? SOURCE_TYPE_CREATE : SOURCE_TYPE_COPY)
                .setDesignJson(reqVO.getDesignJson())
                .setNotifyJson(reqVO.getNotifyJson());
        bpmApprovalSchemeVersionMapper.insert(version);
        replaceRules(version.getId(), reqVO.getRules());
        return version.getId();
    }

    private void replaceRules(Long versionId, List<BpmApprovalSchemeSaveReqVO.Rule> rules) {
        bpmApprovalRuleMapper.delete(BpmApprovalRuleDO::getSchemeVersionId, versionId);
        if (CollUtil.isEmpty(rules)) {
            return;
        }
        List<BpmApprovalRuleDO> ruleDOList = BeanUtils.toBean(rules, BpmApprovalRuleDO.class, item -> item
                .setSchemeVersionId(versionId)
                .setEnabled(item.getEnabled() == null ? Boolean.TRUE : item.getEnabled())
                .setDefaultRule(Boolean.TRUE.equals(item.getDefaultRule())));
        bpmApprovalRuleMapper.insertBatch(ruleDOList);
    }

    private void validateDefaultRule(Long versionId) {
        List<BpmApprovalRuleDO> rules = bpmApprovalRuleMapper.selectListBySchemeVersionId(versionId);
        long defaultRuleCount = rules.stream()
                .filter(rule -> Boolean.TRUE.equals(rule.getDefaultRule()))
                .count();
        if (defaultRuleCount == 0) {
            throw exception(APPROVAL_SCHEME_DEFAULT_RULE_REQUIRED);
        }
        if (defaultRuleCount > 1) {
            throw exception(APPROVAL_SCHEME_DEFAULT_RULE_DUPLICATE);
        }
    }

    private void updateSchemeBase(Long schemeId, BpmApprovalSchemeSaveReqVO reqVO) {
        bpmApprovalSchemeMapper.updateById(new BpmApprovalSchemeDO()
                .setId(schemeId)
                .setName(reqVO.getName())
                .setCode(reqVO.getCode())
                .setModuleCode(reqVO.getModuleCode())
                .setBizType(reqVO.getBizType())
                .setRemark(reqVO.getRemark()));
    }

    private void validateSchemeCodeUnique(Long id, String code) {
        BpmApprovalSchemeDO scheme = bpmApprovalSchemeMapper.selectByCode(code);
        if (scheme == null || ObjUtil.equal(scheme.getId(), id)) {
            return;
        }
        throw exception(APPROVAL_SCHEME_CODE_DUPLICATE, code);
    }

    private void validateSchemeNotBoundToScene(Long schemeId) {
        Long count = bpmApprovalSceneMapper.selectCountByActiveSchemeId(schemeId);
        if (count != null && count > 0) {
            throw exception(APPROVAL_SCHEME_BIND_TO_SCENE);
        }
    }

    private BpmApprovalSchemeDO validateSchemeExists(Long id) {
        BpmApprovalSchemeDO scheme = bpmApprovalSchemeMapper.selectById(id);
        if (scheme == null) {
            throw exception(APPROVAL_SCHEME_NOT_EXISTS);
        }
        return scheme;
    }

    /**
     * 校验当前用户是否有权限操作该方案
     * 
     * 流程配置管理员只能操作自己配置的方案
     * 
     * @param scheme 审批方案
     */
    private void validateSchemeOwnership(BpmApprovalSchemeDO scheme) {
        Long currentUserId = SecurityFrameworkUtils.getLoginUserId();
        // 如果 ownerUserId 为空或者是当前用户，则允许操作
        if (scheme.getOwnerUserId() == null || scheme.getOwnerUserId().equals(currentUserId)) {
            return;
        }
        // TODO: 这里可以添加更复杂的权限校验逻辑，如检查用户角色
        // 暂时简单校验：只有归属用户才能操作
        throw exception(APPROVAL_SCENE_NO_PERMISSION);
    }

    private BpmApprovalSchemeVersionDO validateVersionExists(Long id) {
        BpmApprovalSchemeVersionDO version = bpmApprovalSchemeVersionMapper.selectById(id);
        if (version == null) {
            throw exception(APPROVAL_SCHEME_VERSION_NOT_EXISTS);
        }
        return version;
    }

    private void validateVersionStatus(BpmApprovalSchemeVersionDO version,
                                       BpmApprovalSchemeStatusEnum expectedStatus,
                                       cn.iocoder.yudao.framework.common.exception.ErrorCode errorCode) {
        if (ObjUtil.equal(version.getStatus(), expectedStatus.getStatus())) {
            return;
        }
        throw exception(errorCode);
    }

    private List<BpmApprovalSchemeRespVO.Rule> convertRules(List<BpmApprovalRuleDO> rules) {
        return BeanUtils.toBean(rules, BpmApprovalSchemeRespVO.Rule.class);
    }

}
