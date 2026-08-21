package cn.weitee.erp.module.erp.service.rd;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpBomDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpBomItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpBomItemSubstituteDO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductDO;
import cn.weitee.erp.module.erp.dal.dataobject.rd.ErpRdBomDO;
import cn.weitee.erp.module.erp.dal.dataobject.rd.ErpRdBomItemDO;
import cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomIntegrityIssueRespVO;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpBomItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpBomMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpBomItemSubstituteMapper;
import cn.weitee.erp.module.erp.dal.mysql.rd.ErpRdBomItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.rd.ErpRdBomMapper;
import cn.weitee.erp.module.erp.dal.dataobject.rd.ErpRdBomItemSubstituteDO;
import cn.weitee.erp.module.erp.dal.mysql.rd.ErpRdBomItemSubstituteMapper;
import cn.weitee.erp.module.erp.enums.mrp.ErpBomStatusEnum;
import cn.weitee.erp.module.erp.enums.rd.ErpRdBomStatusEnum;
import cn.weitee.erp.module.erp.enums.rd.RdBomIntegrityIssueType;
import cn.weitee.erp.module.erp.service.product.ErpProductService;
import cn.weitee.erp.util.BomDesignatorUtils;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.BOM_ITEM_EMPTY;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.RD_BOM_BPM_SUBMIT_FAIL;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.RD_BOM_NOT_EXISTS;

@Service
@Validated
@Slf4j
public class ErpRdBomServiceImpl implements ErpRdBomService {

    private static final Pattern RD_BOM_VERSION_PATTERN = Pattern.compile("^[Vv](\\d+)(?:\\.\\d+)?$");

    @Resource
    private ErpRdBomMapper erpRdBomMapper;
    @Resource
    private ErpRdBomItemMapper erpRdBomItemMapper;
    @Resource
    private ErpBomMapper erpBomMapper;
    @Resource
    private ErpBomItemMapper erpBomItemMapper;
    @Resource
    private ErpBomItemSubstituteMapper erpBomItemSubstituteMapper;
    @Resource
    private ErpRdBomItemSubstituteMapper erpRdBomItemSubstituteMapper;
    @Resource
    private ErpProductService productService;
    @Resource
    private ErpRdBomChangeLogService changeLogService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRdBom(ErpRdBomSaveReqVO createReqVO) {
        validateBomItems(createReqVO.getItems());
        productService.validProductList(List.of(createReqVO.getProductId()));
        ErpRdBomDO rdBom = BeanUtils.toBean(createReqVO, ErpRdBomDO.class)
                .setVersion(null)
                .setStatus(ErpRdBomStatusEnum.DRAFT.getStatus());
        erpRdBomMapper.insert(rdBom);
        saveRdBomItems(rdBom.getId(), createReqVO.getItems());
        return rdBom.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRdBom(ErpRdBomSaveReqVO updateReqVO) {
        ErpRdBomDO existed = validateRdBomExists(updateReqVO.getId());
        if (ErpRdBomStatusEnum.PROCESS.getStatus().equals(existed.getStatus())) {
            throw exception(RD_BOM_BPM_SUBMIT_FAIL);
        }
        validateBomItems(updateReqVO.getItems());
        productService.validProductList(List.of(updateReqVO.getProductId()));
        List<ErpRdBomItemDO> existedItems = erpRdBomItemMapper.selectListByBomId(updateReqVO.getId());
        String oldDetail = buildChangeSnapshot(existed, existedItems);
        erpRdBomMapper.updateById(BeanUtils.toBean(updateReqVO, ErpRdBomDO.class)
                .setVersion(existed.getVersion())
                .setStatus(ErpRdBomStatusEnum.DRAFT.getStatus())
                .setPublishedBomId(existed.getPublishedBomId())
                .setLastPublishedTime(existed.getLastPublishedTime()));
        erpRdBomItemSubstituteMapper.deleteByBomItemIds(convertSet(existedItems, ErpRdBomItemDO::getId));
        erpRdBomItemMapper.deleteByBomId(updateReqVO.getId());
        saveRdBomItems(updateReqVO.getId(), updateReqVO.getItems());
        List<ErpRdBomItemDO> newItems = erpRdBomItemMapper.selectListByBomId(updateReqVO.getId());
        ErpRdBomDO updated = erpRdBomMapper.selectById(updateReqVO.getId());
        String newDetail = buildChangeSnapshot(updated, newItems);
        String diff = "旧值: " + oldDetail + " | 新值: " + newDetail;
        try {
            changeLogService.logChange(updateReqVO.getId(), "UPDATE", diff);
        } catch (Exception e) {
            log.warn("[updateRdBom] 记录变更日志失败，bomId={}", updateReqVO.getId(), e);
        }
    }

    private String buildChangeSnapshot(ErpRdBomDO bom, List<ErpRdBomItemDO> items) {
        StringBuilder sb = new StringBuilder();
        sb.append("bomCode=").append(bom.getBomCode())
                .append(",productId=").append(bom.getProductId())
                .append(",remark=").append(bom.getRemark())
                .append(",items=").append(items != null ? items.size() : 0).append("[");
        if (items != null) {
            for (ErpRdBomItemDO item : items) {
                sb.append("{materialId=").append(item.getMaterialId())
                        .append(",qty=").append(item.getUsageQty())
                        .append(",designator=").append(item.getReferenceDesignator()).append("},");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRdBom(Long id) {
        validateRdBomExists(id);
        List<ErpRdBomItemDO> itemList = erpRdBomItemMapper.selectListByBomId(id);
        erpRdBomItemSubstituteMapper.deleteByBomItemIds(convertSet(itemList, ErpRdBomItemDO::getId));
        erpRdBomMapper.deleteById(id);
        erpRdBomItemMapper.deleteByBomId(id);
    }

    @Override
    public ErpRdBomDO getRdBom(Long id) {
        return erpRdBomMapper.selectById(id);
    }

    @Override
    public ErpRdBomDO getLatestRdBomByProductId(Long productId) {
        if (productId == null) {
            return null;
        }
        List<ErpRdBomDO> bomList = erpRdBomMapper.selectList(ErpRdBomDO::getProductId, productId);
        if (CollUtil.isEmpty(bomList)) {
            return null;
        }
        // 取版本号主版本最大者；并列时取 id 最大（最近创建）
        return bomList.stream()
                .max((a, b) -> {
                    int majorCompare = Integer.compare(extractMajorVersion(a.getVersion()), extractMajorVersion(b.getVersion()));
                    if (majorCompare != 0) {
                        return majorCompare;
                    }
                    return Long.compare(a.getId(), b.getId());
                })
                .orElse(null);
    }

    @Override
    public Map<Long, ErpRdBomDO> getLatestRdBomMapByProductIds(Collection<Long> productIds) {
        if (CollUtil.isEmpty(productIds)) {
            return Map.of();
        }
        List<ErpRdBomDO> bomList = erpRdBomMapper.selectList(ErpRdBomDO::getProductId, productIds);
        Map<Long, ErpRdBomDO> result = new HashMap<>();
        for (ErpRdBomDO bom : bomList) {
            result.merge(bom.getProductId(), bom, (existing, candidate) -> {
                int majorCompare = Integer.compare(extractMajorVersion(candidate.getVersion()), extractMajorVersion(existing.getVersion()));
                if (majorCompare != 0) {
                    return majorCompare > 0 ? candidate : existing;
                }
                return candidate.getId() > existing.getId() ? candidate : existing;
            });
        }
        return result;
    }

    @Override
    public PageResult<ErpRdBomDO> getRdBomPage(ErpRdBomPageReqVO pageReqVO) {
        return erpRdBomMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ErpRdBomItemDO> getRdBomItemList(Long bomId) {
        return erpRdBomItemMapper.selectListByBomId(bomId);
    }

    @Override
    public List<ErpRdBomItemSubstituteDO> getRdBomItemSubstituteList(java.util.Collection<Long> bomItemIds) {
        return erpRdBomItemSubstituteMapper.selectListByBomItemIds(bomItemIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishRdBom(Long id) {
        ErpRdBomDO rdBom = validateRdBomExists(id);
        List<ErpRdBomItemDO> rdBomItems = erpRdBomItemMapper.selectListByBomId(id);
        if (CollUtil.isEmpty(rdBomItems)) {
            throw exception(BOM_ITEM_EMPTY);
        }
        String nextVersion = generateNextVersion(rdBom.getProductId());
        rdBom.setVersion(nextVersion);

        ErpBomDO manufacturingBom = rdBom.getPublishedBomId() == null ? null : erpBomMapper.selectById(rdBom.getPublishedBomId());
        boolean createNewManufacturingBom = manufacturingBom == null
                || ErpBomStatusEnum.ENABLE.getStatus().equals(manufacturingBom.getStatus());

        Long manufacturingBomId;
        if (createNewManufacturingBom) {
            ErpBomDO newManufacturingBom = new ErpBomDO()
                    .setBomCode(rdBom.getBomCode())
                    .setProductId(rdBom.getProductId())
                    .setVersion(rdBom.getVersion())
                    .setStatus(ErpBomStatusEnum.DISABLE.getStatus())
                    .setSourceRdBomId(rdBom.getId())
                    .setRemark(rdBom.getRemark());
            erpBomMapper.insert(newManufacturingBom);
            manufacturingBomId = newManufacturingBom.getId();
        } else {
            manufacturingBomId = manufacturingBom.getId();
            List<ErpBomItemDO> existedManufacturingBomItems = erpBomItemMapper.selectListByBomId(manufacturingBomId);
            erpBomItemSubstituteMapper.deleteByBomItemIds(convertSet(existedManufacturingBomItems, ErpBomItemDO::getId));
            erpBomMapper.updateById(new ErpBomDO()
                    .setId(manufacturingBomId)
                    .setBomCode(rdBom.getBomCode())
                    .setProductId(rdBom.getProductId())
                    .setVersion(rdBom.getVersion())
                    .setStatus(ErpBomStatusEnum.DISABLE.getStatus())
                    .setSourceRdBomId(rdBom.getId())
                    .setRemark(rdBom.getRemark()));
            erpBomItemMapper.deleteByBomId(manufacturingBomId);
        }

        List<ErpBomItemDO> manufacturingBomItems = rdBomItems.stream()
                .map(item -> new ErpBomItemDO()
                        .setBomId(manufacturingBomId)
                        .setMaterialId(item.getMaterialId())
                        .setMaterialType(item.getMaterialType())
                        .setUnitId(item.getUnitId())
                        .setUsageQty(item.getUsageQty())
                        .setLossRate(item.getLossRate())
                        .setReferenceDesignator(item.getReferenceDesignator())
                        .setLeadTimeDay(item.getLeadTimeDay())
                        .setSort(item.getSort())
                        .setRemark(item.getRemark()))
                .toList();
        erpBomItemMapper.insertBatch(manufacturingBomItems);
        copyRdBomItemSubstitutes(rdBomItems, manufacturingBomItems);

        erpRdBomMapper.updateById(new ErpRdBomDO()
                .setId(id)
                .setVersion(nextVersion)
                .setPublishedBomId(manufacturingBomId)
                .setLastPublishedTime(LocalDateTime.now()));
    }

    @Override
    public List<ErpRdBomIntegrityIssueRespVO> validateRdBomIntegrity(Long id) {
        validateRdBomExists(id);
        List<ErpRdBomItemDO> items = erpRdBomItemMapper.selectListByBomId(id);
        List<ErpRdBomIntegrityIssueRespVO> issues = new ArrayList<>();
        if (CollUtil.isEmpty(items)) {
            return issues;
        }

        Set<Long> materialIds = convertSet(items, ErpRdBomItemDO::getMaterialId);
        Map<Long, ErpProductRespVO> productMap = productService.getProductVOMap(materialIds);

        // 收集所有 MAKE（自制件/装配体）的物料，批量判断其是否挂接了下层 BOM（避免 N+1）
        List<Long> makeMaterialIds = items.stream()
                .filter(item -> Integer.valueOf(1).equals(item.getMaterialType()))
                .map(ErpRdBomItemDO::getMaterialId)
                .distinct()
                .collect(Collectors.toList());
        Set<Long> productIdsWithRdBom = Collections.emptySet();
        Set<Long> productIdsWithBom = Collections.emptySet();
        if (CollUtil.isNotEmpty(makeMaterialIds)) {
            productIdsWithRdBom = convertSet(
                    erpRdBomMapper.selectList(ErpRdBomDO::getProductId, makeMaterialIds),
                    ErpRdBomDO::getProductId);
            productIdsWithBom = convertSet(
                    erpBomMapper.selectListByProductIds(makeMaterialIds),
                    ErpBomDO::getProductId);
        }

        for (int i = 0; i < items.size(); i++) {
            ErpRdBomItemDO item = items.get(i);
            int rowIndex = i + 1;
            ErpProductRespVO material = productMap.get(item.getMaterialId());

            // 悬浮件：子件物料不存在，跳过后续该项校验
            if (material == null) {
                issues.add(buildIssue(rowIndex, item.getMaterialId(), null, RdBomIntegrityIssueType.FLOATING_MATERIAL, null));
                continue;
            }

            boolean usageInvalid = item.getUsageQty() == null || item.getUsageQty().compareTo(BigDecimal.ZERO) <= 0;
            if (usageInvalid) {
                issues.add(buildIssue(rowIndex, item.getMaterialId(), material.getName(),
                        RdBomIntegrityIssueType.USAGE_INVALID, null));
            }

            boolean isMake = Integer.valueOf(1).equals(item.getMaterialType());
            if (isMake) {
                if (StrUtil.isNotBlank(item.getReferenceDesignator())) {
                    issues.add(buildIssue(rowIndex, item.getMaterialId(), material.getName(),
                            RdBomIntegrityIssueType.DESIGNATOR_ON_ASSEMBLY, null));
                }
                if (!productIdsWithRdBom.contains(item.getMaterialId())
                        && !productIdsWithBom.contains(item.getMaterialId())) {
                    issues.add(buildIssue(rowIndex, item.getMaterialId(), material.getName(),
                            RdBomIntegrityIssueType.FLOATING_ASSEMBLY, null));
                }
                continue;
            }

            // 采购件 / 需位号元器件
            if (StrUtil.isBlank(item.getReferenceDesignator())) {
                issues.add(buildIssue(rowIndex, item.getMaterialId(), material.getName(),
                        RdBomIntegrityIssueType.MISSING_DESIGNATOR, null));
            } else {
                int designatorCount = BomDesignatorUtils.countDesignators(item.getReferenceDesignator());
                boolean usageIsInteger = item.getUsageQty() != null
                        && item.getUsageQty().remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0;
                if (usageIsInteger && designatorCount != item.getUsageQty().intValue()) {
                    String message = String.format("位号数量 %d 与用量 %s 不一致",
                            designatorCount, item.getUsageQty().toPlainString());
                    issues.add(buildIssue(rowIndex, item.getMaterialId(), material.getName(),
                            RdBomIntegrityIssueType.DESIGNATOR_COUNT_MISMATCH, message));
                }
            }
        }
        return issues;
    }

    private ErpRdBomIntegrityIssueRespVO buildIssue(int rowIndex, Long materialId, String materialName,
                                                   RdBomIntegrityIssueType type, String message) {
        ErpRdBomIntegrityIssueRespVO issue = new ErpRdBomIntegrityIssueRespVO();
        issue.setIssueType(type.getType());
        issue.setSeverity(type.getSeverity());
        issue.setRowIndex(rowIndex);
        issue.setMaterialId(materialId);
        issue.setMaterialName(materialName);
        issue.setMessage(message != null ? message : type.getDefaultMessage());
        return issue;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRdBomStatusByBpm(Long id, String processInstanceId, Integer status, String reason) {
        ErpRdBomDO bom = validateRdBomExists(id);
        if (!StrUtil.equals(processInstanceId, bom.getProcessInstanceId())) {
            throw exception(cn.weitee.erp.module.erp.enums.ErrorCodeConstants.RD_BOM_STATUS_UPDATE_ILLEGAL);
        }
        if (!ErpRdBomStatusEnum.PROCESS.getStatus().equals(bom.getStatus())) {
            log.warn("[updateRdBomStatusByBpm] 忽略非审批中回调，id={}, currentStatus={}, callbackStatus={}", id, bom.getStatus(), status);
            return;
        }
        int count = erpRdBomMapper.update(null, new LambdaUpdateWrapper<ErpRdBomDO>()
                .eq(ErpRdBomDO::getId, id)
                .eq(ErpRdBomDO::getStatus, ErpRdBomStatusEnum.PROCESS.getStatus())
                .eq(ErpRdBomDO::getProcessInstanceId, processInstanceId)
                .set(ErpRdBomDO::getStatus, status)
                .set(ErpRdBomDO::getProcessInstanceId, processInstanceId));
        if (count == 0) {
            throw exception(cn.weitee.erp.module.erp.enums.ErrorCodeConstants.RD_BOM_STATUS_UPDATE_ILLEGAL);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rollbackRdBomStatusToDraftByBpm(Long id, String processInstanceId, String reason) {
        ErpRdBomDO bom = validateRdBomExists(id);
        if (!ErpRdBomStatusEnum.PROCESS.getStatus().equals(bom.getStatus())) {
            log.warn("[rollbackRdBomStatusToDraftByBpm] 忽略非审批中回退，id={}, currentStatus={}", id, bom.getStatus());
            return;
        }
        if (processInstanceId != null && !processInstanceId.equals(bom.getProcessInstanceId())) {
            log.warn("[rollbackRdBomStatusToDraftByBpm] processInstanceId 不匹配，忽略回调，bomId={}, expected={}, actual={}", id, processInstanceId, bom.getProcessInstanceId());
            return;
        }
        int count = erpRdBomMapper.update(null, new LambdaUpdateWrapper<ErpRdBomDO>()
                .eq(ErpRdBomDO::getId, id)
                .eq(ErpRdBomDO::getStatus, ErpRdBomStatusEnum.PROCESS.getStatus())
                .set(ErpRdBomDO::getStatus, ErpRdBomStatusEnum.DRAFT.getStatus())
                .set(ErpRdBomDO::getProcessInstanceId, null));
        if (count == 0) {
            throw exception(cn.weitee.erp.module.erp.enums.ErrorCodeConstants.RD_BOM_STATUS_UPDATE_ILLEGAL);
        }
    }

    @Override
    public List<cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomWhereUsedRespVO> getWhereUsed(Long materialId) {
        if (materialId == null) {
            return List.of();
        }
        Set<Long> visitedBomIds = new java.util.HashSet<>();
        List<cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomWhereUsedRespVO> result = new ArrayList<>();
        java.util.Queue<MaterialLevel> queue = new java.util.LinkedList<>();
        queue.add(new MaterialLevel(materialId, 1));
        Set<Long> queuedMaterialIds = new java.util.HashSet<>();
        queuedMaterialIds.add(materialId);
        Map<Long, cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpProductRespVO> productCache = new HashMap<>();
        while (!queue.isEmpty()) {
            MaterialLevel current = queue.poll();
            if (current.level > 10) {
                continue;
            }
            List<ErpRdBomItemDO> items = erpRdBomItemMapper.selectListByMaterialId(current.materialId);
            if (CollUtil.isEmpty(items)) {
                continue;
            }
            Set<Long> bomIds = convertSet(items, ErpRdBomItemDO::getBomId);
            bomIds.removeAll(visitedBomIds);
            if (bomIds.isEmpty()) {
                continue;
            }
            visitedBomIds.addAll(bomIds);
            List<ErpRdBomDO> boms = erpRdBomMapper.selectBatchIds(new ArrayList<>(bomIds));
            if (CollUtil.isEmpty(boms)) {
                continue;
            }
            Set<Long> productIds = convertSet(boms, ErpRdBomDO::getProductId);
            productIds.removeAll(productCache.keySet());
            if (!productIds.isEmpty()) {
                productCache.putAll(productService.getProductVOMap(productIds));
            }
            for (ErpRdBomDO bom : boms) {
                cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomWhereUsedRespVO vo = new cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomWhereUsedRespVO();
                vo.setBomId(bom.getId());
                vo.setBomCode(bom.getBomCode());
                vo.setProductId(bom.getProductId());
                cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpProductRespVO prod = productCache.get(bom.getProductId());
                if (prod != null) {
                    vo.setProductName(prod.getName());
                }
                vo.setVersion(bom.getVersion());
                vo.setStatus(bom.getStatus());
                vo.setLevel(current.level);
                result.add(vo);
                Long parentMaterialId = bom.getProductId();
                if (!queuedMaterialIds.contains(parentMaterialId) && current.level < 10) {
                    queuedMaterialIds.add(parentMaterialId);
                    queue.add(new MaterialLevel(parentMaterialId, current.level + 1));
                }
            }
        }
        return result;
    }

    private static class MaterialLevel {
        Long materialId;
        int level;
        MaterialLevel(Long materialId, int level) {
            this.materialId = materialId;
            this.level = level;
        }
    }

    private String generateNextVersion(Long productId) {
        List<ErpRdBomDO> existedBomList = erpRdBomMapper.selectList(ErpRdBomDO::getProductId, productId);
        int nextMajorVersion = existedBomList.stream()
                .map(ErpRdBomDO::getVersion)
                .map(this::extractMajorVersion)
                .filter(version -> version > 0)
                .max(Integer::compareTo)
                .orElse(0) + 1;
        return "V" + nextMajorVersion + ".0";
    }

    private int extractMajorVersion(String version) {
        if (version == null || version.isBlank()) {
            return 0;
        }
        Matcher matcher = RD_BOM_VERSION_PATTERN.matcher(version.trim());
        if (!matcher.matches()) {
            return 0;
        }
        return Integer.parseInt(matcher.group(1));
    }

    private void saveRdBomItems(Long bomId, List<ErpRdBomSaveReqVO.Item> items) {
        List<ErpProductDO> products = productService.validProductList(convertSet(items, ErpRdBomSaveReqVO.Item::getMaterialId));
        Map<Long, ErpProductDO> productMap = convertMap(products, ErpProductDO::getId);
        List<ErpRdBomItemDO> itemDOs = BeanUtils.toBean(items, ErpRdBomItemDO.class,
                item -> item.setBomId(bomId).setUnitId(productMap.get(item.getMaterialId()).getUnitId()));
        erpRdBomItemMapper.insertBatch(itemDOs);
        saveRdBomItemSubstitutes(itemDOs, items);
    }

    private void saveRdBomItemSubstitutes(List<ErpRdBomItemDO> itemDOs, List<ErpRdBomSaveReqVO.Item> items) {
        List<ErpRdBomItemSubstituteDO> substituteDOs = new java.util.ArrayList<>();
        for (int i = 0; i < itemDOs.size(); i++) {
            ErpRdBomItemDO itemDO = itemDOs.get(i);
            List<ErpRdBomSaveReqVO.Item.Substitute> substitutes = items.get(i).getSubstitutes();
            if (CollUtil.isEmpty(substitutes)) {
                continue;
            }
            productService.validProductList(convertSet(substitutes, ErpRdBomSaveReqVO.Item.Substitute::getSubstituteMaterialId));
            substituteDOs.addAll(BeanUtils.toBean(substitutes, ErpRdBomItemSubstituteDO.class,
                    substitute -> {
                        substitute.setId(null);
                        substitute.setBomItemId(itemDO.getId());
                    }));
        }
        if (CollUtil.isNotEmpty(substituteDOs)) {
            erpRdBomItemSubstituteMapper.insertBatch(substituteDOs);
        }
    }

    private void copyRdBomItemSubstitutes(List<ErpRdBomItemDO> rdBomItems, List<ErpBomItemDO> manufacturingBomItems) {
        List<ErpRdBomItemSubstituteDO> rdBomItemSubstitutes =
                erpRdBomItemSubstituteMapper.selectListByBomItemIds(convertSet(rdBomItems, ErpRdBomItemDO::getId));
        if (CollUtil.isEmpty(rdBomItemSubstitutes)) {
            return;
        }
        Map<Long, Long> rdBomItemIdMap = new java.util.HashMap<>();
        for (int i = 0; i < rdBomItems.size(); i++) {
            rdBomItemIdMap.put(rdBomItems.get(i).getId(), manufacturingBomItems.get(i).getId());
        }
        List<ErpBomItemSubstituteDO> manufacturingItemSubstitutes = BeanUtils.toBean(rdBomItemSubstitutes,
                ErpBomItemSubstituteDO.class, substitute -> {
                    substitute.setId(null);
                    substitute.setBomItemId(rdBomItemIdMap.get(substitute.getBomItemId()));
                });
        erpBomItemSubstituteMapper.insertBatch(manufacturingItemSubstitutes);
    }

    private void validateBomItems(List<?> items) {
        if (CollUtil.isEmpty(items)) {
            throw exception(BOM_ITEM_EMPTY);
        }
    }

    private ErpRdBomDO validateRdBomExists(Long id) {
        ErpRdBomDO rdBom = erpRdBomMapper.selectById(id);
        if (rdBom == null) {
            throw exception(RD_BOM_NOT_EXISTS);
        }
        return rdBom;
    }

}
