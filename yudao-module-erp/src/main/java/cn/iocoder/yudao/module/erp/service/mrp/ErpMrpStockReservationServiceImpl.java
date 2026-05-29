package cn.iocoder.yudao.module.erp.service.mrp;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.stockreservation.ErpMrpStockReservationPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.stockreservation.ErpMrpStockReservationProjectSummaryRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.stockreservation.ErpMrpStockReservationRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.stockreservation.ErpMrpStockReservationSummaryPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.stockreservation.ErpMrpStockReservationSummaryRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpMrpPlanDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpMrpStockReservationDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpMrpStockReservationSummaryDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.project.ErpProjectDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOrderDO;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpMrpPlanMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpMrpStockReservationMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpMrpStockReservationSummaryMapper;
import cn.iocoder.yudao.module.erp.service.product.ErpProductService;
import cn.iocoder.yudao.module.erp.service.project.ErpProjectService;
import cn.iocoder.yudao.module.erp.service.sale.ErpSaleOrderService;
import cn.iocoder.yudao.module.erp.service.stock.ErpStockService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Service
@Validated
public class ErpMrpStockReservationServiceImpl implements ErpMrpStockReservationService {

    private static final String UNASSIGNED_PROJECT_NAME = "未关联项目";
    private static final int SUMMARY_PROJECT_DISPLAY_LIMIT = 2;

    @Resource
    private ErpMrpStockReservationMapper erpMrpStockReservationMapper;
    @Resource
    private ErpMrpPlanMapper erpMrpPlanMapper;
    @Resource
    private ErpMrpStockReservationSummaryMapper erpMrpStockReservationSummaryMapper;
    @Resource
    private ErpProjectService projectService;
    @Resource
    private ErpProductService productService;
    @Resource
    private ErpSaleOrderService saleOrderService;
    @Resource
    private ErpStockService stockService;

    @Override
    public PageResult<ErpMrpStockReservationRespVO> getStockReservationPage(ErpMrpStockReservationPageReqVO pageReqVO) {
        PageResult<ErpMrpStockReservationDO> pageResult = erpMrpStockReservationMapper.selectPage(pageReqVO);
        return new PageResult<>(buildRespVOList(pageResult.getList()), pageResult.getTotal());
    }

    @Override
    public PageResult<ErpMrpStockReservationSummaryRespVO> getStockReservationSummaryPage(
            ErpMrpStockReservationSummaryPageReqVO pageReqVO) {
        PageResult<ErpMrpStockReservationSummaryDO> pageResult = erpMrpStockReservationSummaryMapper.selectPage(pageReqVO);
        return new PageResult<>(buildSummaryRespVOList(pageResult.getList()), pageResult.getTotal());
    }

    @Override
    public List<ErpMrpStockReservationProjectSummaryRespVO> getStockReservationProjectSummaryList(Long productId) {
        if (productId == null) {
            return Collections.emptyList();
        }
        List<ErpMrpStockReservationDO> reservations = erpMrpStockReservationMapper.selectActiveListByProductIds(
                Collections.singleton(productId));
        if (CollUtil.isEmpty(reservations)) {
            return Collections.emptyList();
        }
        return buildProjectSummaryList(Collections.singleton(productId), reservations)
                .getOrDefault(productId, Collections.emptyList());
    }

    private List<ErpMrpStockReservationRespVO> buildRespVOList(List<ErpMrpStockReservationDO> list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> planIds = convertSet(list, ErpMrpStockReservationDO::getPlanId);
        Map<Long, ErpMrpPlanDO> planMap = planIds.isEmpty()
                ? Collections.emptyMap()
                : convertMap(erpMrpPlanMapper.selectByIds(planIds), ErpMrpPlanDO::getId);
        Map<Long, ErpProjectDO> projectMap = projectService.getProjectMap(
                convertSet(list, ErpMrpStockReservationDO::getProjectId));
        Map<Long, ErpProductRespVO> productMap = productService.getProductVOMap(
                convertSet(list, ErpMrpStockReservationDO::getProductId));
        Set<Long> saleOrderIds = convertSet(list, ErpMrpStockReservationDO::getSourceOrderId);
        Map<Long, ErpSaleOrderDO> saleOrderMap = saleOrderIds.isEmpty()
                ? Collections.emptyMap()
                : convertMap(saleOrderService.getSaleOrderListByIds(saleOrderIds), ErpSaleOrderDO::getId);
        return BeanUtils.toBean(list, ErpMrpStockReservationRespVO.class, item -> {
            ErpMrpPlanDO plan = planMap.get(item.getPlanId());
            if (plan != null) {
                item.setPlanNo(plan.getPlanNo());
            }
            ErpProjectDO project = projectMap.get(item.getProjectId());
            if (project != null) {
                item.setProjectNo(project.getNo());
                item.setProjectName(project.getName());
            }
            ErpProductRespVO product = productMap.get(item.getProductId());
            if (product != null) {
                item.setProductName(product.getName());
            }
            ErpSaleOrderDO saleOrder = saleOrderMap.get(item.getSourceOrderId());
            if (saleOrder != null) {
                item.setSourceOrderNo(saleOrder.getNo());
            }
        });
    }

    private List<ErpMrpStockReservationSummaryRespVO> buildSummaryRespVOList(List<ErpMrpStockReservationSummaryDO> list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> productIds = convertSet(list, ErpMrpStockReservationSummaryDO::getProductId);
        Map<Long, ErpProductRespVO> productMap = productService.getProductVOMap(productIds);
        Map<Long, BigDecimal> stockQtyMap = stockService.getStockCountMap(productIds);
        List<ErpMrpStockReservationDO> reservations = erpMrpStockReservationMapper.selectActiveListByProductIds(productIds);
        Map<Long, List<ErpMrpStockReservationProjectSummaryRespVO>> projectSummaryMap =
                buildProjectSummaryList(productIds, reservations);
        return BeanUtils.toBean(list, ErpMrpStockReservationSummaryRespVO.class, item -> {
            ErpProductRespVO product = productMap.get(item.getProductId());
            if (product != null) {
                item.setProductName(product.getName());
            }
            BigDecimal stockQty = stockQtyMap.getOrDefault(item.getProductId(), BigDecimal.ZERO);
            BigDecimal activeReservedQty = item.getActiveReservedQty() == null ? BigDecimal.ZERO : item.getActiveReservedQty();
            item.setStockQty(stockQty);
            item.setAvailableQty(stockQty.subtract(activeReservedQty));
            List<ErpMrpStockReservationProjectSummaryRespVO> projectSummaries = projectSummaryMap.getOrDefault(
                    item.getProductId(), Collections.emptyList());
            item.setProjectDistributionItems(projectSummaries.stream()
                    .limit(SUMMARY_PROJECT_DISPLAY_LIMIT)
                    .map(this::formatProjectDistributionItem)
                    .collect(Collectors.toList()));
            item.setProjectDistributionMoreCount(Math.max(projectSummaries.size() - SUMMARY_PROJECT_DISPLAY_LIMIT, 0));
        });
    }

    private Map<Long, List<ErpMrpStockReservationProjectSummaryRespVO>> buildProjectSummaryList(Set<Long> productIds,
                                                                                                 List<ErpMrpStockReservationDO> reservations) {
        if (CollUtil.isEmpty(productIds) || CollUtil.isEmpty(reservations)) {
            return Collections.emptyMap();
        }
        Map<Long, Map<Long, ProjectReservationAggregate>> aggregateMap = new LinkedHashMap<>();
        for (ErpMrpStockReservationDO reservation : reservations) {
            if (reservation.getProductId() == null || !productIds.contains(reservation.getProductId())) {
                continue;
            }
            Map<Long, ProjectReservationAggregate> productAggregateMap = aggregateMap.computeIfAbsent(
                    reservation.getProductId(), key -> new LinkedHashMap<>());
            ProjectReservationAggregate aggregate = productAggregateMap.computeIfAbsent(reservation.getProjectId(),
                    key -> new ProjectReservationAggregate(reservation.getProductId(), reservation.getProjectId()));
            aggregate.merge(reservation);
        }
        Set<Long> projectIds = aggregateMap.values().stream()
                .flatMap(projectMap -> projectMap.values().stream())
                .map(ProjectReservationAggregate::getProjectId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, ErpProjectDO> projectMap = projectService.getProjectMap(projectIds);
        Map<Long, List<ErpMrpStockReservationProjectSummaryRespVO>> result = new LinkedHashMap<>();
        for (Map.Entry<Long, Map<Long, ProjectReservationAggregate>> productEntry : aggregateMap.entrySet()) {
            List<ErpMrpStockReservationProjectSummaryRespVO> projectSummaryList = productEntry.getValue().values().stream()
                    .map(aggregate -> aggregate.toRespVO(projectMap.get(aggregate.getProjectId())))
                    .sorted(Comparator
                            .comparing(ErpMrpStockReservationProjectSummaryRespVO::getActiveReservedQty,
                                    Comparator.nullsLast(Comparator.reverseOrder()))
                            .thenComparing(ErpMrpStockReservationProjectSummaryRespVO::getLastReservedTime,
                                    Comparator.nullsLast(Comparator.reverseOrder())))
                    .collect(Collectors.toList());
            result.put(productEntry.getKey(), projectSummaryList);
        }
        return result;
    }

    private String formatProjectDistributionItem(ErpMrpStockReservationProjectSummaryRespVO item) {
        return item.getProjectName() + " " + formatQty(item.getActiveReservedQty());
    }

    private String formatQty(BigDecimal qty) {
        if (qty == null) {
            return "0.00";
        }
        return qty.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private static class ProjectReservationAggregate {

        private final Long productId;

        private final Long projectId;

        private BigDecimal activeReservedQty = BigDecimal.ZERO;

        private int activeReservationCount = 0;

        private final Set<Long> sourceOrderIds = new java.util.HashSet<>();

        private LocalDateTime lastReservedTime;

        private ProjectReservationAggregate(Long productId, Long projectId) {
            this.productId = productId;
            this.projectId = projectId;
        }

        private void merge(ErpMrpStockReservationDO reservation) {
            if (reservation.getReservedQty() != null) {
                activeReservedQty = activeReservedQty.add(reservation.getReservedQty());
            }
            activeReservationCount++;
            if (reservation.getSourceOrderId() != null) {
                sourceOrderIds.add(reservation.getSourceOrderId());
            }
            if (reservation.getCreateTime() != null
                    && (lastReservedTime == null || reservation.getCreateTime().isAfter(lastReservedTime))) {
                lastReservedTime = reservation.getCreateTime();
            }
        }

        private ErpMrpStockReservationProjectSummaryRespVO toRespVO(ErpProjectDO project) {
            ErpMrpStockReservationProjectSummaryRespVO respVO = new ErpMrpStockReservationProjectSummaryRespVO();
            respVO.setProductId(productId);
            respVO.setProjectId(projectId);
            respVO.setProjectNo(project == null ? null : project.getNo());
            respVO.setProjectName(project == null ? UNASSIGNED_PROJECT_NAME : project.getName());
            respVO.setActiveReservedQty(activeReservedQty);
            respVO.setActiveReservationCount(activeReservationCount);
            respVO.setSourceOrderCount(sourceOrderIds.size());
            respVO.setLastReservedTime(lastReservedTime);
            return respVO;
        }

        private Long getProjectId() {
            return projectId;
        }

    }

}
