package cn.weitee.erp.module.erp.service.product;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.collection.MapUtils;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpProductPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpProductSimpleRespVO;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ProductSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductCategoryDO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductCadenceDO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductDO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductPendingChangeDO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductUnitDO;
import cn.weitee.erp.module.erp.dal.mysql.product.ErpProductCadenceMapper;
import cn.weitee.erp.module.erp.dal.mysql.product.ErpProductCodeHistoryMapper;
import cn.weitee.erp.module.erp.dal.mysql.product.ErpProductMapper;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCT_AUDIT_STATUS_ILLEGAL;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCT_UNIT_NOT_BASE;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCT_DELETE_ONLY_DRAFT;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCT_DELETE_REFERENCED_FORBIDDEN;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCT_MATERIAL_CODE_HISTORY_OCCUPIED;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCT_NOT_ENABLE;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCT_NOT_EXISTS;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCT_UPDATE_PROCESSING;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.RD_BOM_MATERIALS_NOT_APPROVED;

/**
 * ERP 产品 Service 实现类
 *
 * @author WeTai
 */
@Service
@Validated
@Slf4j
public class ErpProductServiceImpl implements ErpProductService {

    @Resource
    private ErpProductMapper erpProductMapper;
    @Resource
    private ErpProductCadenceMapper productCadenceMapper;
    @Resource
    private ErpProductCodeHistoryMapper codeHistoryMapper;

    @Resource
    private ErpProductCategoryService productCategoryService;
    @Resource
    private ErpProductUnitService productUnitService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createProduct(ProductSaveReqVO createReqVO) {
        // TODO 芋艿：校验分类
        // 记账单位必须是基本单位，辅助单位只能用于单据录入换算
        validateProductUnitIsBase(createReqVO.getUnitId());
        // 编码沿革：新物料编码不得占用任何历史旧码（DB 唯一约束只管当前码，管不到沿革表）
        if (StrUtil.isNotBlank(createReqVO.getMaterialCode())
                && codeHistoryMapper.existsByOldCode(createReqVO.getMaterialCode())) {
            throw exception(PRODUCT_MATERIAL_CODE_HISTORY_OCCUPIED, createReqVO.getMaterialCode());
        }
        // 插入（新物料默认草稿，需审核后才能被 BOM 引用）
        ErpProductDO product = BeanUtils.toBean(createReqVO, ErpProductDO.class)
                .setAuditStatus(ErpAuditStatus.DRAFT.getStatus())
                .setProcessInstanceId(null);
        erpProductMapper.insert(product);
        if (ErpProductCadenceConverter.hasContent(product)) {
            productCadenceMapper.save(ErpProductCadenceConverter.toCadence(product));
        }
        // 返回
        return product.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProduct(ProductSaveReqVO updateReqVO) {
        // TODO 芋艿：校验分类
        // 记账单位必须是基本单位，辅助单位只能用于单据录入换算
        validateProductUnitIsBase(updateReqVO.getUnitId());
        // 校验存在
        ErpProductDO existed = validateProductExists(updateReqVO.getId());
        // 审批流守卫：新建审批（PROCESS）、两段式审批态、变更编辑态均禁止直改主表
        //（EDITING 走 submitProductUpdate 的暂存保存路由，写暂存不落主表）
        if (ErpAuditStatus.PROCESS.getStatus().equals(existed.getAuditStatus())
                || ErpAuditStatus.isInApprovalFlow(existed.getAuditStatus())
                || ErpAuditStatus.isEditing(existed.getAuditStatus())) {
            throw exception(PRODUCT_UPDATE_PROCESSING, existed.getName());
        }
        // 关键字段冻结与审批状态无关：被 BOM 引用的物料，standard 一律禁改
        // （materialCode 已放开：编码沿革机制保证改码可追溯，历史单据/导入按旧码兜底命中）。
        // 校验放在直改唯一入口，草稿/驳回/失败状态的编辑同样受控（存量脏数据防绕过）。
        java.util.Map<String, Object> changes = pendingChangeService.diffProduct(existed, updateReqVO);
        pendingChangeService.validateFrozenFields(existed.getId(), changes.keySet());
        // 编码沿革：直改路径改码同样留痕（旧码冗余 + 沿革表），旧码占用校验在写沿革时前置
        String oldMaterialCode = existed.getMaterialCode();
        String newMaterialCode = (String) changes.get(
                cn.weitee.erp.module.erp.enums.ErpProductBpmConstants.FROZEN_FIELD_MATERIAL_CODE);
        boolean codeChanged = StrUtil.isNotBlank(newMaterialCode) && !newMaterialCode.equals(oldMaterialCode);
        // 更新
        ErpProductDO updateObj = BeanUtils.toBean(updateReqVO, ErpProductDO.class);
        if (codeChanged) {
            updateObj.setPrevMaterialCode(oldMaterialCode);
        }
        erpProductMapper.updateById(updateObj);
        if (codeChanged) {
            pendingChangeService.recordMaterialCodeChange(existed.getId(), oldMaterialCode,
                    newMaterialCode, null, null);
        }
        if (hasCadenceChanges(changes.keySet())) {
            // 部分更新请求未携带的 Cadence 字段为 null；扩展表保存前合并现有值，避免误清空。
            ErpProductCadenceConverter.mergeMissingValues(productCadenceMapper.selectByProductId(existed.getId()), updateObj);
            productCadenceMapper.save(ErpProductCadenceConverter.toCadence(updateObj));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProduct(Long id) {
        // 校验存在
        ErpProductDO product = validateProductExists(id);
        // 删除权限收紧：仅未生效物料（草稿/驳回/流程失败）允许物理删除，用于清理误建档；
        // 已生效物料的生命周期出口只有停用（冻结）/废除（销号留痕），均走两段式审批，杜绝绕过审批留痕的删除入口
        if (!ErpAuditStatus.DRAFT.getStatus().equals(product.getAuditStatus())
                && !ErpAuditStatus.REJECT.getStatus().equals(product.getAuditStatus())
                && !ErpAuditStatus.FAILED.getStatus().equals(product.getAuditStatus())) {
            throw exception(PRODUCT_DELETE_ONLY_DRAFT, product.getName());
        }
        // P1 删除保护：被任一 BOM（研发/制造明细）引用的物料禁止删除，防止 BOM 悬挂引用
        if (pendingChangeService.isReferencedByBom(id)) {
            throw exception(PRODUCT_DELETE_REFERENCED_FORBIDDEN);
        }
        // 删除
        productCadenceMapper.deleteByProductId(id);
        erpProductMapper.deleteById(id);
    }

    @Override
    public List<ErpProductDO> validProductList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<ErpProductDO> list = erpProductMapper.selectByIds(ids);
        Map<Long, ErpProductDO> productMap = convertMap(list, ErpProductDO::getId);
        for (Long id : ids) {
            ErpProductDO product = productMap.get(id);
            if (productMap.get(id) == null) {
                throw exception(PRODUCT_NOT_EXISTS);
            }
            if (CommonStatusEnum.isDisable(product.getStatus())) {
                throw exception(PRODUCT_NOT_ENABLE, product.getName());
            }
        }
        return list;
    }

    @Override
    public void validateProductsApprovedForBom(Collection<Long> productIds) {
        if (CollUtil.isEmpty(productIds)) {
            return;
        }
        List<ErpProductDO> list = erpProductMapper.selectByIds(productIds);
        Map<Long, ErpProductDO> productMap = convertMap(list, ErpProductDO::getId);
        // P5：BOM 发布是消费点——聚合收集全部未审核物料一次性报出，避免逐个整改
        List<String> offenders = new ArrayList<>();
        for (Long id : new LinkedHashSet<>(productIds)) {
            ErpProductDO product = productMap.get(id);
            if (product == null) {
                throw exception(PRODUCT_NOT_EXISTS);
            }
            if (!ErpAuditStatus.APPROVE.getStatus().equals(product.getAuditStatus())) {
                offenders.add(product.getName() + "（" + product.getMaterialCode() + "/"
                        + ErpAuditStatus.nameOf(product.getAuditStatus()) + "）");
            }
        }
        if (CollUtil.isNotEmpty(offenders)) {
            throw exception(RD_BOM_MATERIALS_NOT_APPROVED, String.join("、", offenders));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProductAuditStatusByBpm(Long id, String processInstanceId, Integer status, String reason) {
        ErpProductDO product = erpProductMapper.selectById(id);
        if (product == null) {
            throw exception(PRODUCT_NOT_EXISTS);
        }
        if (!StrUtil.equals(processInstanceId, product.getProcessInstanceId())) {
            throw exception(PRODUCT_AUDIT_STATUS_ILLEGAL);
        }
        if (!ErpAuditStatus.PROCESS.getStatus().equals(product.getAuditStatus())) {
            log.warn("[updateProductAuditStatusByBpm] 忽略非审批中回调，id={}, auditStatus={}", id, product.getAuditStatus());
            return;
        }
        int count = erpProductMapper.update(null, new LambdaUpdateWrapper<ErpProductDO>()
                .eq(ErpProductDO::getId, id)
                .eq(ErpProductDO::getAuditStatus, ErpAuditStatus.PROCESS.getStatus())
                .eq(ErpProductDO::getProcessInstanceId, processInstanceId)
                .set(ErpProductDO::getAuditStatus, status)
                .set(ErpProductDO::getProcessInstanceId, processInstanceId));
        if (count == 0) {
            throw exception(PRODUCT_AUDIT_STATUS_ILLEGAL);
        }
        // 审批通过时同步启用
        if (ErpAuditStatus.APPROVE.getStatus().equals(status)) {
            erpProductMapper.updateById(new ErpProductDO().setId(id).setStatus(CommonStatusEnum.ENABLE.getStatus()));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rollbackProductAuditStatusToDraftByBpm(Long id, String processInstanceId, String reason) {
        ErpProductDO product = erpProductMapper.selectById(id);
        if (product == null) {
            throw exception(PRODUCT_NOT_EXISTS);
        }
        if (!ErpAuditStatus.PROCESS.getStatus().equals(product.getAuditStatus())) {
            log.warn("[rollbackProductAuditStatusToDraftByBpm] 忽略非审批中回退，id={}, auditStatus={}", id, product.getAuditStatus());
            return;
        }
        if (processInstanceId != null && !processInstanceId.equals(product.getProcessInstanceId())) {
            log.warn("[rollbackProductAuditStatusToDraftByBpm] processInstanceId 不匹配，id={}, expected={}, actual={}", id, processInstanceId, product.getProcessInstanceId());
            return;
        }
        int count = erpProductMapper.update(null, new LambdaUpdateWrapper<ErpProductDO>()
                .eq(ErpProductDO::getId, id)
                .eq(ErpProductDO::getAuditStatus, ErpAuditStatus.PROCESS.getStatus())
                .set(ErpProductDO::getAuditStatus, ErpAuditStatus.DRAFT.getStatus())
                .set(ErpProductDO::getProcessInstanceId, null));
        if (count == 0) {
            throw exception(PRODUCT_AUDIT_STATUS_ILLEGAL);
        }
    }

    /**
     * 通用两段式阶段流转回调（CAS 幂等）：
     * 校验流程实例匹配 + 当前处于目标 PENDING 态，按 approved 落到 toApprove / toReject 状态。
     */
    private void completeStageCallback(Long id, String processInstanceId, Integer expectPending,
                                       Integer toApprove, Integer toReject, boolean approved, String reason) {
        ErpProductDO product = erpProductMapper.selectById(id);
        if (product == null) {
            throw exception(PRODUCT_NOT_EXISTS);
        }
        if (!StrUtil.equals(processInstanceId, product.getProcessInstanceId())) {
            throw exception(PRODUCT_AUDIT_STATUS_ILLEGAL);
        }
        if (!expectPending.equals(product.getAuditStatus())) {
            log.warn("[completeStageCallback] 忽略非目标审批态回调，id={}, expect={}, actual={}",
                    id, expectPending, product.getAuditStatus());
            return;
        }
        int count = erpProductMapper.update(null, new LambdaUpdateWrapper<ErpProductDO>()
                .eq(ErpProductDO::getId, id)
                .eq(ErpProductDO::getAuditStatus, expectPending)
                .eq(ErpProductDO::getProcessInstanceId, processInstanceId)
                .set(ErpProductDO::getAuditStatus, approved ? toApprove : toReject)
                .set(ErpProductDO::getProcessInstanceId, null));
        if (count == 0) {
            throw exception(PRODUCT_AUDIT_STATUS_ILLEGAL);
        }
        log.info("[completeStageCallback] id={}, {} -> {}, approved={}, reason={}",
                id, ErpAuditStatus.nameOf(expectPending),
                ErpAuditStatus.nameOf(approved ? toApprove : toReject), approved, reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeChangeRequest(Long id, String processInstanceId, boolean approved, String reason) {
        // CR_PENDING → EDITING（通过，解锁编辑）/ APPROVE（驳回、撤回）
        if (!approved) {
            // 阶段一驳回/撤回：清理申请时预写的空暂存（仅承载申请理由，未进入编辑），避免残留影响后续视图
            pendingChangeService.discardPendingChange(id);
        }
        completeStageCallback(id, processInstanceId, ErpAuditStatus.CR_PENDING.getStatus(),
                ErpAuditStatus.EDITING.getStatus(), ErpAuditStatus.APPROVE.getStatus(), approved, reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeChangeConfirm(Long id, String processInstanceId, boolean approved, String reason) {
        // CONFIRM_PENDING → APPROVE（通过，暂存落主表）/ EDITING（驳回、撤回）
        if (approved) {
            ErpProductDO product = erpProductMapper.selectById(id);
            if (product == null) {
                throw exception(PRODUCT_NOT_EXISTS);
            }
            if (!StrUtil.equals(processInstanceId, product.getProcessInstanceId())) {
                throw exception(PRODUCT_AUDIT_STATUS_ILLEGAL);
            }
            if (!ErpAuditStatus.CONFIRM_PENDING.getStatus().equals(product.getAuditStatus())) {
                log.warn("[completeChangeConfirm] 忽略非变更确认态回调，id={}, auditStatus={}",
                        id, product.getAuditStatus());
                return;
            }
            // 先业务后终态：暂存快照落主表，成功后才流转 APPROVE
            boolean applied = pendingChangeService.applyPendingChange(id, processInstanceId, reason);
            if (!applied) {
                // CONFIRM_PENDING 态必然存在 PENDING 暂存；apply 失败说明数据异常，回滚事务
                throw exception(PRODUCT_AUDIT_STATUS_ILLEGAL);
            }
        }
        completeStageCallback(id, processInstanceId, ErpAuditStatus.CONFIRM_PENDING.getStatus(),
                ErpAuditStatus.APPROVE.getStatus(), ErpAuditStatus.EDITING.getStatus(), approved, reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeObsoleteRequest(Long id, String processInstanceId, boolean approved, String reason) {
        // 废除一段式：OBSOLETE_CR_PENDING →（通过）OBSOLETED 终态 /（驳回、撤回）APPROVE
        if (approved) {
            ErpProductDO product = erpProductMapper.selectById(id);
            if (product == null) {
                throw exception(PRODUCT_NOT_EXISTS);
            }
            if (!StrUtil.equals(processInstanceId, product.getProcessInstanceId())) {
                throw exception(PRODUCT_AUDIT_STATUS_ILLEGAL);
            }
            if (!ErpAuditStatus.OBSOLETE_CR_PENDING.getStatus().equals(product.getAuditStatus())) {
                log.warn("[completeObsoleteRequest] 忽略非废除审批态回调，id={}, auditStatus={}",
                        id, product.getAuditStatus());
                return;
            }
            // 废除生效：abolish_reason/abolish_by 已在发起时预写；此处落终态（flag+时间+status 停用+清流程实例）
            int count = erpProductMapper.update(null, new LambdaUpdateWrapper<ErpProductDO>()
                    .eq(ErpProductDO::getId, id)
                    .eq(ErpProductDO::getAuditStatus, ErpAuditStatus.OBSOLETE_CR_PENDING.getStatus())
                    .eq(ErpProductDO::getProcessInstanceId, processInstanceId)
                    .set(ErpProductDO::getAuditStatus, ErpAuditStatus.OBSOLETED.getStatus())
                    .set(ErpProductDO::getProcessInstanceId, null)
                    .set(ErpProductDO::getAbolishFlag, Boolean.TRUE)
                    .set(ErpProductDO::getAbolishTime, LocalDateTime.now())
                    .set(ErpProductDO::getStatus, CommonStatusEnum.DISABLE.getStatus()));
            if (count == 0) {
                throw exception(PRODUCT_AUDIT_STATUS_ILLEGAL);
            }
            log.info("[completeObsoleteRequest] 物料废除生效，id={}, reason={}", id, reason);
        } else {
            completeStageCallback(id, processInstanceId, ErpAuditStatus.OBSOLETE_CR_PENDING.getStatus(),
                    ErpAuditStatus.OBSOLETED.getStatus(), ErpAuditStatus.APPROVE.getStatus(), false, reason);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeStatusChange(Long id, String processInstanceId, boolean approved, String reason) {
        // 启停一段式：STOP_PENDING →（通过）目标 status 落主表 + 回 APPROVE /（驳回、撤回）回 APPROVE 状态不变
        if (approved) {
            ErpProductDO product = erpProductMapper.selectById(id);
            if (product == null) {
                throw exception(PRODUCT_NOT_EXISTS);
            }
            if (!StrUtil.equals(processInstanceId, product.getProcessInstanceId())) {
                throw exception(PRODUCT_AUDIT_STATUS_ILLEGAL);
            }
            if (!ErpAuditStatus.STOP_PENDING.getStatus().equals(product.getAuditStatus())) {
                log.warn("[completeStatusChange] 忽略非启停审批态回调，id={}, auditStatus={}",
                        id, product.getAuditStatus());
                return;
            }
            // 先业务后终态：目标 status 从暂存快照读取（提交启停审批时写入），落主表后才回 APPROVE
            boolean applied = pendingChangeService.applyPendingChange(id, processInstanceId, reason);
            if (!applied) {
                throw exception(PRODUCT_AUDIT_STATUS_ILLEGAL);
            }
        }
        completeStageCallback(id, processInstanceId, ErpAuditStatus.STOP_PENDING.getStatus(),
                ErpAuditStatus.APPROVE.getStatus(), ErpAuditStatus.APPROVE.getStatus(), approved, reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restoreProductAfterUpdateApproval(Long id, String processInstanceId) {
        restoreProductAfterUpdateApproval(id, processInstanceId, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restoreProductAfterCancel(Long id) {
        // 撤回：流程已终止，processInstanceId 必须清空，避免残留悬挂引用
        restoreProductAfterUpdateApproval(id, null, true);
    }

    private void restoreProductAfterUpdateApproval(Long id, String processInstanceId, boolean clearProcessInstance) {
        int count = erpProductMapper.update(null, new LambdaUpdateWrapper<ErpProductDO>()
                .eq(ErpProductDO::getId, id)
                .eq(ErpProductDO::getAuditStatus, ErpAuditStatus.PROCESS.getStatus())
                .set(ErpProductDO::getAuditStatus, ErpAuditStatus.APPROVE.getStatus())
                .set(ErpProductDO::getProcessInstanceId,
                        clearProcessInstance ? (String) null : processInstanceId));
        if (count == 0) {
            // 重复回调或状态已被并发处理：记录告警即可，主表已处于目标状态
            log.warn("[restoreProductAfterUpdateApproval] CAS 未命中，跳过，id={}, pi={}, clear={}", id, processInstanceId, clearProcessInstance);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restoreProductsAfterUpdateApproval(Collection<Long> ids, String processInstanceId) {
        restoreProducts(ids, processInstanceId, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restoreProductsAfterCancel(Collection<Long> ids) {
        restoreProducts(ids, null, true);
    }

    private void restoreProducts(Collection<Long> ids, String processInstanceId, boolean clearProcessInstance) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        int count = erpProductMapper.update(null, new LambdaUpdateWrapper<ErpProductDO>()
                .in(ErpProductDO::getId, ids)
                .eq(ErpProductDO::getAuditStatus, ErpAuditStatus.PROCESS.getStatus())
                .set(ErpProductDO::getAuditStatus, ErpAuditStatus.APPROVE.getStatus())
                .set(ErpProductDO::getProcessInstanceId,
                        clearProcessInstance ? (String) null : processInstanceId));
        if (count < ids.size()) {
            // 重复回调或部分物料状态已被并发处理：记录告警即可
            log.warn("[restoreProducts] CAS 部分未命中，expected={}, restored={}, pi={}, clear={}",
                    ids.size(), count, processInstanceId, clearProcessInstance);
        }
    }

    @Override
    public boolean isProductInApproval(Long id) {
        ErpProductDO product = erpProductMapper.selectById(id);
        return product != null && ErpAuditStatus.PROCESS.getStatus().equals(product.getAuditStatus());
    }

    private ErpProductDO validateProductExists(Long id) {
        ErpProductDO product = erpProductMapper.selectById(id);
        if (product == null) {
            throw exception(PRODUCT_NOT_EXISTS);
        }
        return product;
    }

    @Override
    public ErpProductDO getProduct(Long id) {
        return erpProductMapper.selectById(id);
    }

    @Override
    public List<ErpProductRespVO> getProductVOListByStatus(Integer status) {
        return getProductVOListByStatus(status, null);
    }

    @Override
    public List<ErpProductRespVO> getProductVOListByStatus(Integer status, String name) {
        List<ErpProductDO> list = erpProductMapper.selectListByStatus(status, name);
        return buildProductVOList(list);
    }

    @Override
    public List<ErpProductRespVO> getProductVOList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<ErpProductDO> list = erpProductMapper.selectByIds(ids);
        return buildProductVOList(list);
    }

    @Override
    public PageResult<ErpProductRespVO> getProductVOPage(ErpProductPageReqVO pageReqVO) {
        PageResult<ErpProductDO> pageResult = erpProductMapper.selectPage(pageReqVO);
        return new PageResult<>(buildProductVOList(pageResult.getList()), pageResult.getTotal());
    }

    @Resource
    private ErpProductPendingChangeService pendingChangeService;

    public List<ErpProductRespVO> buildProductVOList(List<ErpProductDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        Map<Long, ErpProductCategoryDO> categoryMap = productCategoryService.getProductCategoryMap(
                convertSet(list, ErpProductDO::getCategoryId));
        Map<Long, ErpProductUnitDO> unitMap = productUnitService.getProductUnitMap(
                convertSet(list, ErpProductDO::getUnitId));
        Map<Long, ErpProductCadenceDO> cadenceMap = convertMap(
                productCadenceMapper.selectListByProductIds(convertSet(list, ErpProductDO::getId)),
                ErpProductCadenceDO::getProductId);
        list.forEach(product -> ErpProductCadenceConverter.applyToProduct(cadenceMap.get(product.getId()), product));
        // 批量查询在途暂存的批次归组（撤回整批确认文案依赖），单条 UPDATE IN，防 N+1
        Map<Long, ErpProductPendingChangeDO> pendingMap = convertMap(
                pendingChangeService.getPendingChangesByProductIds(convertSet(list, ErpProductDO::getId)),
                ErpProductPendingChangeDO::getProductId);
        // 批量查询 BOM 引用状态（模式 B：避免循环内逐条查询）
        Set<Long> referencedIds = pendingChangeService.getReferencedProductIds(convertSet(list, ErpProductDO::getId));
        return BeanUtils.toBean(list, ErpProductRespVO.class, product -> {
            ErpProductPendingChangeDO pending = pendingMap.get(product.getId());
            if (pending != null && pending.getBatchId() != null) {
                product.setPendingBatchId(pending.getBatchId());
                product.setPendingBatchSize(
                        pendingChangeService.countPendingChangesByBatch(pending.getBatchId()).intValue());
            }
            MapUtils.findAndThen(categoryMap, product.getCategoryId(),
                    category -> product.setCategoryName(category.getName()));
            MapUtils.findAndThen(unitMap, product.getUnitId(),
                    unit -> {
                        product.setUnitName(unit.getName());
                        product.setQuantityPrecision(unit.getQuantityPrecision());
                    });
            product.setReferencedByBom(referencedIds.contains(product.getId()));
        });
    }

    private boolean hasCadenceChanges(Set<String> changedFields) {
        return changedFields.stream().anyMatch(field -> field.startsWith("cadence"));
    }

    @Override
    public Long getProductCountByCategoryId(Long categoryId) {
        return erpProductMapper.selectCountByCategoryId(categoryId);
    }

    @Override
    public Long getProductCountByUnitId(Long unitId) {
        return erpProductMapper.selectCountByUnitId(unitId);
    }

    /**
     * 校验产品记账单位必须是基本单位（unitId 为空时跳过，由 VO 必填校验兜底）
     */
    private void validateProductUnitIsBase(Long unitId) {
        if (unitId == null) {
            return;
        }
        cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductUnitDO unit = productUnitService.getProductUnit(unitId);
        if (unit != null && cn.weitee.erp.module.erp.enums.product.ErpProductUnitTypeEnum.AUXILIARY.getType()
                .equals(unit.getUnitType())) {
            throw exception(PRODUCT_UNIT_NOT_BASE, unit.getName());
        }
    }

    @Override
    public List<ErpProductSimpleRespVO> getApprovedProductSimpleList(String keyword) {
        // 精简白名单查询：万级物料下必须列裁剪 + 精简 VO，禁止改回 buildProductVOList 全字段构建
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ErpProductDO> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ErpProductDO>()
                        .select(ErpProductDO::getId, ErpProductDO::getName, ErpProductDO::getMaterialCode,
                                ErpProductDO::getPrevMaterialCode,
                                ErpProductDO::getCategoryId, ErpProductDO::getUnitId,
                                ErpProductDO::getStatus, ErpProductDO::getAuditStatus)
                        .eq(ErpProductDO::getAuditStatus, ErpAuditStatus.APPROVE.getStatus())
                        .eq(ErpProductDO::getStatus, CommonStatusEnum.ENABLE.getStatus());
        // 远程搜索：名称/编号模糊过滤，避免全量下发（万级物料下拉卡死）；编码沿革旧码兜底命中
        if (StrUtil.isNotBlank(keyword)) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(ErpProductDO::getName, kw)
                    .or().like(ErpProductDO::getMaterialCode, kw)
                    .or().like(ErpProductDO::getBarCode, kw)
                    .or().apply("EXISTS (SELECT 1 FROM erp_product_code_history h "
                            + "WHERE h.product_id = erp_product.id AND h.deleted = 0 "
                            + "AND h.old_code LIKE CONCAT('%', {0}, '%'))", kw));
        }
        List<ErpProductDO> list = erpProductMapper.selectList(wrapper);
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        Map<Long, ErpProductCategoryDO> categoryMap = productCategoryService.getProductCategoryMap(
                convertSet(list, ErpProductDO::getCategoryId));
        Map<Long, ErpProductUnitDO> unitMap = productUnitService.getProductUnitMap(
                convertSet(list, ErpProductDO::getUnitId));
        return list.stream().map(product -> {
            ErpProductSimpleRespVO vo = new ErpProductSimpleRespVO();
            vo.setId(product.getId());
            vo.setName(product.getName());
            vo.setMaterialCode(product.getMaterialCode());
            vo.setPrevMaterialCode(product.getPrevMaterialCode());
            vo.setCategoryId(product.getCategoryId());
            vo.setUnitId(product.getUnitId());
            vo.setStatus(product.getStatus());
            vo.setAuditStatus(product.getAuditStatus());
            MapUtils.findAndThen(categoryMap, product.getCategoryId(),
                    category -> vo.setCategoryName(category.getName()));
            MapUtils.findAndThen(unitMap, product.getUnitId(),
                    unit -> vo.setUnitName(unit.getName()));
            return vo;
        }).collect(java.util.stream.Collectors.toList());
    }

}
