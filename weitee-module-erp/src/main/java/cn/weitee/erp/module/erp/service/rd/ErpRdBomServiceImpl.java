package cn.weitee.erp.module.erp.service.rd;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomApprovalViewRespVO;
import cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomRespVO;
import cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpBomDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpBomItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpBomItemSubstituteDO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductDO;
import cn.weitee.erp.module.erp.dal.dataobject.rd.ErpRdBomDO;
import cn.weitee.erp.module.erp.dal.dataobject.rd.ErpRdBomItemDO;
import cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomChangeLogRespVO;
import cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomIntegrityIssueRespVO;
import cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomVersionDiffRespVO;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpBomItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpBomMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpBomItemSubstituteMapper;
import cn.weitee.erp.module.erp.dal.mysql.rd.ErpRdBomItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.rd.ErpRdBomMapper;
import cn.weitee.erp.module.erp.dal.dataobject.rd.ErpRdBomItemSubstituteDO;
import cn.weitee.erp.module.erp.dal.mysql.rd.ErpRdBomItemSubstituteMapper;
import cn.weitee.erp.module.erp.enums.mrp.ErpBomStatusEnum;
import cn.weitee.erp.module.erp.enums.rd.ErpRdBomChangeType;
import cn.weitee.erp.module.erp.enums.rd.ErpRdBomStatusEnum;
import cn.weitee.erp.module.erp.enums.rd.RdBomIntegrityIssueType;
import cn.weitee.erp.module.erp.service.product.ErpProductService;
import cn.weitee.erp.util.BomDesignatorUtils;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.BOM_INTEGRITY_INVALID;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.BOM_ITEM_EMPTY;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.RD_BOM_APPROVE_LOCKED;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.RD_BOM_BPM_SUBMIT_FAIL;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.RD_BOM_CHANGE_NOT_APPROVED;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.RD_BOM_DELETE_APPROVED_FORBIDDEN;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.RD_BOM_DELETE_PUBLISHED_FORBIDDEN;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.RD_BOM_DELETE_REFERENCED_FORBIDDEN;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.RD_BOM_DELETE_VOID_FORBIDDEN;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.RD_BOM_DIFF_PRODUCT_MISMATCH;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.RD_BOM_NOT_EXISTS;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.RD_BOM_UNVOID_NOT_VOID;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.RD_BOM_VOID_LOCKED;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.RD_BOM_VOID_NOT_APPROVED;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.RD_BOM_PUBLISH_NOT_APPROVED;

@Service
@Validated
@Slf4j
public class ErpRdBomServiceImpl implements ErpRdBomService {

    private static final Pattern RD_BOM_VERSION_PATTERN = Pattern.compile("^[Vv](\\d+)(?:\\.\\d+)?$");

    /** 审批视图携带的最大变更记录条数 */
    private static final int APPROVAL_VIEW_CHANGE_LOG_LIMIT = 10;

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
        assertBomIntegrityPassed(rdBom.getId());
        changeLogService.logChange(rdBom.getId(), ErpRdBomChangeType.CREATE.getType(),
                "创建研发 BOM，版本=" + rdBom.getVersion());
        return rdBom.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRdBom(ErpRdBomSaveReqVO updateReqVO) {
        ErpRdBomDO existed = validateRdBomExists(updateReqVO.getId());
        if (ErpRdBomStatusEnum.PROCESS.getStatus().equals(existed.getStatus())) {
            throw exception(RD_BOM_BPM_SUBMIT_FAIL);
        }
        if (ErpRdBomStatusEnum.APPROVE.getStatus().equals(existed.getStatus())) {
            throw exception(RD_BOM_APPROVE_LOCKED);
        }
        if (ErpRdBomStatusEnum.VOID.getStatus().equals(existed.getStatus())) {
            throw exception(RD_BOM_VOID_LOCKED);
        }
        validateBomItems(updateReqVO.getItems());
        productService.validProductList(List.of(updateReqVO.getProductId()));
        List<ErpRdBomItemDO> newItemDOs = BeanUtils.toBean(updateReqVO.getItems(), ErpRdBomItemDO.class,
                item -> item.setBomId(updateReqVO.getId()));
        assertBomIntegrityPassed(newItemDOs);
        List<ErpRdBomItemDO> existedItems = erpRdBomItemMapper.selectListByBomId(updateReqVO.getId());
        String oldDetail = buildChangeSnapshot(existed, existedItems);
        erpRdBomMapper.updateById(BeanUtils.toBean(updateReqVO, ErpRdBomDO.class)
                .setVersion(existed.getVersion())
                .setStatus(ErpRdBomStatusEnum.DRAFT.getStatus())
                .setProcessInstanceId(null)
                .setPublishedBomId(existed.getPublishedBomId())
                .setLastPublishedTime(existed.getLastPublishedTime()));
        erpRdBomItemSubstituteMapper.deleteByBomItemIds(convertSet(existedItems, ErpRdBomItemDO::getId));
        erpRdBomItemMapper.deleteByBomId(updateReqVO.getId());
        saveRdBomItems(updateReqVO.getId(), updateReqVO.getItems());
        List<ErpRdBomItemDO> newItems = erpRdBomItemMapper.selectListByBomId(updateReqVO.getId());
        ErpRdBomDO updated = erpRdBomMapper.selectById(updateReqVO.getId());
        String newDetail = buildChangeSnapshot(updated, newItems);
        String diff = "旧值: " + oldDetail + " | 新值: " + newDetail;
        changeLogService.logChange(updateReqVO.getId(), ErpRdBomChangeType.UPDATE.getType(), diff);
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
        ErpRdBomDO rdBom = validateRdBomExists(id);
        // 版本历史保护：已审批 / 已发布 / 已作废 / 已派生其他版本的记录不可删除，防止版本链断链
        if (ErpRdBomStatusEnum.APPROVE.getStatus().equals(rdBom.getStatus())) {
            throw exception(RD_BOM_DELETE_APPROVED_FORBIDDEN);
        }
        if (ErpRdBomStatusEnum.VOID.getStatus().equals(rdBom.getStatus())) {
            throw exception(RD_BOM_DELETE_VOID_FORBIDDEN);
        }
        if (rdBom.getPublishedBomId() != null) {
            throw exception(RD_BOM_DELETE_PUBLISHED_FORBIDDEN);
        }
        if (erpRdBomMapper.selectCount(ErpRdBomDO::getSourceBomId, id) > 0) {
            throw exception(RD_BOM_DELETE_REFERENCED_FORBIDDEN);
        }
        List<ErpRdBomItemDO> itemList = erpRdBomItemMapper.selectListByBomId(id);
        erpRdBomItemSubstituteMapper.deleteByBomItemIds(convertSet(itemList, ErpRdBomItemDO::getId));
        erpRdBomMapper.deleteById(id);
        erpRdBomItemMapper.deleteByBomId(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void voidRdBom(Long id, String reason) {
        validateRdBomExists(id);
        // CAS 条件更新：仅当仍处于已审批态时生效，防并发（如同时发起变更/发布）导致误作废
        int updated = erpRdBomMapper.update(new ErpRdBomDO().setStatus(ErpRdBomStatusEnum.VOID.getStatus()),
                new LambdaUpdateWrapper<ErpRdBomDO>()
                        .eq(ErpRdBomDO::getId, id)
                        .eq(ErpRdBomDO::getStatus, ErpRdBomStatusEnum.APPROVE.getStatus()));
        if (updated == 0) {
            throw exception(RD_BOM_VOID_NOT_APPROVED);
        }
        changeLogService.logChange(id, ErpRdBomChangeType.VOID.getType(),
                StrUtil.isBlank(reason) ? "作废研发 BOM" : "作废研发 BOM：" + reason.trim());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unvoidRdBom(Long id) {
        validateRdBomExists(id);
        // 与作废对称的逆向流转：CAS 条件更新，仅当仍处于已作废态时生效
        int updated = erpRdBomMapper.update(new ErpRdBomDO().setStatus(ErpRdBomStatusEnum.APPROVE.getStatus()),
                new LambdaUpdateWrapper<ErpRdBomDO>()
                        .eq(ErpRdBomDO::getId, id)
                        .eq(ErpRdBomDO::getStatus, ErpRdBomStatusEnum.VOID.getStatus()));
        if (updated == 0) {
            throw exception(RD_BOM_UNVOID_NOT_VOID);
        }
        changeLogService.logChange(id, ErpRdBomChangeType.UNVOID.getType(), "取消作废，恢复为已审批");
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
        // 取版本号主版本最大者；并列时取 id 最大（最近创建）；作废版本退出"最新版"选择
        return bomList.stream()
                .filter(bom -> !ErpRdBomStatusEnum.VOID.getStatus().equals(bom.getStatus()))
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
        // 作废版本退出"最新版"选择
        bomList = bomList.stream()
                .filter(bom -> !ErpRdBomStatusEnum.VOID.getStatus().equals(bom.getStatus()))
                .collect(Collectors.toList());
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
    public List<ErpRdBomItemDO> getRdBomItemListByBomIds(Collection<Long> bomIds) {
        if (CollUtil.isEmpty(bomIds)) {
            return List.of();
        }
        return erpRdBomItemMapper.selectListByBomIds(bomIds);
    }

    @Override
    public List<ErpRdBomItemSubstituteDO> getRdBomItemSubstituteList(java.util.Collection<Long> bomItemIds) {
        return erpRdBomItemSubstituteMapper.selectListByBomItemIds(bomItemIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishRdBom(Long id) {
        ErpRdBomDO rdBom = validateRdBomExists(id);
        if (!ErpRdBomStatusEnum.APPROVE.getStatus().equals(rdBom.getStatus())) {
            throw exception(RD_BOM_PUBLISH_NOT_APPROVED);
        }
        assertBomIntegrityPassed(id);
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
                        .setPosition(item.getPosition())
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
    @Transactional(rollbackFor = Exception.class)
    public Long startChangeRdBom(Long id) {
        ErpRdBomDO source = validateRdBomExists(id);
        if (!ErpRdBomStatusEnum.APPROVE.getStatus().equals(source.getStatus())) {
            throw exception(RD_BOM_CHANGE_NOT_APPROVED);
        }
        List<ErpRdBomItemDO> sourceItems = erpRdBomItemMapper.selectListByBomId(id);
        if (CollUtil.isEmpty(sourceItems)) {
            throw exception(BOM_ITEM_EMPTY);
        }
        // 保留源备注，追加变更说明
        String remarkPrefix = StrUtil.isNotBlank(source.getRemark()) ? source.getRemark() + "；" : "";
        String changeRemarkSuffix = "发起变更，源版本=" + source.getVersion();
        // 并发防护：product_id+version 唯一索引兜底，冲突时重新生成版本号重试一次
        ErpRdBomDO changeBom = null;
        for (int attempt = 0; attempt < 2 && changeBom == null; attempt++) {
            String nextVersion = generateNextVersion(source.getProductId());
            try {
                changeBom = new ErpRdBomDO()
                        .setBomCode(source.getBomCode())
                        .setProductId(source.getProductId())
                        .setVersion(nextVersion)
                        .setStatus(ErpRdBomStatusEnum.DRAFT.getStatus())
                        .setSourceBomId(source.getId())
                        .setRemark(remarkPrefix + changeRemarkSuffix);
                erpRdBomMapper.insert(changeBom);
            } catch (DuplicateKeyException e) {
                changeBom = null;
                if (attempt == 1) {
                    throw e;
                }
                log.warn("[startChangeRdBom] 版本号并发冲突，将重新生成版本号，productId={}", source.getProductId());
            }
        }
        ErpRdBomDO createdBom = changeBom;
        List<ErpRdBomItemDO> changeItems = BeanUtils.toBean(sourceItems, ErpRdBomItemDO.class,
                item -> item.setId(null).setBomId(createdBom.getId()));
        erpRdBomItemMapper.insertBatch(changeItems);
        copyRdBomItemSubstitutesForChange(sourceItems, changeItems);
        changeLogService.logChange(createdBom.getId(), ErpRdBomChangeType.CHANGE_CREATE.getType(),
                "发起变更，源 BOM id=" + id + " 源版本=" + source.getVersion() + " 新版本=" + createdBom.getVersion());
        changeLogService.logChange(id, ErpRdBomChangeType.CHANGE_CREATE.getType(),
                "派生变更版本，新 BOM id=" + createdBom.getId() + " 版本=" + createdBom.getVersion());
        return createdBom.getId();
    }

    @Override
    public List<ErpRdBomIntegrityIssueRespVO> validateRdBomIntegrity(Long id) {
        validateRdBomExists(id);
        List<ErpRdBomItemDO> items = erpRdBomItemMapper.selectListByBomId(id);
        return validateItemsIntegrity(items);
    }

    @Override
    public ErpRdBomVersionDiffRespVO getRdBomVersionDiff(Long sourceId, Long targetId) {
        ErpRdBomDO source = validateRdBomExists(sourceId);
        ErpRdBomDO target = validateRdBomExists(targetId);
        if (!Objects.equals(source.getProductId(), target.getProductId())) {
            throw exception(RD_BOM_DIFF_PRODUCT_MISMATCH);
        }
        List<ErpRdBomItemDO> sourceItems = erpRdBomItemMapper.selectListByBomId(sourceId);
        List<ErpRdBomItemDO> targetItems = erpRdBomItemMapper.selectListByBomId(targetId);

        Set<Long> materialIds = convertSet(sourceItems, ErpRdBomItemDO::getMaterialId);
        materialIds.addAll(convertSet(targetItems, ErpRdBomItemDO::getMaterialId));
        Map<Long, String> materialNameMap = new HashMap<>();
        if (CollUtil.isNotEmpty(materialIds)) {
            Map<Long, ErpProductRespVO> productMap = productService.getProductVOMap(materialIds);
            productMap.forEach((id, vo) -> materialNameMap.put(id, vo != null ? vo.getName() : null));
        }

        List<ErpRdBomVersionDiffRespVO.Entry> entries = buildVersionDiffEntries(sourceItems, targetItems, materialNameMap);

        ErpRdBomVersionDiffRespVO result = new ErpRdBomVersionDiffRespVO();
        result.setSourceBomId(sourceId);
        result.setSourceVersion(source.getVersion());
        result.setTargetBomId(targetId);
        result.setTargetVersion(target.getVersion());
        int added = 0, removed = 0, changed = 0, unchanged = 0;
        for (ErpRdBomVersionDiffRespVO.Entry entry : entries) {
            switch (entry.getChangeType()) {
                case "ADDED" -> added++;
                case "REMOVED" -> removed++;
                case "CHANGED" -> changed++;
                default -> unchanged++;
            }
        }
        result.setAddedCount(added);
        result.setRemovedCount(removed);
        result.setChangedCount(changed);
        result.setUnchangedCount(unchanged);
        result.setEntries(entries);
        return result;
    }

    @Override
    public List<ErpRdBomDO> getRdBomVersionChain(Long id) {
        ErpRdBomDO current = validateRdBomExists(id);
        List<ErpRdBomDO> all = erpRdBomMapper.selectList(ErpRdBomDO::getProductId, current.getProductId());
        Map<Long, ErpRdBomDO> byId = new HashMap<>();
        all.forEach(bom -> byId.put(bom.getId(), bom));

        // 沿 sourceBomId 双向收集：向上直溯，向下逐轮扩展直到无新增
        Set<Long> chainIds = new java.util.HashSet<>();
        Long cursor = current.getId();
        while (cursor != null && byId.containsKey(cursor) && chainIds.add(cursor)) {
            cursor = byId.get(cursor).getSourceBomId();
        }
        boolean expanded = true;
        while (expanded) {
            expanded = false;
            for (ErpRdBomDO bom : all) {
                if (bom.getSourceBomId() != null && chainIds.contains(bom.getSourceBomId())
                        && chainIds.add(bom.getId())) {
                    expanded = true;
                }
            }
        }
        return all.stream()
                .filter(bom -> chainIds.contains(bom.getId()))
                .sorted((a, b) -> Integer.compare(
                        extractMajorVersion(b.getVersion()), extractMajorVersion(a.getVersion())))
                .toList();
    }

    @Override
    public ErpRdBomApprovalViewRespVO getRdBomApprovalView(Long id) {
        ErpRdBomDO bom = validateRdBomExists(id);
        ErpRdBomApprovalViewRespVO view = new ErpRdBomApprovalViewRespVO();
        view.setBom(buildApprovalBomVO(bom));
        // 1. 对比基准：sourceBomId 优先，为空时回退同成品最近一个 APPROVE 版本，均无则视为首次提交
        ErpRdBomDO baseline = resolveDiffBaseline(bom);
        if (baseline != null) {
            view.setFirstSubmit(false);
            view.setBaselineBomId(baseline.getId());
            view.setBaselineVersion(baseline.getVersion());
            view.setDiff(getRdBomVersionDiff(baseline.getId(), bom.getId()));
        } else {
            view.setFirstSubmit(true);
        }
        // 2. 变更记录：最近在前，仅保留最近 N 条
        List<ErpRdBomChangeLogRespVO> logs = changeLogService.getChangeLogList(id);
        if (CollUtil.isEmpty(logs)) {
            logs = List.of();
        } else {
            logs = logs.stream()
                    .sorted((a, b) -> Long.compare(b.getId() != null ? b.getId() : 0L,
                            a.getId() != null ? a.getId() : 0L))
                    .limit(APPROVAL_VIEW_CHANGE_LOG_LIMIT)
                    .toList();
        }
        view.setChangeLogs(logs);
        return view;
    }

    /**
     * 解析审批视图的 diff 基准版本：
     * 1. sourceBomId 非空且记录存在 → 直接返回（升版变更精确锚点）
     * 2. 回退同成品下最近一个 APPROVE 版本（导入等无锚点场景），主版本号大者优先、编号大者兜底
     * 3. 均未命中 → 返回 null（首次提交）
     */
    private ErpRdBomDO resolveDiffBaseline(ErpRdBomDO bom) {
        if (bom.getSourceBomId() != null) {
            ErpRdBomDO source = erpRdBomMapper.selectById(bom.getSourceBomId());
            if (source != null && Objects.equals(source.getProductId(), bom.getProductId())) {
                return source;
            }
        }
        return erpRdBomMapper.selectList(ErpRdBomDO::getProductId, bom.getProductId()).stream()
                .filter(item -> !Objects.equals(item.getId(), bom.getId()))
                .filter(item -> ErpRdBomStatusEnum.APPROVE.getStatus().equals(item.getStatus()))
                .filter(item -> StrUtil.isNotBlank(item.getVersion()))
                .max((a, b) -> {
                    int byVersion = Integer.compare(
                            extractMajorVersion(a.getVersion()), extractMajorVersion(b.getVersion()));
                    return byVersion != 0 ? byVersion
                            : Long.compare(a.getId() != null ? a.getId() : 0L, b.getId() != null ? b.getId() : 0L);
                })
                .orElse(null);
    }

    private ErpRdBomRespVO buildApprovalBomVO(ErpRdBomDO bom) {
        ErpRdBomRespVO respVO = BeanUtils.toBean(bom, ErpRdBomRespVO.class);
        List<ErpRdBomItemDO> itemList = getRdBomItemList(bom.getId());
        Map<Long, ErpProductRespVO> productMap = productService.getProductVOMap(convertSet(itemList, ErpRdBomItemDO::getMaterialId));
        Set<Long> itemIds = convertSet(itemList, ErpRdBomItemDO::getId);
        List<ErpRdBomItemSubstituteDO> substituteList = itemIds.isEmpty()
                ? List.of() : getRdBomItemSubstituteList(itemIds);
        Map<Long, List<ErpRdBomItemSubstituteDO>> substituteMap = new HashMap<>();
        for (ErpRdBomItemSubstituteDO substitute : substituteList) {
            substituteMap.computeIfAbsent(substitute.getBomItemId(), key -> new ArrayList<>()).add(substitute);
        }
        ErpProductRespVO headerProduct = productService.getProductVOMap(List.of(bom.getProductId())).get(bom.getProductId());
        if (headerProduct != null) {
            respVO.setProductName(headerProduct.getName());
        }
        respVO.setItems(BeanUtils.toBean(itemList, ErpRdBomRespVO.Item.class, item -> {
            ErpProductRespVO material = productMap.get(item.getMaterialId());
            if (material != null) {
                item.setMaterialName(material.getName());
                item.setUnitName(material.getUnitName());
            }
            List<ErpRdBomItemSubstituteDO> subs = substituteMap.get(item.getId());
            if (subs == null || subs.isEmpty()) {
                item.setSubstitutes(List.of());
                return;
            }
            Map<Long, ErpProductRespVO> subProductMap = productService.getProductVOMap(
                    convertSet(subs, ErpRdBomItemSubstituteDO::getSubstituteMaterialId));
            item.setSubstitutes(BeanUtils.toBean(subs, ErpRdBomRespVO.Item.Substitute.class, sub -> {
                ErpProductRespVO subProduct = subProductMap.get(sub.getSubstituteMaterialId());
                if (subProduct != null) {
                    sub.setSubstituteMaterialName(subProduct.getName());
                }
            }));
        }));
        return respVO;
    }

    /**
     * 版本明细对比（纯内存计算，静态方法便于单测）。
     * 排序规则：新增/修改/未变按新版本明细行序在前，被删除的旧版条目按旧行序附后。
     * 同一物料在单版内出现多行时取 id 较大者（正常数据唯一，防御性处理）。
     */
    static List<ErpRdBomVersionDiffRespVO.Entry> buildVersionDiffEntries(List<ErpRdBomItemDO> sourceItems,
                                                                         List<ErpRdBomItemDO> targetItems,
                                                                         Map<Long, String> materialNameMap) {
        Map<Long, ErpRdBomItemDO> sourceMap = toLatestItemMap(sourceItems);
        Map<Long, ErpRdBomItemDO> targetMap = toLatestItemMap(targetItems);
        List<ErpRdBomVersionDiffRespVO.Entry> entries = new ArrayList<>();
        Set<Long> consumedSourceMaterialIds = new java.util.HashSet<>();
        // 先遍历新版行序：ADDED / CHANGED / UNCHANGED
        for (ErpRdBomItemDO targetItem : targetItems) {
            Long materialId = targetItem.getMaterialId();
            if (targetMap.get(materialId) != targetItem) {
                continue;
            }
            ErpRdBomItemDO sourceItem = sourceMap.get(materialId);
            if (sourceItem == null) {
                entries.add(buildEntry("ADDED", materialId, materialNameMap, null, targetItem, null));
            } else {
                consumedSourceMaterialIds.add(materialId);
                List<ErpRdBomVersionDiffRespVO.FieldChange> changes = new ArrayList<>();
                compareItemFields(sourceItem, targetItem, changes);
                String changeType = changes.isEmpty() ? "UNCHANGED" : "CHANGED";
                entries.add(buildEntry(changeType, materialId, materialNameMap, sourceItem, targetItem, changes));
            }
        }
        // 再补旧版独有：REMOVED
        for (ErpRdBomItemDO sourceItem : sourceItems) {
            Long materialId = sourceItem.getMaterialId();
            if (sourceMap.get(materialId) != sourceItem || consumedSourceMaterialIds.contains(materialId)) {
                continue;
            }
            consumedSourceMaterialIds.add(materialId);
            entries.add(buildEntry("REMOVED", materialId, materialNameMap, sourceItem, null, null));
        }
        return entries;
    }

    private static Map<Long, ErpRdBomItemDO> toLatestItemMap(List<ErpRdBomItemDO> items) {
        Map<Long, ErpRdBomItemDO> map = new LinkedHashMap<>();
        for (ErpRdBomItemDO item : items) {
            map.merge(item.getMaterialId(), item,
                    (a, b) -> a.getId() != null && b.getId() != null && a.getId() > b.getId() ? a : b);
        }
        return map;
    }

    private static ErpRdBomVersionDiffRespVO.Entry buildEntry(String changeType, Long materialId,
                                                              Map<Long, String> materialNameMap,
                                                              ErpRdBomItemDO sourceItem, ErpRdBomItemDO targetItem,
                                                              List<ErpRdBomVersionDiffRespVO.FieldChange> changes) {
        ErpRdBomVersionDiffRespVO.Entry entry = new ErpRdBomVersionDiffRespVO.Entry();
        entry.setChangeType(changeType);
        entry.setMaterialId(materialId);
        entry.setMaterialName(materialNameMap.get(materialId));
        entry.setMaterialType(targetItem != null ? targetItem.getMaterialType() : sourceItem.getMaterialType());
        entry.setOldItem(sourceItem != null ? toSnapshot(sourceItem) : null);
        entry.setNewItem(targetItem != null ? toSnapshot(targetItem) : null);
        entry.setChanges(changes == null || changes.isEmpty() ? null : changes);
        return entry;
    }

    private static ErpRdBomVersionDiffRespVO.ItemSnapshot toSnapshot(ErpRdBomItemDO item) {
        ErpRdBomVersionDiffRespVO.ItemSnapshot snapshot = new ErpRdBomVersionDiffRespVO.ItemSnapshot();
        snapshot.setUsageQty(item.getUsageQty());
        snapshot.setReferenceDesignator(item.getReferenceDesignator());
        snapshot.setPosition(item.getPosition());
        snapshot.setLossRate(item.getLossRate());
        snapshot.setLeadTimeDay(item.getLeadTimeDay());
        snapshot.setRemark(item.getRemark());
        return snapshot;
    }

    private static void compareItemFields(ErpRdBomItemDO oldItem, ErpRdBomItemDO newItem,
                                          List<ErpRdBomVersionDiffRespVO.FieldChange> changes) {
        if (!numEquals(oldItem.getUsageQty(), newItem.getUsageQty())) {
            addFieldChange(changes, "usageQty", "用量", oldItem.getUsageQty(), newItem.getUsageQty());
        }
        if (!Objects.equals(oldItem.getReferenceDesignator(), newItem.getReferenceDesignator())) {
            addFieldChange(changes, "referenceDesignator", "位号", oldItem.getReferenceDesignator(), newItem.getReferenceDesignator());
        }
        if (!Objects.equals(oldItem.getPosition(), newItem.getPosition())) {
            addFieldChange(changes, "position", "位置", oldItem.getPosition(), newItem.getPosition());
        }
        if (!numEquals(oldItem.getLossRate(), newItem.getLossRate())) {
            addFieldChange(changes, "lossRate", "损耗率", oldItem.getLossRate(), newItem.getLossRate());
        }
        if (!Objects.equals(oldItem.getLeadTimeDay(), newItem.getLeadTimeDay())) {
            addFieldChange(changes, "leadTimeDay", "提前期(天)", oldItem.getLeadTimeDay(), newItem.getLeadTimeDay());
        }
        if (!Objects.equals(oldItem.getRemark(), newItem.getRemark())) {
            addFieldChange(changes, "remark", "备注", oldItem.getRemark(), newItem.getRemark());
        }
    }

    private static void addFieldChange(List<ErpRdBomVersionDiffRespVO.FieldChange> changes,
                                       String field, String label, Object oldValue, Object newValue) {
        ErpRdBomVersionDiffRespVO.FieldChange change = new ErpRdBomVersionDiffRespVO.FieldChange();
        change.setField(field);
        change.setLabel(label);
        change.setOldValue(oldValue != null ? String.valueOf(oldValue) : "");
        change.setNewValue(newValue != null ? String.valueOf(newValue) : "");
        changes.add(change);
    }

    private static boolean numEquals(BigDecimal a, BigDecimal b) {
        return a == null ? b == null : b != null && a.compareTo(b) == 0;
    }

    /**
     * 校验 BOM 明细完整性。ERROR 级问题（悬浮件 / 用量无效 / 缺位号 / 位号数量与用量不一致）将阻断保存。
     */
    private List<ErpRdBomIntegrityIssueRespVO> validateItemsIntegrity(List<ErpRdBomItemDO> items) {
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

    /**
     * 保存前拦截：若存在 ERROR 级完整性问题，直接拒绝保存。
     */
    private void assertBomIntegrityPassed(Long id) {
        List<ErpRdBomIntegrityIssueRespVO> issues = validateRdBomIntegrity(id);
        if (issues.stream().anyMatch(issue -> "ERROR".equals(issue.getSeverity()))) {
            throw exception(BOM_INTEGRITY_INVALID);
        }
    }

    private void assertBomIntegrityPassed(List<ErpRdBomItemDO> items) {
        List<ErpRdBomIntegrityIssueRespVO> issues = validateItemsIntegrity(items);
        if (issues.stream().anyMatch(issue -> "ERROR".equals(issue.getSeverity()))) {
            throw exception(BOM_INTEGRITY_INVALID);
        }
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
                item -> item.setId(null).setBomId(bomId).setUnitId(productMap.get(item.getMaterialId()).getUnitId()));
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

    private void copyRdBomItemSubstitutesForChange(List<ErpRdBomItemDO> sourceItems, List<ErpRdBomItemDO> targetItems) {
        List<ErpRdBomItemSubstituteDO> substitutes =
                erpRdBomItemSubstituteMapper.selectListByBomItemIds(convertSet(sourceItems, ErpRdBomItemDO::getId));
        if (CollUtil.isEmpty(substitutes)) {
            return;
        }
        Map<Long, Long> idMap = new java.util.HashMap<>();
        for (int i = 0; i < sourceItems.size(); i++) {
            idMap.put(sourceItems.get(i).getId(), targetItems.get(i).getId());
        }
        List<ErpRdBomItemSubstituteDO> newSubstitutes = BeanUtils.toBean(substitutes,
                ErpRdBomItemSubstituteDO.class, s -> {
                    s.setId(null);
                    s.setBomItemId(idMap.get(s.getBomItemId()));
                });
        erpRdBomItemSubstituteMapper.insertBatch(newSubstitutes);
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
