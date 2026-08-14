package cn.weitee.erp.module.mes.service.scheduling;

import cn.hutool.core.collection.CollUtil;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionOrderDO;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionOrderMapper;
import cn.weitee.erp.module.mes.dal.dataobject.MesWorkTaskDO;
import cn.weitee.erp.module.mes.dal.mysql.MesWorkTaskMapper;
import cn.weitee.erp.module.mes.enums.MesWorkTaskStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 排程引擎（第一版：顺序贪心排程）。
 * <p>
 * 规则：
 * <ol>
 *   <li>排序：priority 升序 → 工单交期（planEndTime）升序 → 工单 id 升序 → stepNo 升序</li>
 *   <li>前序约束：同工单按 stepNo 顺序，后序任务开始 ≥ 前序完成</li>
 *   <li>资源独占：同一工作中心时间窗不重叠（占用表拒绝）</li>
 *   <li>任务时长：min(该工作中心日历每日小时, 8)，第一版简化</li>
 *   <li>无工作中心的任务跳过（保持待排程）</li>
 * </ol>
 */
@Service
public class MesSchedulingService {

    private static final Logger log = LoggerFactory.getLogger(MesSchedulingService.class);
    private static final BigDecimal MAX_TASK_HOURS = new BigDecimal("8");

    @Resource
    private MesWorkTaskMapper mesWorkTaskMapper;
    @Resource
    private ErpProductionOrderMapper erpProductionOrderMapper;
    @Resource
    private MesCalendarResolver calendarResolver;

    /**
     * 对指定工单的待排程任务执行自动排程，返回排程成功数量。
     */
    @Transactional(rollbackFor = Exception.class)
    public int scheduleByOrder(Long productionOrderId) {
        List<MesWorkTaskDO> tasks = mesWorkTaskMapper.selectListByOrderId(productionOrderId);
        List<MesWorkTaskDO> waitTasks = tasks.stream()
                .filter(t -> MesWorkTaskStatusEnum.WAIT_SCHEDULE.getStatus().equals(t.getStatus()))
                .toList();
        if (CollUtil.isEmpty(waitTasks)) {
            return 0;
        }
        return doSchedule(waitTasks);
    }

    /**
     * 对全部待排程任务执行自动排程，返回排程成功数量。
     */
    @Transactional(rollbackFor = Exception.class)
    public int scheduleAll() {
        List<MesWorkTaskDO> waitTasks = mesWorkTaskMapper.selectList(new cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX<MesWorkTaskDO>()
                .eq(MesWorkTaskDO::getStatus, MesWorkTaskStatusEnum.WAIT_SCHEDULE.getStatus())
                .orderByAsc(MesWorkTaskDO::getId));
        if (CollUtil.isEmpty(waitTasks)) {
            return 0;
        }
        return doSchedule(waitTasks);
    }

    /**
     * 清空工单已排程任务的计划时间并重新排程（重排入口）。
     */
    @Transactional(rollbackFor = Exception.class)
    public int clearAndReschedule(Long productionOrderId) {
        mesWorkTaskMapper.clearPlanTimeByOrderId(productionOrderId);
        return scheduleByOrder(productionOrderId);
    }

    /**
     * 核心排程：排序 → 逐任务搜索可用窗口。
     *
     * @param tasks 待排程任务
     * @return 排程成功数量
     */
    private int doSchedule(List<MesWorkTaskDO> tasks) {
        Map<Long, LocalDateTime> orderDueMap = loadOrderDueMap(tasks);
        List<MesWorkTaskDO> ordered = new ArrayList<>(tasks);
        ordered.sort(Comparator
                .comparing((MesWorkTaskDO t) -> t.getPriority() == null ? 0 : t.getPriority())
                .thenComparing(t -> orderDueMap.getOrDefault(t.getProductionOrderId(), LocalDateTime.MAX))
                .thenComparing(MesWorkTaskDO::getProductionOrderId)
                .thenComparing(t -> t.getStepNo() == null ? Integer.MAX_VALUE : t.getStepNo()));

        // 2. 资源占用表：数据库全部已排程任务 + 本次循环内追加
        ResourceOccupancy occupancy = new ResourceOccupancy();
        mesWorkTaskMapper.selectList(new cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX<MesWorkTaskDO>()
                        .eq(MesWorkTaskDO::getStatus, MesWorkTaskStatusEnum.SCHEDULED.getStatus())
                        .isNotNull(MesWorkTaskDO::getPlanStartTime)
                        .isNotNull(MesWorkTaskDO::getPlanEndTime))
                .forEach(t -> occupancy.add(t.getWorkCenterId(), t.getPlanStartTime(), t.getPlanEndTime()));
        // 同工单前序完成时间
        Map<Long, LocalDateTime> orderPrevEnd = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();
        int scheduled = 0;
        for (MesWorkTaskDO task : ordered) {
            if (task.getWorkCenterId() == null) {
                log.info("[doSchedule][任务 {} 无工作中心，跳过]", task.getTaskNo());
                continue;
            }
            LocalDateTime earliest = orderPrevEnd.getOrDefault(task.getProductionOrderId(), now);
            long durationMinutes = taskHoursOf(task.getWorkCenterId()) * 60;
            Optional<MesCalendarResolver.TimeWindow> window = findAvailableWindow(
                    task.getWorkCenterId(), earliest, durationMinutes, occupancy);
            if (window.isEmpty()) {
                log.warn("[doSchedule][任务 {} 未找到可用窗口]", task.getTaskNo());
                continue;
            }
            MesCalendarResolver.TimeWindow tw = window.get();
            mesWorkTaskMapper.updatePlanTimeByCas(task.getId(), tw.start(), tw.end(), null);
            occupancy.add(task.getWorkCenterId(), tw.start(), tw.end());
            orderPrevEnd.put(task.getProductionOrderId(), tw.end());
            scheduled++;
        }
        log.info("[doSchedule][排程完成，成功 {} / {} ]", scheduled, ordered.size());
        return scheduled;
    }

    /**
     * 搜索最早可用窗口：从 earliestStart 起，按日历推进。
     */
    private Optional<MesCalendarResolver.TimeWindow> findAvailableWindow(Long workCenterId,
                                                                        LocalDateTime earliestStart,
                                                                        long durationMinutes,
                                                                        ResourceOccupancy occupancy) {
        LocalDateTime cursor = earliestStart;
        for (int i = 0; i < 3700; i++) { // 防御上限（约 1 年逐日推进）
            java.time.LocalDate date = cursor.toLocalDate();
            Optional<MesCalendarResolver.TimeWindow> window = calendarResolver.findWorkingWindow(workCenterId, date);
            if (window.isEmpty()) {
                cursor = calendarResolver.nextWorkingDayStart(workCenterId, cursor);
                continue;
            }
            MesCalendarResolver.TimeWindow dayWindow = window.get();
            // cursor 早于当日窗口开始 → 对齐窗口开始
            if (cursor.isBefore(dayWindow.start())) {
                cursor = dayWindow.start();
            }
            // 当日窗口放不下 → 跳下一天
            if (cursor.plusMinutes(durationMinutes).isAfter(dayWindow.end())) {
                cursor = calendarResolver.nextWorkingDayStart(workCenterId, dayWindow.end());
                continue;
            }
            LocalDateTime candidateEnd = cursor.plusMinutes(durationMinutes);
            Optional<ResourceOccupancy.Interval> overlap = occupancy.findOverlap(workCenterId, cursor, candidateEnd);
            if (overlap.isEmpty()) {
                return Optional.of(new MesCalendarResolver.TimeWindow(cursor, candidateEnd));
            }
            // 与占用重叠 → 跳到占用结束继续
            cursor = overlap.get().end();
        }
        return Optional.empty();
    }

    /**
     * 冲突检测：返回指定工作中心内计划时间窗重叠的任务对列表。
     * 每项包含两个重叠任务 id 与重叠时段。
     */
    public List<ConflictPair> detectConflicts(Long workCenterId) {
        List<MesWorkTaskDO> tasks = mesWorkTaskMapper.selectList(new cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX<MesWorkTaskDO>()
                .eq(MesWorkTaskDO::getStatus, MesWorkTaskStatusEnum.SCHEDULED.getStatus())
                .isNotNull(MesWorkTaskDO::getPlanStartTime)
                .isNotNull(MesWorkTaskDO::getPlanEndTime)
                .eq(MesWorkTaskDO::getWorkCenterId, workCenterId)
                .orderByAsc(MesWorkTaskDO::getPlanStartTime));
        List<ConflictPair> conflicts = new ArrayList<>();
        for (int i = 0; i < tasks.size(); i++) {
            MesWorkTaskDO a = tasks.get(i);
            for (int j = i + 1; j < tasks.size(); j++) {
                MesWorkTaskDO b = tasks.get(j);
                if (!a.getPlanStartTime().isBefore(b.getPlanEndTime())) {
                    break; // b 开始 ≥ a 结束，后续更晚
                }
                if (a.getPlanEndTime().isAfter(b.getPlanStartTime())) {
                    LocalDateTime overlapStart = a.getPlanStartTime().isAfter(b.getPlanStartTime())
                            ? a.getPlanStartTime() : b.getPlanStartTime();
                    LocalDateTime overlapEnd = a.getPlanEndTime().isBefore(b.getPlanEndTime())
                            ? a.getPlanEndTime() : b.getPlanEndTime();
                    conflicts.add(new ConflictPair(a.getId(), b.getId(), overlapStart, overlapEnd));
                }
            }
        }
        return conflicts;
    }

    /** 冲突对。 */
    public record ConflictPair(Long taskIdA, Long taskIdB, LocalDateTime overlapStart, LocalDateTime overlapEnd) {
    }

    /**
     * 加载工单交期（planEndTime），批量查询，缺失排最后。
     */
    private Map<Long, LocalDateTime> loadOrderDueMap(List<MesWorkTaskDO> tasks) {
        Map<Long, LocalDateTime> map = new HashMap<>();
        Set<Long> orderIds = tasks.stream().map(MesWorkTaskDO::getProductionOrderId).collect(Collectors.toSet());
        if (orderIds.isEmpty()) {
            return map;
        }
        List<ErpProductionOrderDO> orders = erpProductionOrderMapper.selectBatchIds(orderIds);
        if (CollUtil.isNotEmpty(orders)) {
            for (ErpProductionOrderDO order : orders) {
                map.put(order.getId(),
                        order.getPlanEndTime() != null ? order.getPlanEndTime() : LocalDateTime.MAX);
            }
        }
        // 查询缺失的工单（如已删除）交期按 MAX 处理
        for (Long orderId : orderIds) {
            map.putIfAbsent(orderId, LocalDateTime.MAX);
        }
        return map;
    }

    /**
     * 任务时长（小时）：min(该工作中心"今日"日历可用小时, 8)，日历缺失取 8。
     * 说明：第一版以今日日历为统一时长基准，不做按排程日的逐日解析（后续可按实际工时细化）。
     */
    private long taskHoursOf(Long workCenterId) {
        Optional<MesCalendarResolver.TimeWindow> window =
                calendarResolver.findWorkingWindow(workCenterId, java.time.LocalDate.now());
        if (window.isEmpty()) {
            return MAX_TASK_HOURS.longValue();
        }
        long minutes = java.time.Duration.between(window.get().start(), window.get().end()).toMinutes();
        long hours = Math.max(minutes / 60, 1);
        return Math.min(hours, MAX_TASK_HOURS.longValue());
    }
}
