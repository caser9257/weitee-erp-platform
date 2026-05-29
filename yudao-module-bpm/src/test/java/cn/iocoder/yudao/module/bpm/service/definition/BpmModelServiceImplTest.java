package cn.iocoder.yudao.module.bpm.service.definition;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BpmModelServiceImplTest {

    @Test
    void validateModelManager_shouldAllowSuperAdminEvenIfNotInManagerList() throws Exception {
        Object service = instantiateService();
        Object model = createProxy(Class.forName("org.flowable.engine.repository.Model"), (methodName, args) -> {
            if ("getId".equals(methodName)) {
                return "model-1";
            }
            if ("getName".equals(methodName)) {
                return "ERP Purchase Order Approval";
            }
            if ("getMetaInfo".equals(methodName)) {
                return "{\"managerUserIds\":[200],\"sort\":1}";
            }
            if ("getCreateTime".equals(methodName)) {
                return new Date();
            }
            return null;
        });

        setField(service, "repositoryService", createProxy(
                Class.forName("org.flowable.engine.RepositoryService"),
                (methodName, args) -> "getModel".equals(methodName) ? model : null));
        setField(service, "permissionApi", createProxy(
                Class.forName("cn.iocoder.yudao.module.system.api.permission.PermissionApi"),
                (methodName, args) -> {
                    if ("hasAnyRoles".equals(methodName)) {
                        return true;
                    }
                    return null;
                }));

        Method method = service.getClass().getDeclaredMethod("validateModelManager", String.class, Long.class);
        method.setAccessible(true);

        Object result = method.invoke(service, "model-1", 1L);

        assertNotNull(result);
        assertEquals("model-1", invokeGetter(result, "getId"));
    }

    @Test
    void saveModel_shouldMarkModelAsHavingUnpublishedChanges() throws Exception {
        Object service = instantiateService();
        final String[] metaInfoHolder = {"{\"managerUserIds\":[1],\"sort\":1,\"hasUnpublishedChanges\":false}"};
        Object model = createProxy(Class.forName("org.flowable.engine.repository.Model"), (methodName, args) -> {
            if ("getId".equals(methodName)) {
                return "model-2";
            }
            if ("getMetaInfo".equals(methodName)) {
                return metaInfoHolder[0];
            }
            if ("setMetaInfo".equals(methodName)) {
                metaInfoHolder[0] = (String) args[0];
                return null;
            }
            if ("getCreateTime".equals(methodName)) {
                return new Date();
            }
            return null;
        });
        setField(service, "repositoryService", createProxy(
                Class.forName("org.flowable.engine.RepositoryService"),
                (methodName, args) -> null));

        Class<?> reqClass = Class.forName("cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.BpmModelSaveReqVO");
        Object reqVO = reqClass.getDeclaredConstructor().newInstance();
        reqClass.getMethod("setType", Integer.class).invoke(reqVO, 10);

        Method method = service.getClass().getDeclaredMethod("saveModel",
                Class.forName("org.flowable.engine.repository.Model"), reqClass);
        method.setAccessible(true);
        method.invoke(service, model, reqVO);

        assertTrue(metaInfoHolder[0].contains("\"hasUnpublishedChanges\":true"));
    }

    private Object instantiateService() throws Exception {
        Class<?> clazz = Class.forName("cn.iocoder.yudao.module.bpm.service.definition.BpmModelServiceImpl");
        return clazz.getDeclaredConstructor().newInstance();
    }

    private Object invokeGetter(Object target, String methodName) throws Exception {
        return target.getClass().getMethod(methodName).invoke(target);
    }

    @SuppressWarnings("unchecked")
    private <T> T createProxy(Class<T> type, MethodHandler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type},
                (proxy, method, args) -> {
                    if (method.getDeclaringClass() == Object.class) {
                        if ("toString".equals(method.getName())) {
                            return type.getSimpleName() + "Proxy";
                        }
                        if ("hashCode".equals(method.getName())) {
                            return System.identityHashCode(proxy);
                        }
                        if ("equals".equals(method.getName())) {
                            return proxy == args[0];
                        }
                    }
                    return handler.handle(method.getName(), args);
                });
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @FunctionalInterface
    private interface MethodHandler {
        Object handle(String methodName, Object[] args) throws Throwable;
    }

}
