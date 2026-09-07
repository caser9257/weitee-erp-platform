package cn.weitee.erp.module.erp.service.product;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.weitee.erp.framework.common.util.json.JsonUtils;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpProductApprovalViewRespVO;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpProductBatchApprovalViewRespVO;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ProductSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpBomItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductCadenceDO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductCodeHistoryDO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductDO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductPendingChangeDO;
import cn.weitee.erp.module.erp.dal.dataobject.rd.ErpRdBomItemDO;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpBomItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.product.ErpProductCadenceMapper;
import cn.weitee.erp.module.erp.dal.mysql.product.ErpProductCodeHistoryMapper;
import cn.weitee.erp.module.erp.dal.mysql.product.ErpProductMapper;
import cn.weitee.erp.module.erp.dal.mysql.product.ErpProductPendingChangeMapper;
import cn.weitee.erp.module.erp.dal.mysql.rd.ErpRdBomItemMapper;
import cn.weitee.erp.module.erp.enums.ErpProductBpmConstants;
import cn.weitee.erp.module.erp.enums.ErrorCodeConstants;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;

@Service
@Validated
@Slf4j
public class ErpProductPendingChangeServiceImpl implements ErpProductPendingChangeService {

    /**
     * 可比对的物料业务字段注册表（field -> 中文标签 + 双端取值器 + 是否 BigDecimal 比较）。
     * 显式枚举而非反射：类型可控、新增字段必须显式登记，避免漏比或错比。
     */
    private static final List<FieldSpec> FIELD_SPECS = List.of(
            spec("name", "名称", ProductSaveReqVO::getName, ErpProductDO::getName),
            spec(ErpProductBpmConstants.FROZEN_FIELD_MATERIAL_CODE, "物料编码",
                    ProductSaveReqVO::getMaterialCode, ErpProductDO::getMaterialCode),
            spec("barCode", "条码", ProductSaveReqVO::getBarCode, ErpProductDO::getBarCode),
            spec("categoryId", "分类", ProductSaveReqVO::getCategoryId, ErpProductDO::getCategoryId),
            spec("unitId", "单位", ProductSaveReqVO::getUnitId, ErpProductDO::getUnitId),
            spec("productType", "产品类型", ProductSaveReqVO::getProductType, ErpProductDO::getProductType),
            spec("produceType", "生产方式", ProductSaveReqVO::getProduceType, ErpProductDO::getProduceType),
            spec("batchEnable", "批次启用", ProductSaveReqVO::getBatchEnable, ErpProductDO::getBatchEnable),
            spec("snEnable", "序列号启用", ProductSaveReqVO::getSnEnable, ErpProductDO::getSnEnable),
            spec("defaultRouteId", "默认工艺路线", ProductSaveReqVO::getDefaultRouteId, ErpProductDO::getDefaultRouteId),
            spec("qcEnable", "质检启用", ProductSaveReqVO::getQcEnable, ErpProductDO::getQcEnable),
            spec("outsourceEnable", "委外支持", ProductSaveReqVO::getOutsourceEnable, ErpProductDO::getOutsourceEnable),
            spec("costMethod", "成本方式", ProductSaveReqVO::getCostMethod, ErpProductDO::getCostMethod),
            spec("status", "启停状态", ProductSaveReqVO::getStatus, ErpProductDO::getStatus),
            spec(ErpProductBpmConstants.FROZEN_FIELD_STANDARD, "规格型号",
                    ProductSaveReqVO::getStandard, ErpProductDO::getStandard),
            spec("packaging", "封装", ProductSaveReqVO::getPackaging, ErpProductDO::getPackaging),
            spec("qualityGrade", "质量等级", ProductSaveReqVO::getQualityGrade, ErpProductDO::getQualityGrade),
            spec("brandManufacturer", "品牌/制造商", ProductSaveReqVO::getBrandManufacturer, ErpProductDO::getBrandManufacturer),
            spec("alternativeModel", "替代型号", ProductSaveReqVO::getAlternativeModel, ErpProductDO::getAlternativeModel),
            spec("remark", "备注", ProductSaveReqVO::getRemark, ErpProductDO::getRemark),
            spec("expiryDay", "保质期(天)", ProductSaveReqVO::getExpiryDay, ErpProductDO::getExpiryDay),
            spec("batchControlFlag", "批次管控", ProductSaveReqVO::getBatchControlFlag, ErpProductDO::getBatchControlFlag),
            spec("inspectionRequiredFlag", "必检要求", ProductSaveReqVO::getInspectionRequiredFlag, ErpProductDO::getInspectionRequiredFlag),
            decimalSpec("weight", "重量(mg)", ProductSaveReqVO::getWeight, ErpProductDO::getWeight),
            decimalSpec("purchasePrice", "采购价", ProductSaveReqVO::getPurchasePrice, ErpProductDO::getPurchasePrice),
            decimalSpec("salePrice", "销售价", ProductSaveReqVO::getSalePrice, ErpProductDO::getSalePrice),
            decimalSpec("minPrice", "最低价", ProductSaveReqVO::getMinPrice, ErpProductDO::getMinPrice),
            spec("mrpEnable", "参与MRP", ProductSaveReqVO::getMrpEnable, ErpProductDO::getMrpEnable),
            spec("assetFlag", "资产候选", ProductSaveReqVO::getAssetFlag, ErpProductDO::getAssetFlag),
            spec("pcbComponent", "PCB元器件", ProductSaveReqVO::getPcbComponent, ErpProductDO::getPcbComponent),
            spec("cadenceSchematicPart", "原理图库符号", ProductSaveReqVO::getSchematicPart, ErpProductDO::getSchematicPart),
            spec("cadencePcbFootprint", "PCB封装", ProductSaveReqVO::getPcbFootprint, ErpProductDO::getPcbFootprint),
            spec("cadenceDescription", "关键参数描述", ProductSaveReqVO::getCadenceDescription, ErpProductDO::getCadenceDescription),
            spec("cadenceManufacturerPartNumber", "厂家型号", ProductSaveReqVO::getManufacturerPartNumber, ErpProductDO::getManufacturerPartNumber),
            spec("cadenceDimension", "三维尺寸", ProductSaveReqVO::getDimension, ErpProductDO::getDimension),
            spec("cadenceThreeDLib", "3D模型", ProductSaveReqVO::getThreeDLib, ErpProductDO::getThreeDLib),
            spec("cadenceDatasheet", "数据手册", ProductSaveReqVO::getDatasheet, ErpProductDO::getDatasheet),
            spec("cadenceLifecycle", "生命周期", ProductSaveReqVO::getLifecycle, ErpProductDO::getLifecycle),
            spec("cadencePreferredPart", "是否优选", ProductSaveReqVO::getPreferredPart, ErpProductDO::getPreferredPart),
            spec("cadenceOperatingTemperature", "工作温度", ProductSaveReqVO::getOperatingTemperature, ErpProductDO::getOperatingTemperature),
            spec("cadenceMountingType", "安装类型", ProductSaveReqVO::getMountingType, ErpProductDO::getMountingType),
            spec("cadenceDnp", "空置标志", ProductSaveReqVO::getDnp, ErpProductDO::getDnp),
            spec("cadenceImportedOrReplacement", "进口/替代物料", ProductSaveReqVO::getImportedOrReplacement, ErpProductDO::getImportedOrReplacement),
            spec("cadenceSecondDescription", "参数描述2", ProductSaveReqVO::getSecondDescription, ErpProductDO::getSecondDescription),
            spec("cadenceThirdDescription", "参数描述3", ProductSaveReqVO::getThirdDescription, ErpProductDO::getThirdDescription),
            spec("cadenceFourthDescription", "参数描述4", ProductSaveReqVO::getFourthDescription, ErpProductDO::getFourthDescription)
    );

    @Resource
    private ErpProductPendingChangeMapper pendingChangeMapper;
    @Resource
    private ErpRdBomItemMapper rdBomItemMapper;
    @Resource
    private ErpBomItemMapper bomItemMapper;
    @Resource
    private ErpProductMapper productMapper;
    @Resource
    private ErpProductCadenceMapper productCadenceMapper;
    @Resource
    private ErpProductCodeHistoryMapper codeHistoryMapper;

    @Override
    public Map<String, Object> diffProduct(ErpProductDO existed, ProductSaveReqVO reqVO) {
        ErpProductCadenceConverter.applyToProduct(productCadenceMapper.selectByProductId(existed.getId()), existed);
        Map<String, Object> changes = new LinkedHashMap<>();
        for (FieldSpec field : FIELD_SPECS) {
            Object newValue = field.voGetter().apply(reqVO);
            // null 视为「未提供该字段」：与 applyPendingChange 的 updateById NOT_NULL 落库语义保持一致，
            // 未提供的字段既不会落库也不会构成变更（否则冻结校验会对未触碰字段误拦截）
            if (newValue == null) {
                continue;
            }
            if (!field.equals(existed, newValue)) {
                changes.put(field.field(), newValue);
            }
        }
        return changes;
    }

    @Override
    public void validateFrozenFields(Long productId, Set<String> changedFields) {
        // 编码沿革机制上线后，materialCode 不再冻结：改码可追溯（erp_product_code_history），
        // 历史单据/导入文件按沿革旧码兜底命中；规格型号仍冻结（改规格 = 改设计内容，须走 BOM 升版）。
        Set<String> frozenHits = new LinkedHashSet<>(changedFields);
        frozenHits.retainAll(Set.of(ErpProductBpmConstants.FROZEN_FIELD_STANDARD));
        if (frozenHits.isEmpty()) {
            return;
        }
        if (isReferencedByBom(productId)) {
            throw exception(ErrorCodeConstants.PRODUCT_FROZEN_FIELD_LOCKED,
                    StrUtil.join("、", frozenHits.stream()
                            .map(field -> FIELD_SPECS.stream()
                                    .filter(spec -> spec.field().equals(field)).findFirst()
                                    .map(FieldSpec::label).orElse(field))
                            .toList()));
        }
    }

    @Override
    public boolean isReferencedByBom(Long productId) {
        return !getReferencedProductIds(List.of(productId)).isEmpty();
    }

    @Override
    public java.util.Set<Long> getReferencedProductIds(java.util.Collection<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return java.util.Set.of();
        }
        // 口径：任一研发 BOM 明细或制造 BOM 明细引用即视为在用（草稿也算，因其即将进入审批生效链路）
        java.util.Set<Long> referenced = new java.util.HashSet<>();
        rdBomItemMapper.selectList(new LambdaQueryWrapperX<ErpRdBomItemDO>()
                        .in(ErpRdBomItemDO::getMaterialId, productIds))
                .forEach(item -> referenced.add(item.getMaterialId()));
        bomItemMapper.selectList(new LambdaQueryWrapperX<ErpBomItemDO>()
                        .in(ErpBomItemDO::getMaterialId, productIds))
                .forEach(item -> referenced.add(item.getMaterialId()));
        return referenced;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long upsertPendingChange(Long productId, ProductSaveReqVO target, Set<String> changedFields, String reason) {
        return upsertBatchPendingChange(productId, null, target, changedFields, reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long upsertBatchPendingChange(Long productId, Long batchId, ProductSaveReqVO target,
                                         Set<String> changedFields, String reason) {
        // 唯一约束 uk_product_id：先删后插，保证一个物料同时仅一笔在途变更
        pendingChangeMapper.physicalDeleteByProductId(productId);
        ErpProductPendingChangeDO pending = ErpProductPendingChangeDO.builder()
                .productId(productId)
                .batchId(batchId)
                .changeData(JsonUtils.parseObject(JsonUtils.toJsonString(target), Map.class))
                .changedFields(StrUtil.join(",", changedFields))
                .status(ErpProductPendingChangeDO.STATUS_PENDING)
                .reason(reason)
                .build();
        pendingChangeMapper.insert(pending);
        return pending.getId();
    }

    @Override
    public List<ErpProductPendingChangeDO> getPendingChangesByBatch(Long batchId) {
        return pendingChangeMapper.selectListByBatchId(batchId);
    }

    @Override
    public List<ErpProductPendingChangeDO> getPendingChangesByProductIds(java.util.Collection<Long> productIds) {
        if (CollUtil.isEmpty(productIds)) {
            return List.of();
        }
        return pendingChangeMapper.selectListByProductIds(productIds);
    }

    @Override
    public Long countPendingChangesByBatch(Long batchId) {
        return pendingChangeMapper.selectCountByBatchId(batchId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markBatchFailed(Long batchId, String reason) {
        pendingChangeMapper.update(null, new LambdaUpdateWrapper<ErpProductPendingChangeDO>()
                .eq(ErpProductPendingChangeDO::getBatchId, batchId)
                .set(ErpProductPendingChangeDO::getStatus, ErpProductPendingChangeDO.STATUS_FAILED)
                .set(ErpProductPendingChangeDO::getReason, reason));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBatchProcessInstanceId(Long batchId, String processInstanceId) {
        pendingChangeMapper.update(null, new LambdaUpdateWrapper<ErpProductPendingChangeDO>()
                .eq(ErpProductPendingChangeDO::getBatchId, batchId)
                .set(ErpProductPendingChangeDO::getProcessInstanceId, processInstanceId));
    }

    @Override
    public ErpProductPendingChangeDO getPendingChange(Long productId) {
        return pendingChangeMapper.selectByProductId(productId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean applyPendingChange(Long productId, String processInstanceId, String reason) {
        ErpProductPendingChangeDO pending = pendingChangeMapper.selectByProductId(productId);
        if (pending == null) {
            log.warn("[applyPendingChange] 暂存记录不存在，跳过，productId={}", productId);
            return false;
        }
        // 幂等防御：已完结的终态直接跳过；FAILED/REJECTED 状态收到 approve 回调属于异常时序，放行重试
        if (ErpProductPendingChangeDO.STATUS_APPROVED.equals(pending.getStatus())) {
            log.info("[applyPendingChange] 变更已生效，幂等跳过，productId={}", productId);
            return false;
        }
        // 1. 业务先执行：暂存快照落主表（reqVO 无 auditStatus/processInstanceId 字段，
        //    toBean 后两者为 null，updateById 按 NOT_NULL 策略不会覆盖主表审批字段）
        ProductSaveReqVO target = JsonUtils.parseObject(JsonUtils.toJsonString(pending.getChangeData()),
                ProductSaveReqVO.class);
        target.setId(productId);
        ErpProductDO updateObj = BeanUtils.toBean(target, ErpProductDO.class);
        updateObj.setAuditStatus(null);
        updateObj.setProcessInstanceId(processInstanceId);
        Set<String> changedFields = new LinkedHashSet<>(StrUtil.split(pending.getChangedFields(), ','));
        // 编码沿革：改码需在业务事务内写沿革记录 + 主表冗余旧码；旧码占用校验前置，防并发复用
        String oldCode = null;
        boolean codeChanged = false;
        if (changedFields.contains(ErpProductBpmConstants.FROZEN_FIELD_MATERIAL_CODE)
                && StrUtil.isNotBlank(target.getMaterialCode())) {
            ErpProductDO before = productMapper.selectById(productId);
            oldCode = before != null ? before.getMaterialCode() : null;
            codeChanged = oldCode != null && !target.getMaterialCode().equals(oldCode);
            if (codeChanged) {
                updateObj.setPrevMaterialCode(oldCode);
            }
        }
        int count = productMapper.updateById(updateObj);
        if (count == 0) {
            log.error("[applyPendingChange] 主表落库失败，productId={}", productId);
            throw exception(ErrorCodeConstants.PRODUCT_AUDIT_STATUS_ILLEGAL);
        }
        if (codeChanged) {
            recordMaterialCodeChange(productId, oldCode, target.getMaterialCode(), processInstanceId, reason);
        }
        if (changedFields.stream().anyMatch(field -> field.startsWith("cadence"))) {
            // 暂存记录可能来自部分更新，未登记的扩展字段不能被 null 覆盖。
            ErpProductCadenceConverter.mergeMissingValues(productCadenceMapper.selectByProductId(productId), updateObj);
            productCadenceMapper.save(ErpProductCadenceConverter.toCadence(updateObj));
        }
        // 2. 业务成功后完结暂存记录（终态单一路径）
        pendingChangeMapper.updateById(new ErpProductPendingChangeDO()
                .setId(pending.getId())
                .setStatus(ErpProductPendingChangeDO.STATUS_APPROVED)
                .setProcessInstanceId(processInstanceId)
                .setReason(reason));
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rejectPendingChange(Long productId, String processInstanceId, String reason) {
        ErpProductPendingChangeDO pending = pendingChangeMapper.selectByProductId(productId);
        if (pending == null) {
            log.warn("[rejectPendingChange] 暂存记录不存在，跳过，productId={}", productId);
            return false;
        }
        if (ErpProductPendingChangeDO.STATUS_APPROVED.equals(pending.getStatus())
                || ErpProductPendingChangeDO.STATUS_REJECTED.equals(pending.getStatus())) {
            log.info("[rejectPendingChange] 变更已完结，幂等跳过，productId={}, status={}", productId, pending.getStatus());
            return false;
        }
        // 驳回留痕：保留记录供追溯；下次提交时 upsert 覆盖。主表从未变过，无需回滚。
        pendingChangeMapper.updateById(new ErpProductPendingChangeDO()
                .setId(pending.getId())
                .setStatus(ErpProductPendingChangeDO.STATUS_REJECTED)
                .setProcessInstanceId(processInstanceId)
                .setReason(reason));
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void discardPendingChange(Long productId) {
        pendingChangeMapper.physicalDeleteByProductId(productId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markFailed(Long productId, String reason) {
        ErpProductPendingChangeDO pending = pendingChangeMapper.selectByProductId(productId);
        if (pending == null) {
            return;
        }
        pendingChangeMapper.updateById(new ErpProductPendingChangeDO()
                .setId(pending.getId())
                .setStatus(ErpProductPendingChangeDO.STATUS_FAILED)
                .setReason(reason));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProcessInstanceId(Long productId, String processInstanceId) {
        ErpProductPendingChangeDO pending = pendingChangeMapper.selectByProductId(productId);
        if (pending == null) {
            return;
        }
        pendingChangeMapper.updateById(new ErpProductPendingChangeDO()
                .setId(pending.getId())
                .setProcessInstanceId(processInstanceId));
    }

    @Override
    public ErpProductApprovalViewRespVO getApprovalView(Long productId) {
        ErpProductDO product = productMapper.selectById(productId);
        if (product == null) {
            throw exception(ErrorCodeConstants.PRODUCT_NOT_EXISTS);
        }
        ErpProductCadenceConverter.applyToProduct(productCadenceMapper.selectByProductId(productId), product);
        ErpProductPendingChangeDO pending = pendingChangeMapper.selectByProductId(productId);
        ErpProductApprovalViewRespVO view = new ErpProductApprovalViewRespVO();
        view.setId(productId);
        view.setCurrent(BeanUtils.toBean(product, ErpProductRespVO.class));
        if (pending == null) {
            view.setDiffs(List.of());
            // 废除等无暂存场景：申请理由回退读主表 abolish_reason（发起废除时预写）
            view.setReason(product.getAbolishReason());
            return view;
        }
        // 暂存快照还原为 VO，逐字段构造 diff（仅 changedFields 中登记的字段）
        ProductSaveReqVO target = JsonUtils.parseObject(JsonUtils.toJsonString(pending.getChangeData()),
                ProductSaveReqVO.class);
        view.setTarget(BeanUtils.toBean(target, ErpProductRespVO.class));
        Set<String> changedFields = new LinkedHashSet<>(StrUtil.split(pending.getChangedFields(), ','));
        List<ErpProductApprovalViewRespVO.FieldDiff> diffs = new ArrayList<>();
        for (FieldSpec field : FIELD_SPECS) {
            if (!changedFields.contains(field.field())) {
                continue;
            }
            ErpProductApprovalViewRespVO.FieldDiff diff = new ErpProductApprovalViewRespVO.FieldDiff();
            diff.setField(field.field());
            diff.setLabel(field.label());
            diff.setOldValue(formatValue(field.doGetter().apply(product)));
            diff.setNewValue(formatValue(field.voGetter().apply(target)));
            diffs.add(diff);
        }
        view.setDiffs(diffs);
        view.setChangedFields(pending.getChangedFields());
        view.setPendingStatus(pending.getStatus());
        view.setReason(pending.getReason());
        view.setProcessInstanceId(pending.getProcessInstanceId());
        return view;
    }

    @Override
    public ErpProductBatchApprovalViewRespVO getBatchApprovalView(Long batchId) {
        List<ErpProductPendingChangeDO> pendings = pendingChangeMapper.selectListByBatchId(batchId);
        if (pendings.isEmpty()) {
            throw exception(ErrorCodeConstants.PRODUCT_PENDING_CHANGE_NOT_EXISTS);
        }
        // 批量查询物料主数据（模式 B：消灭 N+1），diff 计算需要主表现值
        Map<Long, ErpProductDO> productMap = productMapper.selectByIds(
                pendings.stream().map(ErpProductPendingChangeDO::getProductId).toList())
                .stream().collect(java.util.stream.Collectors.toMap(ErpProductDO::getId, Function.identity()));
        ErpProductBatchApprovalViewRespVO view = new ErpProductBatchApprovalViewRespVO();
        view.setBatchId(batchId);
        view.setStatus(pendings.get(0).getStatus());
        view.setReason(pendings.get(0).getReason());
        view.setProcessInstanceId(pendings.get(0).getProcessInstanceId());
        view.setTotalCount(pendings.size());
        List<ErpProductBatchApprovalViewRespVO.Item> items = new ArrayList<>(pendings.size());
        for (ErpProductPendingChangeDO pending : pendings) {
            ErpProductDO product = productMap.get(pending.getProductId());
            ProductSaveReqVO target = JsonUtils.parseObject(JsonUtils.toJsonString(pending.getChangeData()),
                    ProductSaveReqVO.class);
            ErpProductBatchApprovalViewRespVO.Item item = new ErpProductBatchApprovalViewRespVO.Item();
            item.setProductId(pending.getProductId());
            item.setMaterialCode(product != null ? product.getMaterialCode() : null);
            item.setName(product != null ? product.getName() : null);
            item.setChangedFields(pending.getChangedFields());
            Set<String> changedFields = new LinkedHashSet<>(StrUtil.split(pending.getChangedFields(), ','));
            List<ErpProductApprovalViewRespVO.FieldDiff> diffs = new ArrayList<>();
            for (FieldSpec field : FIELD_SPECS) {
                if (!changedFields.contains(field.field())) {
                    continue;
                }
                ErpProductApprovalViewRespVO.FieldDiff diff = new ErpProductApprovalViewRespVO.FieldDiff();
                diff.setField(field.field());
                diff.setLabel(field.label());
                diff.setOldValue(formatValue(product != null ? field.doGetter().apply(product) : null));
                diff.setNewValue(formatValue(field.voGetter().apply(target)));
                diffs.add(diff);
            }
            item.setDiffs(diffs);
            items.add(item);
        }
        view.setItems(items);
        return view;
    }

    private static String formatValue(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof BigDecimal decimal) {
            return decimal.stripTrailingZeros().toPlainString();
        }
        return String.valueOf(value);
    }

    /**
     * 编码沿革统一入口：旧码占用校验（新码是其他物料的沿革旧码则拒绝复用，防混料）+ 写沿革记录。
     * 随业务事务提交，先业务成功后落沿革，主表落库失败则不写。
     * newCode 为空或与 oldCode 相同视为未改码，直接跳过。
     */
    @Override
    public void recordMaterialCodeChange(Long productId, String oldCode, String newCode,
                                         String processInstanceId, String reason) {
        if (StrUtil.isBlank(newCode) || newCode.equals(oldCode)) {
            return;
        }
        if (codeHistoryMapper.existsByOldCode(newCode)) {
            throw exception(ErrorCodeConstants.PRODUCT_MATERIAL_CODE_HISTORY_OCCUPIED, newCode);
        }
        codeHistoryMapper.insert(new ErpProductCodeHistoryDO()
                .setProductId(productId)
                .setOldCode(oldCode)
                .setNewCode(newCode)
                .setProcessInstanceId(processInstanceId)
                .setReason(reason));
    }

    private static FieldSpec spec(String field, String label,
                                  Function<ProductSaveReqVO, Object> voGetter,
                                  Function<ErpProductDO, Object> doGetter) {
        return new FieldSpec(field, label, voGetter, doGetter, false);
    }

    private static FieldSpec decimalSpec(String field, String label,
                                         Function<ProductSaveReqVO, Object> voGetter,
                                         Function<ErpProductDO, Object> doGetter) {
        return new FieldSpec(field, label, voGetter, doGetter, true);
    }

    /**
     * 物料字段比对定义
     */
    private record FieldSpec(String field, String label,
                             Function<ProductSaveReqVO, Object> voGetter,
                             Function<ErpProductDO, Object> doGetter,
                             boolean decimalCompare) {

        boolean equals(ErpProductDO existed, Object newValue) {
            Object oldValue = doGetter.apply(existed);
            if (decimalCompare) {
                return FieldSpec.decimalEquals((BigDecimal) oldValue, (BigDecimal) newValue);
            }
            return Objects.equals(oldValue, newValue);
        }

        private static boolean decimalEquals(BigDecimal a, BigDecimal b) {
            return a == null ? b == null : b != null && a.compareTo(b) == 0;
        }
    }

}
