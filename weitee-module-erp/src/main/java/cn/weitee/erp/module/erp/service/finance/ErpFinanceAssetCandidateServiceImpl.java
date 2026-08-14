package cn.weitee.erp.module.erp.service.finance;

import cn.hutool.core.collection.CollUtil;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.asset.ErpFinanceAssetCandidateConfirmReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.asset.ErpFinanceAssetCandidatePageReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.asset.ErpFinanceAssetSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceAssetCandidateDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceExpenseDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceExpenseItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInItemDO;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceAssetMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceAssetCandidateMapper;
import cn.weitee.erp.module.erp.enums.ErpFinanceAssetCandidateStatusEnum;
import cn.weitee.erp.module.erp.enums.ErpFinanceAssetSourceTypeEnum;
import cn.weitee.erp.module.erp.enums.ErpFinanceExpenseAccountingTypeEnum;
import cn.weitee.erp.module.erp.enums.ErpFinanceExpenseTypeEnum;
import cn.weitee.erp.module.erp.service.product.ErpProductService;
import cn.weitee.erp.module.erp.service.purchase.ErpPurchaseInService;
import cn.weitee.erp.module.erp.service.finance.interceptor.FinancePermissionScope;
import cn.weitee.erp.module.erp.service.finance.interceptor.FinanceDataPermissionContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.framework.common.exception.enums.GlobalErrorCodeConstants.FORBIDDEN;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsFinanceAsset.ASSET_CANDIDATE_CONFIRM_FAIL;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsFinanceAsset.ASSET_CANDIDATE_NOT_EXISTS;

@Service
@Validated
public class ErpFinanceAssetCandidateServiceImpl implements ErpFinanceAssetCandidateService {

    @Resource
    private ErpFinanceAssetCandidateMapper financeAssetCandidateMapper;
    @Resource
    private ErpFinanceAssetMapper financeAssetMapper;
    @Resource
    private ErpFinanceAssetService financeAssetService;
    @Resource
    private ErpFinanceExpenseService financeExpenseService;
    @Resource
    private ErpPurchaseInService purchaseInService;
    @Resource
    private ErpProductService productService;
    @Resource
    private FinanceDataPermissionService financeDataPermissionService;

    @Override
    public PageResult<ErpFinanceAssetCandidateDO> getFinanceAssetCandidatePage(ErpFinanceAssetCandidatePageReqVO pageReqVO) {
        FinancePermissionScope.Scope<Long> deptScope = financeDataPermissionService.getPermissionScope().deptScope();
        if (deptScope.mode() == FinancePermissionScope.ScopeMode.NONE) {
            return PageResult.empty(0L);
        }
        if (deptScope.mode() == FinancePermissionScope.ScopeMode.LIMITED) {
            return financeAssetCandidateMapper.selectPageByDeptIds(pageReqVO, deptScope.values());
        }
        return financeAssetCandidateMapper.selectPage(pageReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long confirmFinanceAssetCandidate(ErpFinanceAssetCandidateConfirmReqVO reqVO) {
        ErpFinanceAssetCandidateDO candidate = validateCandidateExists(reqVO.getCandidateId());
        validateDeptAccess(candidate.getDeptId());
        if (!ErpFinanceAssetCandidateStatusEnum.PENDING_CONFIRM.getStatus().equals(candidate.getStatus())) {
            throw exception(ASSET_CANDIDATE_CONFIRM_FAIL);
        }
        if (financeAssetMapper.selectByCandidateId(candidate.getId()) != null) {
            throw exception(ASSET_CANDIDATE_CONFIRM_FAIL);
        }
        int updated = financeAssetCandidateMapper.updateByIdAndStatus(candidate.getId(),
                ErpFinanceAssetCandidateStatusEnum.PENDING_CONFIRM.getStatus(),
                new ErpFinanceAssetCandidateDO()
                        .setId(candidate.getId())
                        .setStatus(ErpFinanceAssetCandidateStatusEnum.CONFIRMED.getStatus())
                        .setRemark(reqVO.getRemark()));
        if (updated == 0) {
            throw exception(ASSET_CANDIDATE_CONFIRM_FAIL);
        }
        ErpFinanceAssetSaveReqVO saveReqVO = BeanUtils.toBean(reqVO, ErpFinanceAssetSaveReqVO.class, in -> in
                .setCandidateId(candidate.getId())
                .setSourceType(candidate.getSourceType() == null
                        ? ErpFinanceAssetSourceTypeEnum.MANUAL.getType() : candidate.getSourceType())
                .setSourceBizId(candidate.getSourceBizId())
                .setSourceBizNo(candidate.getSourceBizNo())
                .setSourceItemId(candidate.getSourceItemId()));
        return financeAssetService.createFinanceAsset(saveReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCandidateFromExpense(Long expenseId) {
        ErpFinanceExpenseDO expense = financeExpenseService.getFinanceExpense(expenseId);
        if (expense == null) {
            return null;
        }
        ErpFinanceExpenseTypeEnum expenseType = ErpFinanceExpenseTypeEnum.fromType(expense.getExpenseType());
        boolean pettyPurchase = expenseType == ErpFinanceExpenseTypeEnum.PETTY_PURCHASE;
        boolean researchCapitalize = expenseType == ErpFinanceExpenseTypeEnum.RESEARCH
                && ErpFinanceExpenseAccountingTypeEnum.CAPITALIZE.getType().equals(expense.getRdAccountingType());
        if (!pettyPurchase && !researchCapitalize) {
            return null;
        }
        List<ErpFinanceExpenseItemDO> items = financeExpenseService.getFinanceExpenseItemListByExpenseId(expenseId);
        Long createdId = null;
        if (CollUtil.isEmpty(items)) {
            if (financeAssetCandidateMapper.selectBySource(ErpFinanceAssetSourceTypeEnum.FINANCE_EXPENSE.getType(), expenseId, 0L) != null) {
                return null;
            }
            String defaultAssetName = researchCapitalize ? "研发资本化资产" : "零星采购资产";
            ErpFinanceAssetCandidateDO candidate = buildExpenseCandidate(expense, null, 0L,
                    defaultAssetName, defaultAmount(expense.getExpensePrice()));
            financeAssetCandidateMapper.insert(candidate);
            return candidate.getId();
        }
        Map<Long, ErpFinanceAssetCandidateDO> existedCandidateMap = convertMap(
                financeAssetCandidateMapper.selectListBySource(ErpFinanceAssetSourceTypeEnum.FINANCE_EXPENSE.getType(), expenseId),
                candidate -> candidate.getSourceItemId() == null ? 0L : candidate.getSourceItemId());
        for (ErpFinanceExpenseItemDO item : items) {
            if (!isExpenseItemAssetCandidate(item)) {
                continue;
            }
            Long sourceItemId = item.getId() == null ? 0L : item.getId();
            if (existedCandidateMap.containsKey(sourceItemId)) {
                continue;
            }
            ErpFinanceAssetCandidateDO candidate = buildExpenseCandidate(expense, item, sourceItemId,
                    item.getItemName(), defaultAmount(item.getAmount()));
            financeAssetCandidateMapper.insert(candidate);
            createdId = candidate.getId();
        }
        return createdId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCandidateFromPurchaseIn(Long purchaseInId) {
        ErpPurchaseInDO purchaseIn = purchaseInService.getPurchaseIn(purchaseInId);
        if (purchaseIn == null) {
            return null;
        }
        List<ErpPurchaseInItemDO> items = purchaseInService.getPurchaseInItemListByInId(purchaseInId);
        Long createdId = null;
        if (CollUtil.isEmpty(items)) {
            if (financeAssetCandidateMapper.selectBySource(ErpFinanceAssetSourceTypeEnum.PURCHASE_IN.getType(), purchaseInId, 0L) != null) {
                return null;
            }
            ErpFinanceAssetCandidateDO candidate = new ErpFinanceAssetCandidateDO()
                    .setSourceType(ErpFinanceAssetSourceTypeEnum.PURCHASE_IN.getType())
                    .setSourceBizId(purchaseInId)
                    .setSourceBizNo(purchaseIn.getNo())
                    .setSourceItemId(0L)
                    .setAssetName("采购入库资产")
                    .setCategoryName("采购设备")
                    .setAmount(defaultAmount(purchaseIn.getTotalPrice()))
                    .setPurchaseDate(resolveLocalDate(purchaseIn.getInTime()))
                    .setStatus(ErpFinanceAssetCandidateStatusEnum.PENDING_CONFIRM.getStatus())
                    .setRemark(purchaseIn.getRemark());
            financeAssetCandidateMapper.insert(candidate);
            return candidate.getId();
        }
        var productMap = productService.getProductVOMap(items.stream().map(ErpPurchaseInItemDO::getProductId).toList());
        Map<Long, ErpFinanceAssetCandidateDO> existedCandidateMap = convertMap(
                financeAssetCandidateMapper.selectListBySource(ErpFinanceAssetSourceTypeEnum.PURCHASE_IN.getType(), purchaseInId),
                candidate -> candidate.getSourceItemId() == null ? 0L : candidate.getSourceItemId());
        for (ErpPurchaseInItemDO item : items) {
            Long sourceItemId = item.getId() == null ? 0L : item.getId();
            if (existedCandidateMap.containsKey(sourceItemId)) {
                continue;
            }
            var product = productMap.get(item.getProductId());
            if (product == null || !isProductAssetCandidate(product.getAssetFlag(), product.getCategoryName(), product.getName())) {
                continue;
            }
            ErpFinanceAssetCandidateDO candidate = new ErpFinanceAssetCandidateDO()
                    .setSourceType(ErpFinanceAssetSourceTypeEnum.PURCHASE_IN.getType())
                    .setSourceBizId(purchaseInId)
                    .setSourceBizNo(purchaseIn.getNo())
                    .setSourceItemId(sourceItemId)
                    .setProductId(item.getProductId())
                    .setAssetName(product.getName())
                    .setCategoryName(product.getCategoryName())
                    .setAmount(defaultAmount(item.getTotalPrice()))
                    .setPurchaseDate(resolveLocalDate(purchaseIn.getInTime()))
                    .setStatus(ErpFinanceAssetCandidateStatusEnum.PENDING_CONFIRM.getStatus())
                    .setRemark(item.getRemark());
            financeAssetCandidateMapper.insert(candidate);
            createdId = candidate.getId();
        }
        return createdId;
    }

    private ErpFinanceAssetCandidateDO validateCandidateExists(Long id) {
        ErpFinanceAssetCandidateDO candidate = financeAssetCandidateMapper.selectById(id);
        if (candidate == null) {
            throw exception(ASSET_CANDIDATE_NOT_EXISTS);
        }
        return candidate;
    }

    private void validateDeptAccess(Long deptId) {
        FinancePermissionScope permissionScope = FinanceDataPermissionContext.getPermissionScope();
        if (permissionScope == null || permissionScope.deptScope().mode() == FinancePermissionScope.ScopeMode.ALL) {
            return;
        }
        if (deptId == null || !permissionScope.deptScope().values().contains(deptId)) {
            throw exception(FORBIDDEN);
        }
    }

    private LocalDate resolveLocalDate(java.time.LocalDateTime value) {
        return value == null ? null : value.toLocalDate();
    }

    private BigDecimal defaultAmount(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    private ErpFinanceAssetCandidateDO buildExpenseCandidate(ErpFinanceExpenseDO expense, ErpFinanceExpenseItemDO item,
                                                             Long sourceItemId, String assetName, BigDecimal amount) {
        return new ErpFinanceAssetCandidateDO()
                .setSourceType(ErpFinanceAssetSourceTypeEnum.FINANCE_EXPENSE.getType())
                .setSourceBizId(expense.getId())
                .setSourceBizNo(expense.getNo())
                .setSourceItemId(sourceItemId)
                .setAssetName(assetName)
                .setCategoryName(resolveExpenseCandidateCategory(expense))
                .setAmount(amount)
                .setPurchaseDate(resolveLocalDate(expense.getExpenseTime()))
                .setDeptId(expense.getDeptId())
                .setResponsibleUserId(expense.getFinanceUserId())
                .setStatus(ErpFinanceAssetCandidateStatusEnum.PENDING_CONFIRM.getStatus())
                .setRemark(item != null ? item.getRemark() : expense.getRemark());
    }

    private boolean isExpenseItemAssetCandidate(ErpFinanceExpenseItemDO item) {
        if (Boolean.TRUE.equals(item.getAssetCandidateFlag())) {
            return true;
        }
        String name = item.getItemName() == null ? "" : item.getItemName();
        String remark = item.getRemark() == null ? "" : item.getRemark();
        return containsAssetKeyword(name) || containsAssetKeyword(remark);
    }

    private boolean isProductAssetCandidate(Boolean assetFlag, String categoryName, String productName) {
        if (Boolean.TRUE.equals(assetFlag)) {
            return true;
        }
        return containsAssetKeyword(categoryName) || containsAssetKeyword(productName);
    }

    private boolean containsAssetKeyword(String value) {
        if (value == null) {
            return false;
        }
        return value.contains("设备") || value.contains("电脑") || value.contains("仪器")
                || value.contains("机器") || value.contains("资产");
    }

    private String resolveExpenseCandidateCategory(ErpFinanceExpenseDO expense) {
        if (ErpFinanceExpenseTypeEnum.RESEARCH.getType().equals(expense.getExpenseType())
                && ErpFinanceExpenseAccountingTypeEnum.CAPITALIZE.getType().equals(expense.getRdAccountingType())) {
            return "研发资本化";
        }
        return "零星采购";
    }
}
