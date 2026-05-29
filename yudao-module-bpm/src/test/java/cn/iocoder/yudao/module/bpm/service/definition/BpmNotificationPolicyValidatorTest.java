package cn.iocoder.yudao.module.bpm.service.definition;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BpmNotificationPolicyValidatorTest {

    @Test
    void validateForDeploy_shouldRejectWhenNotificationEnabledButNoSceneEnabled() throws Exception {
        ServiceException exception = invokeValidateExpectException("{\"enable\":true,\"publishCheckMode\":\"STRICT\",\"scenes\":[]}");

        assertEquals(1_009_002_009, exception.getCode());
        assertTrue(exception.getMessage().contains("通知场景"));
    }

    @Test
    void validateForDeploy_shouldRejectWhenSceneHasNoChannel() throws Exception {
        ServiceException exception = invokeValidateExpectException("{\"enable\":true,\"publishCheckMode\":\"STRICT\","
                + "\"scenes\":[{\"sceneCode\":\"TASK_ASSIGNED\",\"enabled\":true,"
                + "\"internalMessage\":{\"enabled\":false,\"sourceType\":\"DEFAULT\"},"
                + "\"dingTalk\":{\"enabled\":false,\"sourceType\":\"RESERVED\"}}]}");

        assertEquals(1_009_002_010, exception.getCode());
        assertTrue(exception.getMessage().contains("任务到达提醒"));
    }

    @Test
    void validateForDeploy_shouldRejectWhenInlineContentIsBlank() throws Exception {
        ServiceException exception = invokeValidateExpectException("{\"enable\":true,\"publishCheckMode\":\"STRICT\","
                + "\"scenes\":[{\"sceneCode\":\"TASK_ASSIGNED\",\"enabled\":true,"
                + "\"internalMessage\":{\"enabled\":true,\"sourceType\":\"INLINE\",\"title\":\"标题\",\"content\":\"   \"},"
                + "\"dingTalk\":{\"enabled\":false,\"sourceType\":\"RESERVED\"}}]}");

        assertEquals(1_009_002_011, exception.getCode());
        assertTrue(exception.getMessage().contains("任务到达提醒"));
    }

    @Test
    void validateForDeploy_shouldRejectWhenInlineTemplateContainsIllegalVariable() throws Exception {
        ServiceException exception = invokeValidateExpectException("{\"enable\":true,\"publishCheckMode\":\"STRICT\","
                + "\"scenes\":[{\"sceneCode\":\"PROCESS_REJECT\",\"enabled\":true,"
                + "\"internalMessage\":{\"enabled\":true,\"sourceType\":\"INLINE\","
                + "\"title\":\"流程{unknownVar}\",\"content\":\"请查看{detailUrl}\"},"
                + "\"dingTalk\":{\"enabled\":false,\"sourceType\":\"RESERVED\"}}]}");

        assertEquals(1_009_002_012, exception.getCode());
        assertTrue(exception.getMessage().contains("unknownVar"));
    }

    @Test
    void validateForDeploy_shouldAllowValidInlinePolicy() throws Exception {
        assertDoesNotThrow(() -> invokeValidate("{\"enable\":true,\"publishCheckMode\":\"STRICT\","
                + "\"scenes\":[{\"sceneCode\":\"PROCESS_REJECT\",\"enabled\":true,"
                + "\"internalMessage\":{\"enabled\":true,\"sourceType\":\"INLINE\","
                + "\"title\":\"流程{processInstanceName}\",\"content\":\"驳回原因：{reason}，详情：{detailUrl}\"},"
                + "\"dingTalk\":{\"enabled\":true,\"sourceType\":\"RESERVED\"}}]}"));
    }

    private void invokeValidate(String json) throws Exception {
        Class<?> validatorClass = Class.forName("cn.iocoder.yudao.module.bpm.service.definition.BpmNotificationPolicyValidator");
        Object validator = validatorClass.getDeclaredConstructor().newInstance();
        Class<?> settingClass = Class.forName(
                "cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.BpmModelMetaInfoVO$NotificationPolicySetting");
        Method validateMethod = validatorClass.getMethod("validateForDeploy", settingClass);
        Object setting = JsonUtils.parseObject(json, (Class<Object>) settingClass);
        validateMethod.invoke(validator, setting);
    }

    private ServiceException invokeValidateExpectException(String json) throws Exception {
        try {
            invokeValidate(json);
        } catch (InvocationTargetException ex) {
            Throwable targetException = ex.getTargetException();
            if (targetException instanceof ServiceException) {
                return (ServiceException) targetException;
            }
            throw ex;
        }
        throw new AssertionError("Expected ServiceException");
    }

}
