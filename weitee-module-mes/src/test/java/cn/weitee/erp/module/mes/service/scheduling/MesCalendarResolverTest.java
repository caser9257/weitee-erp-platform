package cn.weitee.erp.module.mes.service.scheduling;

import cn.weitee.erp.module.mes.dal.mysql.MesWorkCalendarMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MesCalendarResolverTest {

    private MesCalendarResolver resolver;

    @BeforeEach
    void setUp() throws Exception {
        resolver = new MesCalendarResolver();
        // 无日历配置：走兜底默认（周一~周六 8 小时）
        MesWorkCalendarMapper mapper = (MesWorkCalendarMapper) Proxy.newProxyInstance(
                MesWorkCalendarMapper.class.getClassLoader(), new Class<?>[]{MesWorkCalendarMapper.class},
                (proxy, method, args) -> {
                    if (method.getDeclaringClass() == Object.class) {
                        if ("toString".equals(method.getName())) return "mapperProxy";
                        if ("hashCode".equals(method.getName())) return System.identityHashCode(proxy);
                        if ("equals".equals(method.getName())) return proxy == args[0];
                    }
                    return java.util.List.of();
                });
        java.lang.reflect.Field field = MesCalendarResolver.class.getDeclaredField("mesWorkCalendarMapper");
        field.setAccessible(true);
        field.set(resolver, mapper);
    }

    @Test
    void defaultWindow_shouldBeMondayToSaturday() {
        // 周一 2026-08-17 开工，周日 2026-08-23 不开工
        LocalDate monday = LocalDate.of(2026, 8, 17);
        assertTrue(resolver.findWorkingWindow(null, monday).isPresent());
        LocalDate sunday = LocalDate.of(2026, 8, 23);
        assertTrue(resolver.findWorkingWindow(null, sunday).isEmpty());
    }

    @Test
    void defaultWindow_shouldBe8HoursFrom8() {
        LocalDate monday = LocalDate.of(2026, 8, 17);
        MesCalendarResolver.TimeWindow w = resolver.findWorkingWindow(null, monday).orElseThrow();
        assertEquals(LocalDateTime.of(2026, 8, 17, 8, 0), w.start());
        assertEquals(LocalDateTime.of(2026, 8, 17, 16, 0), w.end());
    }

    @Test
    void nextWorkingDayStart_shouldSkipWeekend() {
        // 2026-08-14 是周五；下一个开工日应为周一 2026-08-17
        LocalDateTime friday = LocalDateTime.of(2026, 8, 14, 12, 0);
        LocalDateTime next = resolver.nextWorkingDayStart(null, friday);
        assertEquals(LocalDate.of(2026, 8, 17), next.toLocalDate());
        assertEquals(8, next.getHour());
    }
}
