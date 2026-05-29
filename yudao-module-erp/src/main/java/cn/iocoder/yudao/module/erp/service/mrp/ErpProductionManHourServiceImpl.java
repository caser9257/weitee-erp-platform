package cn.iocoder.yudao.module.erp.service.mrp;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost.ErpProductionManHourProjectSummaryReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost.ErpProductionManHourProjectSummaryRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost.ErpProductionManHourPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost.ErpProductionManHourSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionCostEntryDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionManHourDO;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpProductionCostAllocationMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpProductionCostEntryMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpProductionManHourMapper;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionOrderDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.project.ErpProjectDO;
import cn.iocoder.yudao.module.erp.service.project.ErpProjectService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PRODUCTION_ORDER_NOT_EXISTS;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstantsMrpExt.PRODUCTION_ACCOUNTING_MONTH_INVALID;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstantsMrpExt.PRODUCTION_MAN_HOUR_INVALID;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstantsMrpExt.PRODUCTION_MAN_HOUR_MONTH_LOCKED;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstantsMrpExt.PRODUCTION_MAN_HOUR_NOT_EXISTS;

@Service
@Validated
public class ErpProductionManHourServiceImpl implements ErpProductionManHourService {

    @Resource
    private ErpProductionManHourMapper erpProductionManHourMapper;
    @Resource
    private ErpProductionCostEntryMapper erpProductionCostEntryMapper;
    @Resource
    private ErpProductionOrderService productionOrderService;
    @Resource
    private ErpProjectService projectService;
    @Resource
    private ErpProductionCostAllocationMapper erpProductionCostAllocationMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createProductionManHour(ErpProductionManHourSaveReqVO createReqVO) {
        validateEditableMonth(createReqVO.getAccountingMonth());
        validateSaveReq(createReqVO.getProductionOrderId(), createReqVO.getAccountingMonth(),
                createReqVO.getWorkDate(), createReqVO.getManHour());
        ErpProductionManHourDO manHour = BeanUtils.toBean(createReqVO, ErpProductionManHourDO.class);
        erpProductionManHourMapper.insert(manHour);
        return manHour.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProductionManHour(ErpProductionManHourSaveReqVO updateReqVO) {
        validateProductionManHourExists(updateReqVO.getId());
        validateEditableMonth(updateReqVO.getAccountingMonth());
        validateSaveReq(updateReqVO.getProductionOrderId(), updateReqVO.getAccountingMonth(),
                updateReqVO.getWorkDate(), updateReqVO.getManHour());
        erpProductionManHourMapper.updateById(BeanUtils.toBean(updateReqVO, ErpProductionManHourDO.class));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProductionManHour(Long id) {
        ErpProductionManHourDO manHour = validateProductionManHourExists(id);
        validateEditableMonth(manHour.getAccountingMonth());
        erpProductionManHourMapper.deleteById(id);
    }

    @Override
    public ErpProductionManHourDO getProductionManHour(Long id) {
        return erpProductionManHourMapper.selectById(id);
    }

    @Override
    public PageResult<ErpProductionManHourDO> getProductionManHourPage(ErpProductionManHourPageReqVO pageReqVO) {
        return erpProductionManHourMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ErpProductionManHourProjectSummaryRespVO> getProductionManHourProjectSummaryList(
            ErpProductionManHourProjectSummaryReqVO reqVO) {
        validateAccountingMonthFormat(reqVO.getAccountingMonth());
        List<ErpProductionManHourDO> manHours = erpProductionManHourMapper.selectListByAccountingMonth(reqVO.getAccountingMonth());
        if (CollUtil.isEmpty(manHours)) {
            return List.of();
        }

        Map<Long, BigDecimal> manHourMap = summarizeManHourByOrder(manHours);
        if (manHourMap.isEmpty()) {
            return List.of();
        }

        List<Long> orderIds = new ArrayList<>(manHourMap.keySet());
        Map<Long, ErpProductionOrderDO> orderMap = productionOrderService.getProductionOrderList(orderIds).stream()
                .collect(java.util.stream.Collectors.toMap(ErpProductionOrderDO::getId, item -> item, (left, right) -> left,
                        LinkedHashMap::new));
        if (orderMap.isEmpty()) {
            return List.of();
        }

        Map<Long, BigDecimal> laborCostMap = summarizeLaborCostByOrder(
                erpProductionCostEntryMapper.selectListByAccountingMonth(reqVO.getAccountingMonth()));
        Map<Long, LocalDate> lastWorkDateMap = summarizeLastWorkDateByOrder(manHours);
        Set<Long> projectIds = orderMap.values().stream()
                .map(ErpProductionOrderDO::getProjectId)
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        Map<Long, ErpProjectDO> projectMap = projectIds.isEmpty() ? Map.of() : projectService.getProjectMap(projectIds);

        Map<Long, ErpProductionManHourProjectSummaryRespVO> summaryMap = new LinkedHashMap<>();
        for (Map.Entry<Long, BigDecimal> entry : manHourMap.entrySet()) {
            ErpProductionOrderDO order = orderMap.get(entry.getKey());
            if (order == null) {
                continue;
            }
            if (reqVO.getProjectId() != null && !Objects.equals(order.getProjectId(), reqVO.getProjectId())) {
                continue;
            }
            Long projectId = order.getProjectId();
            ErpProductionManHourProjectSummaryRespVO summary = summaryMap.computeIfAbsent(projectId, key -> {
                ErpProductionManHourProjectSummaryRespVO vo = new ErpProductionManHourProjectSummaryRespVO();
                vo.setAccountingMonth(reqVO.getAccountingMonth());
                vo.setProjectId(key);
                vo.setProductionOrderCount(0);
                vo.setManHour(BigDecimal.ZERO.setScale(6, RoundingMode.HALF_UP));
                vo.setLaborCost(BigDecimal.ZERO.setScale(6, RoundingMode.HALF_UP));
                MapUtils.findAndThen(projectMap, key, project -> {
                    vo.setProjectNo(project.getNo());
                    vo.setProjectName(project.getName());
                });
                return vo;
            });
            summary.setProductionOrderCount(summary.getProductionOrderCount() + 1);
            summary.setManHour(addAmount(summary.getManHour(), entry.getValue()));
            summary.setLaborCost(addAmount(summary.getLaborCost(), laborCostMap.get(entry.getKey())));
            summary.setLastWorkDate(maxDate(summary.getLastWorkDate(), lastWorkDateMap.get(entry.getKey())));
        }

        List<ErpProductionManHourProjectSummaryRespVO> summaryList = new ArrayList<>(summaryMap.values());
        summaryList.sort(Comparator.comparing(ErpProductionManHourProjectSummaryRespVO::getProjectId,
                Comparator.nullsLast(Comparator.naturalOrder())));
        summaryList.forEach(item -> item.setLaborUnitCost(
                item.getManHour() != null && item.getManHour().compareTo(BigDecimal.ZERO) > 0
                        ? item.getLaborCost().divide(item.getManHour(), 6, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO.setScale(6, RoundingMode.HALF_UP)));
        return summaryList;
    }

    private void validateSaveReq(Long productionOrderId, String accountingMonth, LocalDate workDate, BigDecimal manHour) {
        if (productionOrderService.getProductionOrder(productionOrderId) == null) {
            throw exception(PRODUCTION_ORDER_NOT_EXISTS);
        }
        if (manHour == null || manHour.compareTo(BigDecimal.ZERO) <= 0) {
            throw exception(PRODUCTION_MAN_HOUR_INVALID);
        }
        validateAccountingMonth(accountingMonth, workDate);
    }

    private void validateEditableMonth(String accountingMonth) {
        if (erpProductionCostAllocationMapper.selectCountExecutedByAccountingMonth(accountingMonth) > 0) {
            throw exception(PRODUCTION_MAN_HOUR_MONTH_LOCKED);
        }
    }

    private void validateAccountingMonth(String accountingMonth, LocalDate workDate) {
        try {
            YearMonth yearMonth = YearMonth.parse(accountingMonth);
            if (workDate == null || !yearMonth.equals(YearMonth.from(workDate))) {
                throw exception(PRODUCTION_ACCOUNTING_MONTH_INVALID);
            }
        } catch (DateTimeParseException ex) {
            throw exception(PRODUCTION_ACCOUNTING_MONTH_INVALID);
        }
    }

    private ErpProductionManHourDO validateProductionManHourExists(Long id) {
        ErpProductionManHourDO manHour = erpProductionManHourMapper.selectById(id);
        if (manHour == null) {
            throw exception(PRODUCTION_MAN_HOUR_NOT_EXISTS);
        }
        return manHour;
    }

    private void validateAccountingMonthFormat(String accountingMonth) {
        try {
            YearMonth.parse(accountingMonth);
        } catch (DateTimeParseException ex) {
            throw exception(PRODUCTION_ACCOUNTING_MONTH_INVALID);
        }
    }

    private Map<Long, BigDecimal> summarizeManHourByOrder(List<ErpProductionManHourDO> manHours) {
        Map<Long, BigDecimal> manHourMap = new LinkedHashMap<>();
        for (ErpProductionManHourDO manHour : manHours) {
            if (manHour.getProductionOrderId() == null || manHour.getManHour() == null
                    || manHour.getManHour().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            manHourMap.merge(manHour.getProductionOrderId(), manHour.getManHour(), BigDecimal::add);
        }
        return manHourMap;
    }

    private Map<Long, BigDecimal> summarizeLaborCostByOrder(List<ErpProductionCostEntryDO> entries) {
        Map<Long, BigDecimal> laborCostMap = new LinkedHashMap<>();
        if (CollUtil.isEmpty(entries)) {
            return laborCostMap;
        }
        for (ErpProductionCostEntryDO entry : entries) {
            if (!Objects.equals(entry.getCostType(), cn.iocoder.yudao.module.erp.enums.mrp.ErpProductionCostTypeEnum.LABOR.getType())
                    || entry.getProductionOrderId() == null) {
                continue;
            }
            laborCostMap.merge(entry.getProductionOrderId(),
                    entry.getAmount() == null ? BigDecimal.ZERO : entry.getAmount(), BigDecimal::add);
        }
        return laborCostMap;
    }

    private Map<Long, LocalDate> summarizeLastWorkDateByOrder(List<ErpProductionManHourDO> manHours) {
        Map<Long, LocalDate> lastWorkDateMap = new LinkedHashMap<>();
        for (ErpProductionManHourDO manHour : manHours) {
            if (manHour.getProductionOrderId() == null || manHour.getWorkDate() == null) {
                continue;
            }
            lastWorkDateMap.merge(manHour.getProductionOrderId(), manHour.getWorkDate(),
                    (left, right) -> left.isAfter(right) ? left : right);
        }
        return lastWorkDateMap;
    }

    private BigDecimal addAmount(BigDecimal left, BigDecimal right) {
        BigDecimal normalizedLeft = left == null ? BigDecimal.ZERO.setScale(6, RoundingMode.HALF_UP) : left.setScale(6, RoundingMode.HALF_UP);
        BigDecimal normalizedRight = right == null ? BigDecimal.ZERO.setScale(6, RoundingMode.HALF_UP) : right.setScale(6, RoundingMode.HALF_UP);
        return normalizedLeft.add(normalizedRight).setScale(6, RoundingMode.HALF_UP);
    }

    private LocalDate maxDate(LocalDate left, LocalDate right) {
        if (left == null) {
            return right;
        }
        if (right == null) {
            return left;
        }
        return left.isAfter(right) ? left : right;
    }

}
