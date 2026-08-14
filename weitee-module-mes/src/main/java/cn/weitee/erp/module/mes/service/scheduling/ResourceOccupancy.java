package cn.weitee.erp.module.mes.service.scheduling;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

/**
 * 资源占用表：每个工作中心维护按开始时间有序的占用区间。
 * <p>
 * 支持：添加占用、查找与指定区间重叠的占用（冲突检测）、跳过重叠。
 */
public class ResourceOccupancy {

    /** workCenterId -> 有序占用区间（start -> end） */
    private final TreeMap<Long, List<Interval>> centerIntervals = new TreeMap<>();

    public void add(Long workCenterId, LocalDateTime start, LocalDateTime end) {
        centerIntervals.computeIfAbsent(workCenterId, k -> new ArrayList<>())
                .add(new Interval(start, end));
        centerIntervals.get(workCenterId).sort(Comparator.comparing(Interval::start));
    }

    /**
     * 查找与 [start, end] 重叠的第一个占用区间，无则 empty。
     */
    public java.util.Optional<Interval> findOverlap(Long workCenterId, LocalDateTime start, LocalDateTime end) {
        List<Interval> intervals = centerIntervals.get(workCenterId);
        if (intervals == null || intervals.isEmpty()) {
            return java.util.Optional.empty();
        }
        for (Interval interval : intervals) {
            // 区间重叠：interval.start < end && interval.end > start
            if (interval.start().isBefore(end) && interval.end().isAfter(start)) {
                return java.util.Optional.of(interval);
            }
            // 已越过（interval.start >= end），后续都不重叠
            if (!interval.start().isBefore(end)) {
                break;
            }
        }
        return java.util.Optional.empty();
    }

    /**
     * 查找与 [start, end] 重叠的全部占用区间（冲突检测用）。
     */
    public List<Interval> findOverlaps(Long workCenterId, LocalDateTime start, LocalDateTime end) {
        List<Interval> result = new ArrayList<>();
        List<Interval> intervals = centerIntervals.get(workCenterId);
        if (intervals == null || intervals.isEmpty()) {
            return result;
        }
        for (Interval interval : intervals) {
            if (interval.start().isBefore(end) && interval.end().isAfter(start)) {
                result.add(interval);
            } else if (!interval.start().isBefore(end)) {
                break;
            }
        }
        return result;
    }

    /** 占用区间。 */
    public record Interval(LocalDateTime start, LocalDateTime end) {
    }
}
