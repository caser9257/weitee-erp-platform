package cn.weitee.erp.module.mes.service;

import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.module.mes.controller.admin.vo.workcalendar.MesWorkCalendarSaveReqVO;
import cn.weitee.erp.module.mes.dal.dataobject.MesWorkCalendarDO;
import cn.weitee.erp.module.mes.dal.mysql.MesWorkCalendarMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MesWorkCalendarServiceImplTest {

    private final AtomicReference<MesWorkCalendarDO> insertedRef = new AtomicReference<>();
    private final AtomicReference<MesWorkCalendarDO> calendarRef = new AtomicReference<>();

    private MesWorkCalendarServiceImpl calendarService;

    @BeforeEach
    void setUp() throws Exception {
        calendarService = new MesWorkCalendarServiceImpl();
        insertedRef.set(null);
        calendarRef.set(null);
        setField(calendarService, "mesWorkCalendarMapper", createProxy(MesWorkCalendarMapper.class, (m, a) -> {
            if ("insert".equals(m)) {
                insertedRef.set((MesWorkCalendarDO) a[0]);
                return 1;
            }
            if ("selectById".equals(m)) return calendarRef.get();
            if ("updateById".equals(m)) return 1;
            if ("deleteById".equals(m)) return 1;
            return null;
        }));
    }

    private MesWorkCalendarSaveReqVO buildReqVO() {
        MesWorkCalendarSaveReqVO reqVO = new MesWorkCalendarSaveReqVO();
        reqVO.setCalendarName("标准白班");
        reqVO.setWeekMask("1111100");
        reqVO.setDailyHours(new BigDecimal("8"));
        return reqVO;
    }

    @Test
    void createCalendar_shouldSetEnableStatus() {
        calendarService.createCalendar(buildReqVO());
        assertEquals(1, insertedRef.get().getStatus());
        assertEquals("标准白班", insertedRef.get().getCalendarName());
    }

    @Test
    void createCalendar_shouldRejectInvalidWeekMask() {
        MesWorkCalendarSaveReqVO reqVO = buildReqVO();
        reqVO.setWeekMask("1111");
        assertThrows(ServiceException.class, () -> calendarService.createCalendar(reqVO));
    }

    @Test
    void createCalendar_shouldRejectDateRangeInvalid() {
        MesWorkCalendarSaveReqVO reqVO = buildReqVO();
        reqVO.setEffectiveDate(LocalDate.of(2026, 8, 20));
        reqVO.setExpireDate(LocalDate.of(2026, 8, 10));
        assertThrows(ServiceException.class, () -> calendarService.createCalendar(reqVO));
    }

    @Test
    void deleteCalendar_shouldRejectWhenMissing() {
        assertThrows(ServiceException.class, () -> calendarService.deleteCalendar(99L));
    }

    @SuppressWarnings("unchecked")
    private <T> T createProxy(Class<T> type, MethodHandler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type},
                (proxy, method, args) -> {
                    if (method.getDeclaringClass() == Object.class) {
                        if ("toString".equals(method.getName())) return type.getSimpleName() + "Proxy";
                        if ("hashCode".equals(method.getName())) return System.identityHashCode(proxy);
                        if ("equals".equals(method.getName())) return proxy == args[0];
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
        Object handle(String methodName, Object[] args);
    }

}
