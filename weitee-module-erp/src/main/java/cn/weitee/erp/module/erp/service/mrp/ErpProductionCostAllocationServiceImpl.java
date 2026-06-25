package cn.weitee.erp.module.erp.service.mrp;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.collection.MapUtils;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.cost.ErpProductionCostAllocationDetailRespVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.cost.ErpProductionCostAllocationPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.cost.ErpProductionCostAllocationSaveReqVO;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionCostAllocationDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionCostAllocationResultDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionCostAllocationRuleDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionCostEntryDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionManHourDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionOrderDO;
import cn.weitee.erp.module.erp.dal.dataobject.project.ErpProjectDO;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionCostAllocationMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionCostAllocationResultMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionCostAllocationRuleMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionCostEntryMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionManHourMapper;
import cn.weitee.erp.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.weitee.erp.module.erp.enums.mrp.ErpProductionCostAllocationBasisTypeEnum;
import cn.weitee.erp.module.erp.enums.mrp.ErpProductionCostAllocationStatusEnum;
import cn.weitee.erp.module.erp.enums.mrp.ErpProductionCostSourceTypeEnum;
import cn.weitee.erp.module.erp.enums.mrp.ErpProductionCostTypeEnum;
import cn.weitee.erp.module.erp.service.product.ErpProductService;
import cn.weitee.erp.module.erp.service.project.ErpProjectService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertList;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCTION_COST_AMOUNT_INVALID;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCTION_COST_TYPE_INVALID;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsMrpExt.PRODUCTION_ACCOUNTING_MONTH_INVALID;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsMrpExt.PRODUCTION_COST_ALLOCATION_ALREADY_EXECUTED;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsMrpExt.PRODUCTION_COST_ALLOCATION_BASIS_EMPTY;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsMrpExt.PRODUCTION_COST_ALLOCATION_BASIS_INVALID;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsMrpExt.PRODUCTION_COST_ALLOCATION_DUPLICATED;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsMrpExt.PRODUCTION_COST_ALLOCATION_NOT_EXISTS;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsMrpExt.PRODUCTION_COST_ALLOCATION_RULE_DISABLED;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsMrpExt.PRODUCTION_COST_ALLOCATION_RULE_NOT_EXISTS;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsMrpExt.PRODUCTION_COST_ALLOCATION_RULE_TYPE_MISMATCH;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsMrpExt.PRODUCTION_COST_ALLOCATION_STATUS_INVALID;

@Service
@Validated
public class ErpProductionCostAllocationServiceImpl implements ErpProductionCostAllocationService {

    @Resource
    private ErpProductionCostAllocationMapper erpProductionCostAllocationMapper;
    @Resource
    private ErpProductionCostAllocationRuleMapper erpProductionCostAllocationRuleMapper;
    @Resource
    private ErpProductionCostAllocationResultMapper erpProductionCostAllocationResultMapper;
    @Resource
    private ErpProductionManHourMapper erpProductionManHourMapper;
    @Resource
    private ErpProductionCostEntryMapper erpProductionCostEntryMapper;
    @Resource
    private ErpProductionOrderService productionOrderService;
    @Resource
    private ErpProductService productService;
    @Resource
    private ErpProjectService projectService;
    @Resource
    private ErpNoRedisDAO noRedisDAO;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createProductionCostAllocation(ErpProductionCostAllocationSaveReqVO createReqVO) {
        validateAccountingMonth(createReqVO.getAccountingMonth());
        ErpProductionCostAllocationRuleDO rule = validateCreateOrUpdateReq(createReqVO, null);
        ErpProductionCostAllocationDO allocation = BeanUtils.toBean(createReqVO, ErpProductionCostAllocationDO.class);
        allocation.setAllocationNo(noRedisDAO.generate(ErpNoRedisDAO.PRODUCTION_COST_ALLOCATION_NO_PREFIX));
        allocation.setStatus(ErpProductionCostAllocationStatusEnum.DRAFT.getStatus());
        allocation.setRuleId(rule.getId());
        erpProductionCostAllocationMapper.insert(allocation);
        return allocation.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProductionCostAllocation(ErpProductionCostAllocationSaveReqVO updateReqVO) {
        ErpProductionCostAllocationDO existed = validateProductionCostAllocationExists(updateReqVO.getId());
        validateDraft(existed);
        validateAccountingMonth(updateReqVO.getAccountingMonth());
        ErpProductionCostAllocationRuleDO rule = validateCreateOrUpdateReq(updateReqVO, existed.getId());
        ErpProductionCostAllocationDO updateObj = BeanUtils.toBean(updateReqVO, ErpProductionCostAllocationDO.class);
        updateObj.setAllocationNo(existed.getAllocationNo());
        updateObj.setStatus(existed.getStatus());
        updateObj.setExecutedTime(existed.getExecutedTime());
        updateObj.setRuleId(rule.getId());
        erpProductionCostAllocationMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProductionCostAllocation(Long id) {
        ErpProductionCostAllocationDO existed = validateProductionCostAllocationExists(id);
        validateDraft(existed);
        erpProductionCostAllocationMapper.deleteById(id);
    }

    @Override
    public ErpProductionCostAllocationDO getProductionCostAllocation(Long id) {
        return erpProductionCostAllocationMapper.selectById(id);
    }

    @Override
    public PageResult<ErpProductionCostAllocationDO> getProductionCostAllocationPage(ErpProductionCostAllocationPageReqVO pageReqVO) {
        return erpProductionCostAllocationMapper.selectPage(pageReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void executeProductionCostAllocation(Long id) {
        ErpProductionCostAllocationDO allocation = validateProductionCostAllocationExists(id);
        if (ErpProductionCostAllocationStatusEnum.EXECUTED.getStatus().equals(allocation.getStatus())) {
            throw exception(PRODUCTION_COST_ALLOCATION_ALREADY_EXECUTED);
        }
        validateDraft(allocation);
        if (CollUtil.isNotEmpty(erpProductionCostAllocationResultMapper.selectListByAllocationId(id))) {
            throw exception(PRODUCTION_COST_ALLOCATION_ALREADY_EXECUTED);
        }
        ErpProductionCostAllocationRuleDO rule = validateExecutableRule(allocation);
        Map<Long, BigDecimal> basisMap = summarizeBasisMap(allocation.getAccountingMonth(), rule.getBasisType());
        if (basisMap.isEmpty()) {
            throw exception(PRODUCTION_COST_ALLOCATION_BASIS_EMPTY);
        }
        BigDecimal totalBasis = basisMap.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalBasis.compareTo(BigDecimal.ZERO) <= 0) {
            throw exception(PRODUCTION_COST_ALLOCATION_BASIS_EMPTY);
        }

        List<Map.Entry<Long, BigDecimal>> basisEntries = new ArrayList<>(basisMap.entrySet());
        basisEntries.sort(Map.Entry.comparingByKey());
        List<ErpProductionCostAllocationResultDO> resultList = new ArrayList<>();
        BigDecimal allocatedSum = BigDecimal.ZERO.setScale(6, RoundingMode.HALF_UP);
        BigDecimal totalAmount = scaleAmount(allocation.getTotalAmount());
        String generatedRemark = buildGeneratedRemark(allocation, rule);
        for (int i = 0; i < basisEntries.size(); i++) {
            Map.Entry<Long, BigDecimal> entry = basisEntries.get(i);
            BigDecimal basisValue = scaleAmount(entry.getValue());
            BigDecimal basisRatio = basisValue.divide(totalBasis, 6, RoundingMode.HALF_UP);
            BigDecimal allocatedAmount = i == basisEntries.size() - 1
                    ? totalAmount.subtract(allocatedSum).setScale(6, RoundingMode.HALF_UP)
                    : totalAmount.multiply(basisRatio).setScale(6, RoundingMode.HALF_UP);
            allocatedSum = allocatedSum.add(allocatedAmount).setScale(6, RoundingMode.HALF_UP);

            ErpProductionCostEntryDO costEntry = new ErpProductionCostEntryDO()
                    .setProductionOrderId(entry.getKey())
                    .setCostType(allocation.getCostType())
                    .setSourceType(ErpProductionCostSourceTypeEnum.ALLOCATION.getType())
                    .setAccountingMonth(allocation.getAccountingMonth())
                    .setAmount(allocatedAmount)
                    .setRemark(generatedRemark);
            erpProductionCostEntryMapper.insert(costEntry);

            resultList.add(new ErpProductionCostAllocationResultDO()
                    .setAllocationId(allocation.getId())
                    .setProductionOrderId(entry.getKey())
                    .setBasisValue(basisValue)
                    .setBasisRatio(basisRatio)
                    .setAllocatedAmount(allocatedAmount)
                    .setGeneratedCostEntryId(costEntry.getId())
                    .setRemark(generatedRemark));
        }
        erpProductionCostAllocationResultMapper.insertBatch(resultList);
        erpProductionCostAllocationMapper.updateById(new ErpProductionCostAllocationDO()
                .setId(allocation.getId())
                .setStatus(ErpProductionCostAllocationStatusEnum.EXECUTED.getStatus())
                .setExecutedTime(LocalDateTime.now()));
    }

    @Override
    public ErpProductionCostAllocationDetailRespVO getProductionCostAllocationDetail(Long id) {
        ErpProductionCostAllocationDO allocation = validateProductionCostAllocationExists(id);
        ErpProductionCostAllocationRuleDO rule = erpProductionCostAllocationRuleMapper.selectById(allocation.getRuleId());
        List<ErpProductionCostAllocationResultDO> results = erpProductionCostAllocationResultMapper.selectListByAllocationId(id);
        Map<Long, ErpProductionOrderDO> orderMap = CollUtil.isEmpty(results) ? Collections.emptyMap()
                : productionOrderService.getProductionOrderList(filterNullIds(convertSet(results, ErpProductionCostAllocationResultDO::getProductionOrderId))).stream()
                .collect(Collectors.toMap(ErpProductionOrderDO::getId, item -> item));
        Map<Long, ErpProductRespVO> productMap = buildProductMap(orderMap);
        Map<Long, ErpProjectDO> projectMap = buildProjectMap(orderMap);

        ErpProductionCostAllocationDetailRespVO respVO = BeanUtils.toBean(allocation, ErpProductionCostAllocationDetailRespVO.class);
        respVO.setCostTypeName(ErpProductionCostTypeEnum.resolveName(allocation.getCostType()));
        respVO.setStatusName(ErpProductionCostAllocationStatusEnum.resolveName(allocation.getStatus()));
        if (rule != null) {
            respVO.setRuleName(rule.getRuleName());
            respVO.setBasisType(rule.getBasisType());
            respVO.setBasisTypeName(ErpProductionCostAllocationBasisTypeEnum.resolveName(rule.getBasisType()));
        }
        respVO.setResults(convertList(results, result -> {
            ErpProductionCostAllocationDetailRespVO.ResultItem item = BeanUtils.toBean(result,
                    ErpProductionCostAllocationDetailRespVO.ResultItem.class);
            ErpProductionOrderDO order = orderMap.get(result.getProductionOrderId());
            if (order != null) {
                item.setProductionOrderNo(order.getOrderNo());
                item.setProductId(order.getProductId());
                MapUtils.findAndThen(productMap, order.getProductId(), product -> item.setProductName(product.getName()));
                item.setProjectId(order.getProjectId());
                MapUtils.findAndThen(projectMap, order.getProjectId(), project -> {
                    item.setProjectNo(project.getNo());
                    item.setProjectName(project.getName());
                });
                item.setFinishedQty(order.getFinishedQty());
            }
            return item;
        }));
        return respVO;
    }

    private ErpProductionCostAllocationRuleDO validateCreateOrUpdateReq(ErpProductionCostAllocationSaveReqVO reqVO, Long currentId) {
        if (!ErpProductionCostTypeEnum.isAllocatableType(reqVO.getCostType())) {
            throw exception(PRODUCTION_COST_TYPE_INVALID);
        }
        if (reqVO.getTotalAmount() == null || reqVO.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw exception(PRODUCTION_COST_AMOUNT_INVALID);
        }
        ErpProductionCostAllocationRuleDO rule = erpProductionCostAllocationRuleMapper.selectById(reqVO.getRuleId());
        if (rule == null) {
            throw exception(PRODUCTION_COST_ALLOCATION_RULE_NOT_EXISTS);
        }
        if (!Objects.equals(rule.getCostType(), reqVO.getCostType())) {
            throw exception(PRODUCTION_COST_ALLOCATION_RULE_TYPE_MISMATCH);
        }
        if (!ErpProductionCostAllocationBasisTypeEnum.isSupported(rule.getBasisType())) {
            throw exception(PRODUCTION_COST_ALLOCATION_BASIS_INVALID);
        }
        ErpProductionCostAllocationDO duplicated = erpProductionCostAllocationMapper
                .selectByAccountingMonthAndCostType(reqVO.getAccountingMonth(), reqVO.getCostType());
        if (duplicated != null && !Objects.equals(duplicated.getId(), currentId)) {
            throw exception(PRODUCTION_COST_ALLOCATION_DUPLICATED);
        }
        return rule;
    }

    private ErpProductionCostAllocationRuleDO validateExecutableRule(ErpProductionCostAllocationDO allocation) {
        ErpProductionCostAllocationRuleDO rule = erpProductionCostAllocationRuleMapper.selectById(allocation.getRuleId());
        if (rule == null) {
            throw exception(PRODUCTION_COST_ALLOCATION_RULE_NOT_EXISTS);
        }
        if (CommonStatusEnum.isDisable(rule.getStatus())) {
            throw exception(PRODUCTION_COST_ALLOCATION_RULE_DISABLED);
        }
        if (!Objects.equals(rule.getCostType(), allocation.getCostType())) {
            throw exception(PRODUCTION_COST_ALLOCATION_RULE_TYPE_MISMATCH);
        }
        if (!ErpProductionCostAllocationBasisTypeEnum.isSupported(rule.getBasisType())) {
            throw exception(PRODUCTION_COST_ALLOCATION_BASIS_INVALID);
        }
        return rule;
    }

    private Map<Long, BigDecimal> summarizeBasisMap(String accountingMonth, Integer basisType) {
        List<ErpProductionManHourDO> manHours = erpProductionManHourMapper.selectListByAccountingMonth(accountingMonth);
        if (CollUtil.isEmpty(manHours)) {
            return Collections.emptyMap();
        }
        Map<Long, BigDecimal> manHourMap = summarizeManHourBasis(manHours);
        if (ErpProductionCostAllocationBasisTypeEnum.MAN_HOUR.getType().equals(basisType)) {
            return manHourMap;
        }
        List<Long> orderIds = new ArrayList<>(manHourMap.keySet());
        Map<Long, ErpProductionOrderDO> orderMap = productionOrderService.getProductionOrderList(orderIds).stream()
                .collect(Collectors.toMap(ErpProductionOrderDO::getId, item -> item, (left, right) -> left, LinkedHashMap::new));
        if (orderMap.isEmpty()) {
            return Collections.emptyMap();
        }
        if (ErpProductionCostAllocationBasisTypeEnum.MACHINE_HOUR.getType().equals(basisType)) {
            return summarizeMachineHourBasis(accountingMonth, new ArrayList<>(orderMap.keySet()));
        }
        if (ErpProductionCostAllocationBasisTypeEnum.OUTPUT.getType().equals(basisType)) {
            return summarizeOutputBasis(orderMap.values());
        }
        if (ErpProductionCostAllocationBasisTypeEnum.WEIGHT.getType().equals(basisType)) {
            return summarizeWeightBasis(orderMap.values());
        }
        return Collections.emptyMap();
    }

    private Map<Long, BigDecimal> summarizeManHourBasis(List<ErpProductionManHourDO> manHours) {
        Map<Long, BigDecimal> basisMap = new LinkedHashMap<>();
        for (ErpProductionManHourDO manHour : manHours) {
            if (manHour.getProductionOrderId() == null || manHour.getManHour() == null
                    || manHour.getManHour().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            basisMap.merge(manHour.getProductionOrderId(), manHour.getManHour(), BigDecimal::add);
        }
        return basisMap;
    }

    private Map<Long, BigDecimal> summarizeOutputBasis(Iterable<ErpProductionOrderDO> orders) {
        Map<Long, BigDecimal> basisMap = new LinkedHashMap<>();
        for (ErpProductionOrderDO order : orders) {
            if (order.getId() == null) {
                continue;
            }
            BigDecimal finishedQty = scaleAmount(order.getFinishedQty());
            if (finishedQty.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            basisMap.put(order.getId(), finishedQty);
        }
        return basisMap;
    }

    private Map<Long, BigDecimal> summarizeWeightBasis(Iterable<ErpProductionOrderDO> orders) {
        Set<Long> productIds = new java.util.LinkedHashSet<>();
        for (ErpProductionOrderDO order : orders) {
            if (order.getProductId() != null) {
                productIds.add(order.getProductId());
            }
        }
        Map<Long, ErpProductRespVO> productMap = productIds.isEmpty() ? Collections.emptyMap()
                : productService.getProductVOMap(productIds);
        Map<Long, BigDecimal> basisMap = new LinkedHashMap<>();
        for (ErpProductionOrderDO order : orders) {
            if (order.getId() == null || order.getProductId() == null) {
                continue;
            }
            BigDecimal finishedQty = scaleAmount(order.getFinishedQty());
            ErpProductRespVO product = productMap.get(order.getProductId());
            BigDecimal productWeight = product == null || product.getWeight() == null
                    ? BigDecimal.ZERO.setScale(6, RoundingMode.HALF_UP) : product.getWeight().setScale(6, RoundingMode.HALF_UP);
            BigDecimal weightBasis = finishedQty.multiply(productWeight).setScale(6, RoundingMode.HALF_UP);
            if (weightBasis.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            basisMap.put(order.getId(), weightBasis);
        }
        return basisMap;
    }

    private Map<Long, ErpProductRespVO> buildProductMap(Map<Long, ErpProductionOrderDO> orderMap) {
        List<Long> productIds = filterNullIds(convertSet(orderMap.values(), ErpProductionOrderDO::getProductId));
        return productIds.isEmpty() ? Collections.emptyMap() : productService.getProductVOMap(productIds);
    }

    private Map<Long, ErpProjectDO> buildProjectMap(Map<Long, ErpProductionOrderDO> orderMap) {
        List<Long> projectIds = filterNullIds(convertSet(orderMap.values(), ErpProductionOrderDO::getProjectId));
        return projectIds.isEmpty() ? Collections.emptyMap() : projectService.getProjectMap(projectIds);
    }

    private List<Long> filterNullIds(Collection<Long> ids) {
        List<Long> result = new ArrayList<>(ids);
        result.removeIf(Objects::isNull);
        return result;
    }

    private void validateAccountingMonth(String accountingMonth) {
        try {
            YearMonth.parse(accountingMonth);
        } catch (DateTimeParseException ex) {
            throw exception(PRODUCTION_ACCOUNTING_MONTH_INVALID);
        }
    }

    private void validateDraft(ErpProductionCostAllocationDO allocation) {
        if (!ErpProductionCostAllocationStatusEnum.DRAFT.getStatus().equals(allocation.getStatus())) {
            throw exception(PRODUCTION_COST_ALLOCATION_STATUS_INVALID);
        }
    }

    private ErpProductionCostAllocationDO validateProductionCostAllocationExists(Long id) {
        ErpProductionCostAllocationDO allocation = erpProductionCostAllocationMapper.selectById(id);
        if (allocation == null) {
            throw exception(PRODUCTION_COST_ALLOCATION_NOT_EXISTS);
        }
        return allocation;
    }

    private BigDecimal scaleAmount(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO.setScale(6, RoundingMode.HALF_UP)
                : amount.setScale(6, RoundingMode.HALF_UP);
    }

    private String buildGeneratedRemark(ErpProductionCostAllocationDO allocation, ErpProductionCostAllocationRuleDO rule) {
        if (StrUtil.isNotBlank(allocation.getRemark())) {
            return allocation.getRemark();
        }
        return StrUtil.format("{}-{}", allocation.getAllocationNo(),
                rule == null ? "分摊生成" : rule.getRuleName());
    }

    /**
     * 汇总机器工时分摊基准
     *
     * @param accountingMonth 会计月份
     * @param productionOrderIds 生产工单编号列表
     * @return 分摊基准数据（工单ID -> 工时数值）
     */
    public Map<Long, BigDecimal> summarizeMachineHourBasis(String accountingMonth, List<Long> productionOrderIds) {
        // 查询生产工单的机器工时数据
        if (CollUtil.isEmpty(productionOrderIds)) {
            return Collections.emptyMap();
        }
        List<ErpProductionOrderDO> orders = productionOrderService.getProductionOrderList(productionOrderIds);
        if (CollUtil.isEmpty(orders)) {
            return Collections.emptyMap();
        }
        Map<Long, BigDecimal> basisMap = new LinkedHashMap<>();
        for (ErpProductionOrderDO order : orders) {
            if (order.getId() == null || order.getMachineHour() == null
                    || order.getMachineHour().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            basisMap.put(order.getId(), order.getMachineHour().setScale(6, RoundingMode.HALF_UP));
        }
        return basisMap;
    }

}
