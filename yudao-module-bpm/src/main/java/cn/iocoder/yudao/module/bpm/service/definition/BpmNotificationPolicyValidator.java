package cn.iocoder.yudao.module.bpm.service.definition;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.BpmModelMetaInfoVO;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmDingTalkTemplateSourceTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmInternalNotificationTemplateSourceTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmNotificationSceneEnum;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.*;

public class BpmNotificationPolicyValidator {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{([a-zA-Z][a-zA-Z0-9]*)}");

    public void validateForDeploy(BpmModelMetaInfoVO.NotificationPolicySetting setting) {
        if (setting == null || !Boolean.TRUE.equals(setting.getEnable())) {
            return;
        }
        if (CollUtil.isEmpty(setting.getScenes())) {
            throw exception(MODEL_DEPLOY_FAIL_NOTIFICATION_SCENE_NOT_CONFIG);
        }

        boolean hasEnabledScene = false;
        for (BpmModelMetaInfoVO.ScenePolicy scene : setting.getScenes()) {
            if (!Boolean.TRUE.equals(scene.getEnabled())) {
                continue;
            }
            hasEnabledScene = true;
            validateScene(scene);
        }
        if (!hasEnabledScene) {
            throw exception(MODEL_DEPLOY_FAIL_NOTIFICATION_SCENE_NOT_CONFIG);
        }
    }

    private void validateScene(BpmModelMetaInfoVO.ScenePolicy scene) {
        BpmNotificationSceneEnum sceneEnum = BpmNotificationSceneEnum.getByCode(scene.getSceneCode());
        if (sceneEnum == null) {
            throw exception(MODEL_DEPLOY_FAIL_NOTIFICATION_SCENE_INVALID, scene.getSceneCode());
        }
        boolean internalEnabled = scene.getInternalMessage() != null
                && Boolean.TRUE.equals(scene.getInternalMessage().getEnabled());
        boolean dingTalkEnabled = scene.getDingTalk() != null
                && Boolean.TRUE.equals(scene.getDingTalk().getEnabled());
        if (!internalEnabled && !dingTalkEnabled) {
            throw exception(MODEL_DEPLOY_FAIL_NOTIFICATION_CHANNEL_NOT_CONFIG, sceneEnum.getName());
        }
        if (internalEnabled) {
            validateInternalMessage(sceneEnum, scene.getInternalMessage());
        }
        if (dingTalkEnabled) {
            validateDingTalk(sceneEnum, scene.getDingTalk());
        }
    }

    private void validateInternalMessage(BpmNotificationSceneEnum sceneEnum,
                                         BpmModelMetaInfoVO.InternalTemplateSetting internalMessage) {
        if (!StrUtil.equals(BpmInternalNotificationTemplateSourceTypeEnum.INLINE.getCode(), internalMessage.getSourceType())) {
            return;
        }
        if (StrUtil.isBlank(internalMessage.getContent())) {
            throw exception(MODEL_DEPLOY_FAIL_NOTIFICATION_CONTENT_EMPTY, sceneEnum.getName());
        }
        validateVariables(sceneEnum, internalMessage.getTitle());
        validateVariables(sceneEnum, internalMessage.getContent());
    }

    private void validateDingTalk(BpmNotificationSceneEnum sceneEnum,
                                  BpmModelMetaInfoVO.DingTalkTemplateSetting dingTalk) {
        if (StrUtil.equals(BpmDingTalkTemplateSourceTypeEnum.TEMPLATE.getCode(), dingTalk.getSourceType())
                && StrUtil.isBlank(dingTalk.getTemplateCode())) {
            throw exception(MODEL_DEPLOY_FAIL_NOTIFICATION_DING_TALK_TEMPLATE_EMPTY, sceneEnum.getName());
        }
    }

    private void validateVariables(BpmNotificationSceneEnum sceneEnum, String template) {
        if (StrUtil.isBlank(template)) {
            return;
        }
        for (String variable : extractVariables(template)) {
            if (!sceneEnum.getAllowedVariables().contains(variable)) {
                throw exception(MODEL_DEPLOY_FAIL_NOTIFICATION_VARIABLE_ILLEGAL, sceneEnum.getName(), variable);
            }
        }
    }

    private Set<String> extractVariables(String template) {
        Set<String> variables = new LinkedHashSet<>();
        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        while (matcher.find()) {
            variables.add(matcher.group(1));
        }
        return variables;
    }

}
