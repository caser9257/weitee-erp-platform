package cn.iocoder.yudao.module.bpm.convert.definition;

import org.flowable.engine.repository.Model;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BpmModelConvertTest {

    @Test
    void parseMetaInfo_shouldFallbackHasUnpublishedChangesToFalseWhenModelAlreadyPublished() {
        Model model = createModelProxy("{\"managerUserIds\":[1],\"sort\":1}", "deploy-1");

        boolean result = Boolean.TRUE.equals(BpmModelConvert.INSTANCE.parseMetaInfo(model).getHasUnpublishedChanges());

        assertFalse(result);
    }

    @Test
    void parseMetaInfo_shouldFallbackHasUnpublishedChangesToTrueWhenModelNeverPublished() {
        Model model = createModelProxy("{\"managerUserIds\":[1],\"sort\":1}", null);

        boolean result = Boolean.TRUE.equals(BpmModelConvert.INSTANCE.parseMetaInfo(model).getHasUnpublishedChanges());

        assertTrue(result);
    }

    @Test
    void parseMetaInfo_shouldPreserveNotificationPolicySetting() throws Exception {
        Model model = createModelProxy("{\"managerUserIds\":[1],\"sort\":1,"
                + "\"notificationPolicySetting\":{\"enable\":true,\"publishCheckMode\":\"STRICT\","
                + "\"scenes\":[{\"sceneCode\":\"TASK_ASSIGNED\",\"enabled\":true}]}}", null);

        Object metaInfo = BpmModelConvert.INSTANCE.parseMetaInfo(model);
        Method getter = metaInfo.getClass().getMethod("getNotificationPolicySetting");
        Object notificationPolicySetting = getter.invoke(metaInfo);
        assertNotNull(notificationPolicySetting);
        Object scenes = notificationPolicySetting.getClass().getMethod("getScenes").invoke(notificationPolicySetting);

        assertEquals(1, ((java.util.List<?>) scenes).size());
    }

    @SuppressWarnings("unchecked")
    private Model createModelProxy(String metaInfo, String deploymentId) {
        return (Model) Proxy.newProxyInstance(Model.class.getClassLoader(), new Class<?>[]{Model.class},
                (proxy, method, args) -> {
                    if (method.getDeclaringClass() == Object.class) {
                        if ("toString".equals(method.getName())) {
                            return "ModelProxy";
                        }
                        if ("hashCode".equals(method.getName())) {
                            return System.identityHashCode(proxy);
                        }
                        if ("equals".equals(method.getName())) {
                            return proxy == args[0];
                        }
                    }
                    return switch (method.getName()) {
                        case "getMetaInfo" -> metaInfo;
                        case "getDeploymentId" -> deploymentId;
                        case "getCreateTime" -> new Date();
                        default -> null;
                    };
                });
    }

}
