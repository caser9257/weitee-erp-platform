package cn.iocoder.yudao.module.erp.service.mrp;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost.ErpProductionCostDetailRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost.ErpProductionCostEntryPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost.ErpProductionCostEntrySaveReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost.ErpProductionCostProjectSummaryRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost.ErpProductionCostEntryRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost.ErpProductionCostSummaryRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionCostAllocationDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionCostAllocationResultDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionCostEntryDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionIssueDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionManHourDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionOrderDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.project.ErpProjectDO;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpProductionCostAllocationMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpProductionCostAllocationResultMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpProductionCostEntryMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpProductionIssueMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpProductionManHourMapper;
import cn.iocoder.yudao.module.erp.enums.mrp.ErpProductionCostSourceTypeEnum;
import cn.iocoder.yudao.module.erp.enums.mrp.ErpProductionCostTypeEnum;
import cn.iocoder.yudao.module.erp.service.product.ErpProductService;
import cn.iocoder.yudao.module.erp.service.project.ErpProjectService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PRODUCTION_COST_AMOUNT_INVALID;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PRODUCTION_COST_ENTRY_NOT_EXISTS;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PRODUCTION_COST_TYPE_INVALID;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PRODUCTION_ORDER_NOT_EXISTS;
import static cn.iocoder.yudao.module.erp.util.ErpUserIdUtils.parseUserId;

@Service
@Validated
public class ErpProductionCostServiceImpl implements ErpProductionCostService {

    private static final Integer MANUAL_SOURCE_TYPE = ErpProductionCostSourceTypeEnum.MANUAL.getType();

    @Resource
    private ErpProductionCostEntryMapper erpProductionCostEntryMapper;
    @Resource
    private ErpProductionIssueMapper erpProductionIssueMapper;
    @Resource
    private ErpProductionCostAllocationResultMapper erpProductionCostAllocationResultMapper;
    @Resource
    private ErpProductionCostAllocationMapper erpProductionCostAllocationMapper;
    @Resource
    private ErpProductionManHourMapper erpProductionManHourMapper;
    @Resource
    private ErpProductionOrderService productionOrderService;
    @Resource
    private ErpProductService productService;
    @Resource
    private ErpProjectService projectService;
    @Resource
    private AdminUserApi adminUserApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createProductionCostEntry(ErpProductionCostEntrySaveReqVO createReqVO) {
        validateManualEntry(createReqVO.getProductionOrderId(), createReqVO.getCostType(), createReqVO.getAmount());
        ErpProductionCostEntryDO entry = BeanUtils.toBean(createReqVO, ErpProductionCostEntryDO.class);
        entry.setSourceType(MANUAL_SOURCE_TYPE);
        erpProductionCostEntryMapper.insert(entry);
        return entry.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProductionCostEntry(ErpProductionCostEntrySaveReqVO updateReqVO) {
        validateProductionCostEntryExists(updateReqVO.getId());
        validateManualEntry(updateReqVO.getProductionOrderId(), updateReqVO.getCostType(), updateReqVO.getAmount());
        ErpProductionCostEntryDO updateObj = BeanUtils.toBean(updateReqVO, ErpProductionCostEntryDO.class);
        updateObj.setSourceType(MANUAL_SOURCE_TYPE);
        erpProductionCostEntryMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProductionCostEntry(Long id) {
        validateProductionCostEntryExists(id);
        erpProductionCostEntryMapper.deleteById(id);
    }

    @Override
    public ErpProductionCostEntryDO getProductionCostEntry(Long id) {
        return erpProductionCostEntryMapper.selectById(id);
    }

    @Override
    public PageResult<ErpProductionCostEntryDO> getProductionCostEntryPage(ErpProductionCostEntryPageReqVO pageReqVO) {
        return erpProductionCostEntryMapper.selectPage(pageReqVO);
    }

    @Override
    public PageResult<ErpProductionCostSummaryRespVO> getProductionCostSummaryPage(ErpProductionCostEntryPageReqVO pageReqVO) {
        List<ErpProductionCostEntryDO> filteredEntries = erpProductionCostEntryMapper.selectListByPageReqVO(pageReqVO);
        if (filteredEntries.isEmpty()) {
            return PageResult.empty(0L);
        }

        Set<Long> productionOrderIds = filterNotNull(convertSet(filteredEntries, ErpProductionCostEntryDO::getProductionOrderId));
        Map<Long, ErpProductionOrderDO> orderMap = productionOrderService.getProductionOrderList(productionOrderIds).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(ErpProductionOrderDO::getId, item -> item, (left, right) -> left));
        Map<Long, ErpProductRespVO> productMap = buildProductMap(orderMap);
        Map<Long, ErpProjectDO> projectMap = buildProjectMap(orderMap);
        List<Long> creatorIds = new ArrayList<>(convertList(filteredEntries, item -> parseUserId(item.getCreator())));
        creatorIds.removeIf(Objects::isNull);
        Map<Long, AdminUserRespDTO> userMap = creatorIds.isEmpty() ? Collections.emptyMap() : adminUserApi.getUserMap(creatorIds);

        Map<String, ErpProductionCostSummaryRespVO> summaryMap = new LinkedHashMap<>();
        for (ErpProductionCostEntryDO entry : filteredEntries) {
            Long productionOrderId = entry.getProductionOrderId();
            if (productionOrderId == null) {
                continue;
            }
            ErpProductionOrderDO order = orderMap.get(productionOrderId);
            if (order == null) {
                continue;
            }
            String summaryKey = buildSummaryKey(productionOrderId, entry.getAccountingMonth());
            ErpProductionCostSummaryRespVO summary = summaryMap.computeIfAbsent(summaryKey,
                    id -> createSummaryVO(order, productMap, projectMap, entry.getAccountingMonth()));
            summary.setTotalAmount(defaultAmount(summary.getTotalAmount()).add(defaultAmount(entry.getAmount())));
            summary.getBreakdown().add(buildEntryVO(entry, orderMap, productMap, projectMap, userMap));
            summary.setBreakdownCount(summary.getBreakdown().size());
        }

        List<ErpProductionCostSummaryRespVO> summaryList = new ArrayList<>(summaryMap.values());
        long total = summaryList.size();
        int fromIndex = Math.max((pageReqVO.getPageNo() - 1) * pageReqVO.getPageSize(), 0);
        if (fromIndex >= summaryList.size()) {
            return new PageResult<>(Collections.emptyList(), total);
        }
        int toIndex = Math.min(fromIndex + pageReqVO.getPageSize(), summaryList.size());
        return new PageResult<>(summaryList.subList(fromIndex, toIndex), total);
    }

    @Override
    public List<ErpProductionCostEntryDO> getProductionCostEntryListByProductionOrderId(Long productionOrderId) {
        return erpProductionCostEntryMapper.selectListByProductionOrderId(productionOrderId);
    }

    @Override
    public ErpProductionCostDetailRespVO getCostDetail(Long productionOrderId) {
        ErpProductionOrderDO order = productionOrderService.getProductionOrder(productionOrderId);
        if (order == null) {
            throw exception(PRODUCTION_ORDER_NOT_EXISTS);
        }
        List<ErpProductionIssueDO> issueList = erpProductionIssueMapper.selectListByProductionOrderId(productionOrderId);
        List<ErpProductionCostEntryDO> entryList = erpProductionCostEntryMapper.selectListByProductionOrderId(productionOrderId);
        Map<Long, ErpProductionCostAllocationResultDO> allocationResultMap = convertMapByGeneratedCostEntryId(entryList);
        Map<Long, ErpProductionCostAllocationDO> allocationMap = convertAllocationMap(allocationResultMap.values());

        BigDecimal materialCost = sumIssues(issueList);
        BigDecimal laborCost = sumEntries(entryList, ErpProductionCostTypeEnum.LABOR.getType());
        BigDecimal depreciationCost = sumEntries(entryList, ErpProductionCostTypeEnum.DEPRECIATION.getType());
        BigDecimal powerCost = sumEntries(entryList, ErpProductionCostTypeEnum.POWER.getType());
        BigDecimal otherCost = sumEntries(entryList, ErpProductionCostTypeEnum.OTHER.getType());
        BigDecimal totalCost = materialCost.add(laborCost).add(depreciationCost).add(powerCost).add(otherCost);
        BigDecimal finishedQty = ObjectUtil.defaultIfNull(order.getFinishedQty(), BigDecimal.ZERO);
        LocalDateTime costSnapshotTime = calculateCostSnapshotTime(issueList, entryList, allocationResultMap.values(), allocationMap.values());
        Map<Long, ErpProductRespVO> productMap = order.getProductId() == null || productService == null ? Map.of()
                : productService.getProductVOMap(List.of(order.getProductId()));
        Map<Long, ErpProjectDO> projectMap = order.getProjectId() == null || projectService == null ? Map.of()
                : projectService.getProjectMap(List.of(order.getProjectId()));
        ErpProductRespVO product = order.getProductId() == null ? null : productMap.get(order.getProductId());
        ErpProjectDO project = order.getProjectId() == null ? null : projectMap.get(order.getProjectId());

        ErpProductionCostDetailRespVO respVO = new ErpProductionCostDetailRespVO();
        respVO.setProductionOrderId(order.getId());
        respVO.setProductionOrderNo(order.getOrderNo());
        respVO.setProductId(order.getProductId());
        respVO.setProductName(product == null ? null : product.getName());
        respVO.setProjectId(order.getProjectId());
        respVO.setProjectNo(project == null ? null : project.getNo());
        respVO.setProjectName(project == null ? null : project.getName());
        respVO.setFinishedQty(finishedQty);
        respVO.setMaterialCost(materialCost);
        respVO.setLaborCost(laborCost);
        respVO.setDepreciationCost(depreciationCost);
        respVO.setPowerCost(powerCost);
        respVO.setOtherCost(otherCost);
        respVO.setTotalCost(totalCost);
        respVO.setUnitCost(finishedQty.compareTo(BigDecimal.ZERO) > 0
                ? totalCost.divide(finishedQty, 6, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(6, RoundingMode.HALF_UP));
        respVO.setCostSnapshotTime(costSnapshotTime);
        respVO.setMaterialDetails(convertList(issueList,
                issue -> BeanUtils.toBean(issue, ErpProductionCostDetailRespVO.MaterialDetail.class)));
        List<ErpProductionCostDetailRespVO.CostEntry> costEntries = convertList(entryList,
                entry -> buildCostEntryVO(entry, allocationResultMap, allocationMap));
        respVO.setCostEntries(costEntries);
        respVO.setLaborDetails(filterCostEntries(costEntries, ErpProductionCostTypeEnum.LABOR.getType()));
        respVO.setManufacturingCostDetails(filterCostEntries(costEntries,
                ErpProductionCostTypeEnum.DEPRECIATION.getType(),
                ErpProductionCostTypeEnum.POWER.getType(),
                ErpProductionCostTypeEnum.OTHER.getType()));
        return respVO;
    }

    @Override
    public List<ErpProductionCostProjectSummaryRespVO> getProjectSummary(String accountingMonth) {
        List<ErpProductionCostEntryDO> entryList = erpProductionCostEntryMapper.selectListByAccountingMonth(accountingMonth);
        List<ErpProductionManHourDO> manHourList = erpProductionManHourMapper.selectListByAccountingMonth(accountingMonth);
        Set<Long> productionOrderIds = new LinkedHashSet<>();
        productionOrderIds.addAll(filterNotNull(convertSet(entryList, ErpProductionCostEntryDO::getProductionOrderId)));
        productionOrderIds.addAll(filterNotNull(convertSet(manHourList, ErpProductionManHourDO::getProductionOrderId)));
        if (productionOrderIds.isEmpty()) {
            return List.of();
        }
        Map<Long, ErpProductionOrderDO> orderMap = productionOrderService.getProductionOrderList(productionOrderIds).stream()
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toMap(ErpProductionOrderDO::getId, item -> item, (left, right) -> left));
        Set<Long> projectIds = filterNotNull(convertSet(orderMap.values(), ErpProductionOrderDO::getProjectId));
        Map<Long, ErpProjectDO> projectMap = projectService == null || projectIds.isEmpty() ? Map.of()
                : projectService.getProjectMap(projectIds);
        Map<Long, ErpProductionCostProjectSummaryRespVO> summaryMap = new LinkedHashMap<>();
        Map<Long, Set<Long>> productSetMap = new LinkedHashMap<>();
        Map<Long, Set<Long>> orderSetMap = new LinkedHashMap<>();

        for (ErpProductionManHourDO manHour : manHourList) {
            ErpProductionOrderDO order = orderMap.get(manHour.getProductionOrderId());
            if (order == null || order.getProjectId() == null) {
                continue;
            }
            ErpProductionCostProjectSummaryRespVO summary = summaryMap.computeIfAbsent(order.getProjectId(),
                    projectId -> createProjectSummary(projectId, projectMap.get(projectId)));
            summary.setTotalManHour(defaultScaled(summary.getTotalManHour()).add(defaultScaled(manHour.getManHour())));
            orderSetMap.computeIfAbsent(order.getProjectId(), item -> new LinkedHashSet<>()).add(order.getId());
            if (order.getProductId() != null) {
                productSetMap.computeIfAbsent(order.getProjectId(), item -> new LinkedHashSet<>()).add(order.getProductId());
            }
        }
        for (ErpProductionCostEntryDO entry : entryList) {
            ErpProductionOrderDO order = orderMap.get(entry.getProductionOrderId());
            if (order == null || order.getProjectId() == null) {
                continue;
            }
            ErpProductionCostProjectSummaryRespVO summary = summaryMap.computeIfAbsent(order.getProjectId(),
                    projectId -> createProjectSummary(projectId, projectMap.get(projectId)));
            accumulateCost(summary, entry);
            orderSetMap.computeIfAbsent(order.getProjectId(), item -> new LinkedHashSet<>()).add(order.getId());
            if (order.getProductId() != null) {
                productSetMap.computeIfAbsent(order.getProjectId(), item -> new LinkedHashSet<>()).add(order.getProductId());
            }
        }
        List<ErpProductionCostProjectSummaryRespVO> result = new ArrayList<>(summaryMap.values());
        for (ErpProductionCostProjectSummaryRespVO summary : result) {
            summary.setProductionOrderCount(orderSetMap.getOrDefault(summary.getProjectId(), Collections.emptySet()).size());
            summary.setProductCount(productSetMap.getOrDefault(summary.getProjectId(), Collections.emptySet()).size());
            summary.setTotalCost(defaultAmount(summary.getLaborCost())
                    .add(defaultAmount(summary.getDepreciationCost()))
                    .add(defaultAmount(summary.getPowerCost()))
                    .add(defaultAmount(summary.getOtherCost())));
        }
        result.sort(java.util.Comparator.comparing(ErpProductionCostProjectSummaryRespVO::getProjectId));
        return result;
    }

    private void validateManualEntry(Long productionOrderId, Integer costType, BigDecimal amount) {
        if (productionOrderService.getProductionOrder(productionOrderId) == null) {
            throw exception(PRODUCTION_ORDER_NOT_EXISTS);
        }
        if (!ErpProductionCostTypeEnum.isManualType(costType)) {
            throw exception(PRODUCTION_COST_TYPE_INVALID);
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw exception(PRODUCTION_COST_AMOUNT_INVALID);
        }
    }

    private void validateProductionCostEntryExists(Long id) {
        if (erpProductionCostEntryMapper.selectById(id) == null) {
            throw exception(PRODUCTION_COST_ENTRY_NOT_EXISTS);
        }
    }

    private BigDecimal sumIssues(List<ErpProductionIssueDO> issueList) {
        if (issueList == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return issueList.stream()
                .map(item -> ObjectUtil.defaultIfNull(item.getIssueAmount(), BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumEntries(List<ErpProductionCostEntryDO> entryList, Integer costType) {
        if (entryList == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return entryList.stream()
                .filter(item -> costType.equals(item.getCostType()))
                .map(item -> ObjectUtil.defaultIfNull(item.getAmount(), BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private ErpProductionCostProjectSummaryRespVO createProjectSummary(Long projectId, ErpProjectDO project) {
        ErpProductionCostProjectSummaryRespVO summary = new ErpProductionCostProjectSummaryRespVO();
        summary.setProjectId(projectId);
        summary.setProjectNo(project == null ? null : project.getNo());
        summary.setProjectName(project == null ? null : project.getName());
        summary.setProductionOrderCount(0);
        summary.setProductCount(0);
        summary.setTotalManHour(BigDecimal.ZERO.setScale(6, RoundingMode.HALF_UP));
        summary.setLaborCost(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        summary.setDepreciationCost(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        summary.setPowerCost(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        summary.setOtherCost(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        summary.setTotalCost(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        return summary;
    }

    private void accumulateCost(ErpProductionCostProjectSummaryRespVO summary, ErpProductionCostEntryDO entry) {
        BigDecimal amount = defaultAmount(entry.getAmount());
        if (ErpProductionCostTypeEnum.LABOR.getType().equals(entry.getCostType())) {
            summary.setLaborCost(defaultAmount(summary.getLaborCost()).add(amount));
            return;
        }
        if (ErpProductionCostTypeEnum.DEPRECIATION.getType().equals(entry.getCostType())) {
            summary.setDepreciationCost(defaultAmount(summary.getDepreciationCost()).add(amount));
            return;
        }
        if (ErpProductionCostTypeEnum.POWER.getType().equals(entry.getCostType())) {
            summary.setPowerCost(defaultAmount(summary.getPowerCost()).add(amount));
            return;
        }
        if (ErpProductionCostTypeEnum.OTHER.getType().equals(entry.getCostType())) {
            summary.setOtherCost(defaultAmount(summary.getOtherCost()).add(amount));
        }
    }

    private Map<Long, ErpProductRespVO> buildProductMap(Map<Long, ErpProductionOrderDO> orderMap) {
        Set<Long> productIds = filterNotNull(convertSet(orderMap.values(), ErpProductionOrderDO::getProductId));
        if (productIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return productService.getProductVOMap(productIds);
    }

    private Map<Long, ErpProjectDO> buildProjectMap(Map<Long, ErpProductionOrderDO> orderMap) {
        Set<Long> projectIds = filterNotNull(convertSet(orderMap.values(), ErpProductionOrderDO::getProjectId));
        if (projectIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return projectService.getProjectMap(projectIds);
    }

    private ErpProductionCostSummaryRespVO createSummaryVO(ErpProductionOrderDO order,
                                                           Map<Long, ErpProductRespVO> productMap,
                                                           Map<Long, ErpProjectDO> projectMap,
                                                           String accountingMonth) {
        ErpProductionCostSummaryRespVO summary = new ErpProductionCostSummaryRespVO();
        summary.setProductionOrderId(order.getId());
        summary.setProductionOrderNo(order.getOrderNo());
        summary.setProductId(order.getProductId());
        ErpProductRespVO product = order.getProductId() == null ? null : productMap.get(order.getProductId());
        summary.setProductName(product == null ? null : product.getName());
        summary.setProjectId(order.getProjectId());
        ErpProjectDO project = order.getProjectId() == null ? null : projectMap.get(order.getProjectId());
        summary.setProjectNo(project == null ? null : project.getNo());
        summary.setProjectName(project == null ? null : project.getName());
        summary.setAccountingMonth(accountingMonth);
        summary.setTotalAmount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        summary.setBreakdownCount(0);
        summary.setBreakdown(new ArrayList<>());
        return summary;
    }

    private String buildSummaryKey(Long productionOrderId, String accountingMonth) {
        return productionOrderId + "::" + ObjectUtil.defaultIfNull(accountingMonth, "");
    }

    private ErpProductionCostEntryRespVO buildEntryVO(ErpProductionCostEntryDO entry,
                                                      Map<Long, ErpProductionOrderDO> orderMap,
                                                      Map<Long, ErpProductRespVO> productMap,
                                                      Map<Long, ErpProjectDO> projectMap,
                                                      Map<Long, AdminUserRespDTO> userMap) {
        ErpProductionCostEntryRespVO vo = BeanUtils.toBean(entry, ErpProductionCostEntryRespVO.class);
        ErpProductionOrderDO order = orderMap.get(entry.getProductionOrderId());
        if (order != null) {
            vo.setProductionOrderNo(order.getOrderNo());
            vo.setProductId(order.getProductId());
            ErpProductRespVO product = productMap.get(order.getProductId());
            if (product != null) {
                vo.setProductName(product.getName());
            }
            vo.setProjectId(order.getProjectId());
            ErpProjectDO project = projectMap.get(order.getProjectId());
            if (project != null) {
                vo.setProjectNo(project.getNo());
                vo.setProjectName(project.getName());
            }
        }
        AdminUserRespDTO user = userMap.get(parseUserId(entry.getCreator()));
        if (user != null) {
            vo.setCreatorName(user.getNickname());
        }
        vo.setCostTypeName(ErpProductionCostTypeEnum.resolveName(entry.getCostType()));
        vo.setSourceTypeName(ErpProductionCostSourceTypeEnum.resolveName(entry.getSourceType()));
        return vo;
    }

    private Map<Long, ErpProductionCostAllocationResultDO> convertMapByGeneratedCostEntryId(List<ErpProductionCostEntryDO> entryList) {
        if (entryList == null || entryList.isEmpty()) {
            return Map.of();
        }
        List<Long> generatedCostEntryIds = convertList(entryList, ErpProductionCostEntryDO::getId);
        List<ErpProductionCostAllocationResultDO> resultList =
                erpProductionCostAllocationResultMapper.selectListByGeneratedCostEntryIds(generatedCostEntryIds);
        if (resultList.isEmpty()) {
            return Map.of();
        }
        return resultList.stream().filter(item -> item.getGeneratedCostEntryId() != null)
                .collect(java.util.stream.Collectors.toMap(ErpProductionCostAllocationResultDO::getGeneratedCostEntryId,
                        item -> item, (left, right) -> left));
    }

    private Map<Long, ErpProductionCostAllocationDO> convertAllocationMap(Iterable<ErpProductionCostAllocationResultDO> resultList) {
        List<Long> allocationIds = new java.util.ArrayList<>();
        for (ErpProductionCostAllocationResultDO result : resultList) {
            if (result.getAllocationId() != null) {
                allocationIds.add(result.getAllocationId());
            }
        }
        if (allocationIds.isEmpty()) {
            return Map.of();
        }
        List<ErpProductionCostAllocationDO> allocationList = erpProductionCostAllocationMapper.selectByIds(allocationIds);
        if (allocationList.isEmpty()) {
            return Map.of();
        }
        return allocationList.stream().filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toMap(ErpProductionCostAllocationDO::getId, item -> item, (left, right) -> left));
    }

    private LocalDateTime calculateCostSnapshotTime(List<ErpProductionIssueDO> issueList,
                                                    List<ErpProductionCostEntryDO> entryList,
                                                    Iterable<ErpProductionCostAllocationResultDO> resultList,
                                                    Iterable<ErpProductionCostAllocationDO> allocationList) {
        LocalDateTime snapshotTime = null;
        snapshotTime = maxTime(snapshotTime, maxTime(issueList, ErpProductionIssueDO::getCreateTime));
        snapshotTime = maxTime(snapshotTime, maxTime(entryList, ErpProductionCostEntryDO::getCreateTime));
        for (ErpProductionCostAllocationResultDO result : resultList) {
            snapshotTime = maxTime(snapshotTime, result.getCreateTime());
            snapshotTime = maxTime(snapshotTime, result.getUpdateTime());
        }
        for (ErpProductionCostAllocationDO allocation : allocationList) {
            snapshotTime = maxTime(snapshotTime, allocation.getCreateTime());
            snapshotTime = maxTime(snapshotTime, allocation.getExecutedTime());
            snapshotTime = maxTime(snapshotTime, allocation.getUpdateTime());
        }
        return snapshotTime;
    }

    private LocalDateTime maxTime(LocalDateTime left, LocalDateTime right) {
        if (left == null) {
            return right;
        }
        if (right == null) {
            return left;
        }
        return left.isAfter(right) ? left : right;
    }

    private <T> LocalDateTime maxTime(List<T> list, java.util.function.Function<T, LocalDateTime> timeMapper) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        LocalDateTime maxTime = null;
        for (T item : list) {
            maxTime = maxTime(maxTime, timeMapper.apply(item));
        }
        return maxTime;
    }

    private ErpProductionCostDetailRespVO.CostEntry buildCostEntryVO(ErpProductionCostEntryDO entry,
                                                                     Map<Long, ErpProductionCostAllocationResultDO> allocationResultMap,
                                                                     Map<Long, ErpProductionCostAllocationDO> allocationMap) {
        ErpProductionCostDetailRespVO.CostEntry item =
                BeanUtils.toBean(entry, ErpProductionCostDetailRespVO.CostEntry.class);
        item.setCostTypeName(ErpProductionCostTypeEnum.resolveName(entry.getCostType()));
        item.setSourceTypeName(ErpProductionCostSourceTypeEnum.resolveName(entry.getSourceType()));
        ErpProductionCostAllocationResultDO allocationResult = allocationResultMap.get(entry.getId());
        if (allocationResult != null) {
            item.setSourceAllocationResultId(allocationResult.getId());
            item.setSourceAllocationId(allocationResult.getAllocationId());
            ErpProductionCostAllocationDO allocation = allocationMap.get(allocationResult.getAllocationId());
            if (allocation != null) {
                item.setSourceAllocationNo(allocation.getAllocationNo());
            }
        }
        return item;
    }

    private List<ErpProductionCostDetailRespVO.CostEntry> filterCostEntries(List<ErpProductionCostDetailRespVO.CostEntry> costEntries,
                                                                            Integer... costTypes) {
        if (costEntries == null || costEntries.isEmpty()) {
            return List.of();
        }
        return costEntries.stream()
                .filter(item -> matchesCostType(item.getCostType(), costTypes))
                .collect(Collectors.toList());
    }

    private boolean matchesCostType(Integer costType, Integer... costTypes) {
        if (costType == null || costTypes == null) {
            return false;
        }
        for (Integer type : costTypes) {
            if (Objects.equals(costType, type)) {
                return true;
            }
        }
        return false;
    }

    private BigDecimal defaultAmount(BigDecimal amount) {
        return ObjectUtil.defaultIfNull(amount, BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal defaultScaled(BigDecimal amount) {
        return ObjectUtil.defaultIfNull(amount, BigDecimal.ZERO).setScale(6, RoundingMode.HALF_UP);
    }

    private Set<Long> filterNotNull(Set<Long> ids) {
        Set<Long> result = new LinkedHashSet<>();
        if (ids == null) {
            return result;
        }
        for (Long id : ids) {
            if (id != null) {
                result.add(id);
            }
        }
        return result;
    }

}
