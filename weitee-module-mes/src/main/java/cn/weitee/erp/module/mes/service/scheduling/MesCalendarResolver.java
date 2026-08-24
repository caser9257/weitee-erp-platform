package cn.weitee.erp.module.mes.service.scheduling;

import cn.hutool.core.collection.CollUtil;
import cn.weitee.erp.module.mes.dal.dataobject.MesWorkCalendarDO;
import cn.weitee.erp.module.mes.dal.mysql.MesWorkCalendarMapper;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * 工作日历解析器：把日历配置（周几开工 + 每日小时）解析为可用时段。
 * <p>
 * 规则：
 * <ul>
 *   <li>优先匹配工作中心专属日历，其次全局默认日历，都没有则兜底（周一~周六，每日 8 小时）</li>
 *   <li>第一版简化：每天为连续可用段 [08:00, 08:00 + dailyHours]</li>
 * </ul>
 */
@Component
public class MesCalendarResolver {

    public static final LocalTime DEFAULT_DAY_START = LocalTime.of(8, 0);
    public static final BigDecimal DEFAULT_DAILY_HOURS = new BigDecimal("8");
    public static final String DEFAULT_WEEK_MASK = "1111100"; // 周一~周五

    @Resource
    private MesWorkCalendarMapper mesWorkCalendarMapper;

    /**
     * 返回某天该工作中心的可用窗口 [start, end]，当日不开工返回 empty。
     */
    public Optional<TimeWindow> findWorkingWindow(Long workCenterId, LocalDate date) {
        MesWorkCalendarDO calendar = resolveCalendar(workCenterId);
        if (calendar == null) {
            // 兜底默认：周一~周六 8 小时
            return defaultWindow(date);
        }
        if (!isWorkingDay(calendar.getWeekMask(), date)) {
            return Optional.empty();
        }
        if (calendar.getEffectiveDate() != null && date.isBefore(calendar.getEffectiveDate())) {
            return Optional.empty();
        }
        if (calendar.getExpireDate() != null && date.isAfter(calendar.getExpireDate())) {
            return Optional.empty();
        }
        BigDecimal dailyHours = calendar.getDailyHours() == null ? DEFAULT_DAILY_HOURS : calendar.getDailyHours();
        LocalDateTime start = LocalDateTime.of(date, DEFAULT_DAY_START);
        LocalDateTime end = start.plusMinutes((long) (dailyHours.doubleValue() * 60));
        return Optional.of(new TimeWindow(start, end));
    }

    /**
     * 返回严格晚于 from 的下一个开工日的 08:00（当天已排除，避免窗口放不下时回退当天）。
     */
    public LocalDateTime nextWorkingDayStart(Long workCenterId, LocalDateTime from) {
        LocalDate date = from.toLocalDate();
        for (int i = 1; i <= 370; i++) { // 上限一年，防御死循环
            LocalDate candidate = date.plusDays(i);
            Optional<TimeWindow> window = findWorkingWindow(workCenterId, candidate);
            if (window.isPresent()) {
                return window.get().start();
            }
        }
        // 理论上不会走到：一年内必然有开工日（兜底日历周一~周六）
        return LocalDateTime.of(date.plusDays(1), DEFAULT_DAY_START);
    }

    /**
     * 解析工作中心日历：精确匹配优先，其次全局默认，最后兜底。
     */
    private MesWorkCalendarDO resolveCalendar(Long workCenterId) {
        List<MesWorkCalendarDO> calendars = mesWorkCalendarMapper.selectEnabledList(workCenterId);
        if (CollUtil.isEmpty(calendars)) {
            return null;
        }
        // 精确匹配优先
        if (workCenterId != null) {
            Optional<MesWorkCalendarDO> matched = calendars.stream()
                    .filter(c -> workCenterId.equals(c.getWorkCenterId()))
                    .findFirst();
            if (matched.isPresent()) {
                return matched.get();
            }
        }
        return calendars.get(0);
    }

    private Optional<TimeWindow> defaultWindow(LocalDate date) {
        if (!isWorkingDay(DEFAULT_WEEK_MASK, date)) {
            return Optional.empty();
        }
        LocalDateTime start = LocalDateTime.of(date, DEFAULT_DAY_START);
        LocalDateTime end = start.plusMinutes((long) (DEFAULT_DAILY_HOURS.doubleValue() * 60));
        return Optional.of(new TimeWindow(start, end));
    }

    private boolean isWorkingDay(String weekMask, LocalDate date) {
        if (weekMask == null || weekMask.length() != 7) {
            weekMask = DEFAULT_WEEK_MASK;
        }
        int idx = date.getDayOfWeek().getValue() - 1; // 周一=0
        return weekMask.charAt(idx) == '1';
    }

    /** 可用时间窗。 */
    public record TimeWindow(LocalDateTime start, LocalDateTime end) {
    }
}
