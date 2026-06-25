package cn.weitee.erp.module.erp.service.mrp;

import cn.hutool.core.collection.CollUtil;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.suggest.ErpProductionSuggestConvertReqVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.suggest.ErpProductionSuggestPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.suggest.ErpPurchaseSuggestConvertReqVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.suggest.ErpPurchaseSuggestPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionSuggestDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpPurchaseSuggestDO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductDO;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionSuggestMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpPurchaseSuggestMapper;
import cn.weitee.erp.module.erp.enums.mrp.ErpMrpSuggestStatusEnum;
import cn.weitee.erp.module.erp.service.finance.ErpAccountService;
import cn.weitee.erp.module.erp.service.product.ErpProductService;
import cn.weitee.erp.module.erp.service.project.ErpProjectRoleTaskService;
import cn.weitee.erp.module.erp.service.purchase.ErpPurchaseOrderService;
import cn.weitee.erp.module.erp.service.purchase.ErpSupplierService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.MRP_SUGGEST_STATUS_INVALID;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCTION_SUGGEST_NOT_EXISTS;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PURCHASE_SUGGEST_NOT_EXISTS;

@Service
@Validated
public class ErpMrpSuggestServiceImpl implements ErpMrpSuggestService {

    @Resource
    private ErpPurchaseSuggestMapper erpPurchaseSuggestMapper;
    @Resource
    private ErpProductionSuggestMapper erpProductionSuggestMapper;
    @Resource
    private ErpSupplierService supplierService;
    @Resource
    private ErpAccountService accountService;
    @Resource
    private ErpProductService productService;
    @Resource
    private ErpPurchaseOrderService purchaseOrderService;
    @Resource
    private ErpProductionOrderService productionOrderService;
    @Resource
    private ErpProjectRoleTaskService projectRoleTaskService;

    @Override
    public PageResult<ErpPurchaseSuggestDO> getPurchaseSuggestPage(ErpPurchaseSuggestPageReqVO pageReqVO) {
        return erpPurchaseSuggestMapper.selectPage(pageReqVO);
    }

    @Override
    public PageResult<ErpProductionSuggestDO> getProductionSuggestPage(ErpProductionSuggestPageReqVO pageReqVO) {
        return erpProductionSuggestMapper.selectPage(pageReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmPurchaseSuggest(List<Long> ids) {
        List<ErpPurchaseSuggestDO> suggests = validatePurchaseSuggestsForApprove(ids);
        suggests.forEach(suggest ->
                erpPurchaseSuggestMapper.updateById(new ErpPurchaseSuggestDO().setId(suggest.getId())
                        .setStatus(ErpMrpSuggestStatusEnum.CONFIRMED.getStatus())));
        refreshMcTaskAfterSuggestChange(extractProjectIds(suggests));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmProductionSuggest(List<Long> ids) {
        List<ErpProductionSuggestDO> suggests = validateProductionSuggestsForApprove(ids);
        suggests.forEach(suggest ->
                erpProductionSuggestMapper.updateById(new ErpProductionSuggestDO().setId(suggest.getId())
                        .setStatus(ErpMrpSuggestStatusEnum.CONFIRMED.getStatus())));
        refreshMcTaskAfterSuggestChange(extractProjectIdsFromProduction(suggests));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectPurchaseSuggest(List<Long> ids) {
        List<ErpPurchaseSuggestDO> suggests = validatePurchaseSuggestsForReject(ids);
        suggests.forEach(suggest ->
                erpPurchaseSuggestMapper.updateById(new ErpPurchaseSuggestDO().setId(suggest.getId())
                        .setStatus(ErpMrpSuggestStatusEnum.REJECTED.getStatus())));
        refreshMcTaskAfterSuggestChange(extractProjectIds(suggests));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectProductionSuggest(List<Long> ids) {
        List<ErpProductionSuggestDO> suggests = validateProductionSuggestsForReject(ids);
        suggests.forEach(suggest ->
                erpProductionSuggestMapper.updateById(new ErpProductionSuggestDO().setId(suggest.getId())
                        .setStatus(ErpMrpSuggestStatusEnum.REJECTED.getStatus())));
        refreshMcTaskAfterSuggestChange(extractProjectIdsFromProduction(suggests));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long convertPurchaseSuggest(ErpPurchaseSuggestConvertReqVO reqVO) {
        List<ErpPurchaseSuggestDO> suggests = validatePurchaseSuggestsForConvert(reqVO.getIds());
        int lockedCount = erpPurchaseSuggestMapper.updateStatusByIdsAndStatus(reqVO.getIds(),
                ErpMrpSuggestStatusEnum.CONFIRMED.getStatus(),
                new ErpPurchaseSuggestDO().setStatus(ErpMrpSuggestStatusEnum.CONVERTED.getStatus()));
        if (lockedCount != suggests.size()) {
            throw exception(MRP_SUGGEST_STATUS_INVALID);
        }
        supplierService.validateSupplier(reqVO.getSupplierId());
        if (reqVO.getAccountId() != null) {
            accountService.validateAccount(reqVO.getAccountId());
        }
        Map<PurchaseConvertKey, BigDecimal> qtyMap = new LinkedHashMap<>();
        suggests.forEach(suggest -> qtyMap.merge(new PurchaseConvertKey(suggest.getProjectId(), suggest.getMaterialId()),
                suggest.getSuggestQty(), BigDecimal::add));
        Map<Long, ErpProductDO> productMap = convertMap(productService.validProductList(
                qtyMap.keySet().stream().map(PurchaseConvertKey::getMaterialId).toList()), ErpProductDO::getId);
        List<ErpPurchaseOrderSaveReqVO.Item> items = new ArrayList<>();
        qtyMap.forEach((key, qty) -> {
            ErpProductDO product = productMap.get(key.getMaterialId());
            ErpPurchaseOrderSaveReqVO.Item item = new ErpPurchaseOrderSaveReqVO.Item();
            item.setProductId(key.getMaterialId());
            item.setProjectId(key.getProjectId());
            item.setProductUnitId(product.getUnitId());
            item.setProductPrice(product.getPurchasePrice());
            item.setCount(qty);
            item.setTaxPercent(BigDecimal.ZERO);
            item.setRemark(reqVO.getRemark());
            items.add(item);
        });
        ErpPurchaseOrderSaveReqVO createReqVO = new ErpPurchaseOrderSaveReqVO();
        createReqVO.setSupplierId(reqVO.getSupplierId());
        createReqVO.setAccountId(reqVO.getAccountId());
        createReqVO.setOrderTime(LocalDateTime.now());
        createReqVO.setDiscountPercent(BigDecimal.ZERO);
        createReqVO.setDepositPrice(BigDecimal.ZERO);
        createReqVO.setRemark(reqVO.getRemark());
        createReqVO.setItems(items);
        Long purchaseOrderId = purchaseOrderService.createPurchaseOrder(createReqVO);
        suggests.forEach(suggest -> erpPurchaseSuggestMapper.updateById(new ErpPurchaseSuggestDO().setId(suggest.getId())
                .setConvertPurchaseOrderId(purchaseOrderId)));
        refreshMcTaskAfterSuggestChange(extractProjectIds(suggests));
        return purchaseOrderId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Long> convertProductionSuggest(ErpProductionSuggestConvertReqVO reqVO) {
        List<ErpProductionSuggestDO> suggests = validateProductionSuggestsForConvert(reqVO.getIds());
        int lockedCount = erpProductionSuggestMapper.updateStatusByIdsAndStatus(reqVO.getIds(),
                ErpMrpSuggestStatusEnum.CONFIRMED.getStatus(),
                new ErpProductionSuggestDO().setStatus(ErpMrpSuggestStatusEnum.CONVERTED.getStatus()));
        if (lockedCount != suggests.size()) {
            throw exception(MRP_SUGGEST_STATUS_INVALID);
        }
        List<Long> orderIds = new ArrayList<>(suggests.size());
        for (ErpProductionSuggestDO suggest : suggests) {
            Long orderId = productionOrderService.createProductionOrderBySuggest(suggest, reqVO.getRemark());
            orderIds.add(orderId);
            erpProductionSuggestMapper.updateById(new ErpProductionSuggestDO().setId(suggest.getId())
                    .setConvertProductionOrderId(orderId));
        }
        refreshMcTaskAfterSuggestChange(extractProjectIdsFromProduction(suggests));
        return orderIds;
    }

    @Override
    public List<ErpPurchaseSuggestDO> getPurchaseSuggestListByConvertPurchaseOrderIds(Collection<Long> convertPurchaseOrderIds) {
        if (CollUtil.isEmpty(convertPurchaseOrderIds)) {
            return Collections.emptyList();
        }
        return erpPurchaseSuggestMapper.selectListByConvertPurchaseOrderIds(convertPurchaseOrderIds);
    }

    private List<ErpPurchaseSuggestDO> validatePurchaseSuggestsForApprove(List<Long> ids) {
        List<ErpPurchaseSuggestDO> suggests = erpPurchaseSuggestMapper.selectListByIds(ids);
        if (CollUtil.isEmpty(suggests) || suggests.size() != ids.size()) {
            throw exception(PURCHASE_SUGGEST_NOT_EXISTS);
        }
        suggests.forEach(suggest -> {
            if (!Objects.equals(suggest.getStatus(), ErpMrpSuggestStatusEnum.TO_CONFIRM.getStatus())) {
                throw exception(MRP_SUGGEST_STATUS_INVALID);
            }
        });
        return suggests;
    }

    private List<ErpPurchaseSuggestDO> validatePurchaseSuggestsForReject(List<Long> ids) {
        List<ErpPurchaseSuggestDO> suggests = erpPurchaseSuggestMapper.selectListByIds(ids);
        if (CollUtil.isEmpty(suggests) || suggests.size() != ids.size()) {
            throw exception(PURCHASE_SUGGEST_NOT_EXISTS);
        }
        suggests.forEach(suggest -> {
            if (Objects.equals(suggest.getStatus(), ErpMrpSuggestStatusEnum.CONVERTED.getStatus())
                    || Objects.equals(suggest.getStatus(), ErpMrpSuggestStatusEnum.REJECTED.getStatus())) {
                throw exception(MRP_SUGGEST_STATUS_INVALID);
            }
        });
        return suggests;
    }

    private List<ErpPurchaseSuggestDO> validatePurchaseSuggestsForConvert(List<Long> ids) {
        List<ErpPurchaseSuggestDO> suggests = erpPurchaseSuggestMapper.selectListByIds(ids);
        if (CollUtil.isEmpty(suggests) || suggests.size() != ids.size()) {
            throw exception(PURCHASE_SUGGEST_NOT_EXISTS);
        }
        suggests.forEach(suggest -> {
            if (!Objects.equals(suggest.getStatus(), ErpMrpSuggestStatusEnum.CONFIRMED.getStatus())) {
                throw exception(MRP_SUGGEST_STATUS_INVALID);
            }
        });
        return suggests;
    }

    private List<ErpProductionSuggestDO> validateProductionSuggestsForApprove(List<Long> ids) {
        List<ErpProductionSuggestDO> suggests = erpProductionSuggestMapper.selectListByIds(ids);
        if (CollUtil.isEmpty(suggests) || suggests.size() != ids.size()) {
            throw exception(PRODUCTION_SUGGEST_NOT_EXISTS);
        }
        suggests.forEach(suggest -> {
            if (!Objects.equals(suggest.getStatus(), ErpMrpSuggestStatusEnum.TO_CONFIRM.getStatus())) {
                throw exception(MRP_SUGGEST_STATUS_INVALID);
            }
        });
        return suggests;
    }

    private List<ErpProductionSuggestDO> validateProductionSuggestsForReject(List<Long> ids) {
        List<ErpProductionSuggestDO> suggests = erpProductionSuggestMapper.selectListByIds(ids);
        if (CollUtil.isEmpty(suggests) || suggests.size() != ids.size()) {
            throw exception(PRODUCTION_SUGGEST_NOT_EXISTS);
        }
        suggests.forEach(suggest -> {
            if (Objects.equals(suggest.getStatus(), ErpMrpSuggestStatusEnum.CONVERTED.getStatus())
                    || Objects.equals(suggest.getStatus(), ErpMrpSuggestStatusEnum.REJECTED.getStatus())) {
                throw exception(MRP_SUGGEST_STATUS_INVALID);
            }
        });
        return suggests;
    }

    private List<ErpProductionSuggestDO> validateProductionSuggestsForConvert(List<Long> ids) {
        List<ErpProductionSuggestDO> suggests = erpProductionSuggestMapper.selectListByIds(ids);
        if (CollUtil.isEmpty(suggests) || suggests.size() != ids.size()) {
            throw exception(PRODUCTION_SUGGEST_NOT_EXISTS);
        }
        suggests.forEach(suggest -> {
            if (!Objects.equals(suggest.getStatus(), ErpMrpSuggestStatusEnum.CONFIRMED.getStatus())) {
                throw exception(MRP_SUGGEST_STATUS_INVALID);
            }
        });
        return suggests;
    }

    private static class PurchaseConvertKey {
        private final Long projectId;
        private final Long materialId;

        private PurchaseConvertKey(Long projectId, Long materialId) {
            this.projectId = projectId;
            this.materialId = materialId;
        }

        private Long getProjectId() {
            return projectId;
        }

        private Long getMaterialId() {
            return materialId;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (!(object instanceof PurchaseConvertKey that)) {
                return false;
            }
            return Objects.equals(projectId, that.projectId) && Objects.equals(materialId, that.materialId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(projectId, materialId);
        }
    }

    private List<Long> extractProjectIds(List<ErpPurchaseSuggestDO> suggests) {
        return suggests.stream()
                .map(ErpPurchaseSuggestDO::getProjectId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private List<Long> extractProjectIdsFromProduction(List<ErpProductionSuggestDO> suggests) {
        return suggests.stream()
                .map(ErpProductionSuggestDO::getProjectId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private void refreshMcTaskAfterSuggestChange(List<Long> projectIds) {
        projectIds.forEach(projectId -> {
            long pendingPurchaseCount = erpPurchaseSuggestMapper.selectCountByProjectIdAndStatus(projectId,
                    ErpMrpSuggestStatusEnum.TO_CONFIRM.getStatus());
            long pendingProductionCount = erpProductionSuggestMapper.selectCountByProjectIdAndStatus(projectId,
                    ErpMrpSuggestStatusEnum.TO_CONFIRM.getStatus());
            if (pendingPurchaseCount > 0 || pendingProductionCount > 0) {
                projectRoleTaskService.createOrRefreshMcTask(projectId, null, "MRP 建议仍待 MC 处理");
                return;
            }
            projectRoleTaskService.completeMcTask(projectId, "MRP 建议已处理完成");
        });
    }

}
