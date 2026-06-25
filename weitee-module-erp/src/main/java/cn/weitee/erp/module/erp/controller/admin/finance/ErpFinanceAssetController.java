package cn.weitee.erp.module.erp.controller.admin.finance;

import cn.hutool.core.collection.CollUtil;
import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.collection.MapUtils;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.asset.ErpFinanceAssetPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.asset.ErpFinanceAssetCandidateRespVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.asset.ErpFinanceAssetRespVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.asset.ErpFinanceAssetSaveReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.asset.ErpFinanceAssetStatusReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.asset.ErpFinanceAssetTraceRespVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.expense.ErpFinanceExpenseItemRespVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.expense.ErpFinanceExpenseRespVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceAssetDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceAssetCandidateDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpAccountDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceExpenseDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceExpenseItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpSupplierDO;
import cn.weitee.erp.module.erp.dal.dataobject.project.ErpProjectDO;
import cn.weitee.erp.module.erp.enums.ErpFinanceExpenseTypeEnum;
import cn.weitee.erp.module.erp.enums.ErpResearchExpenseCategoryEnum;
import cn.weitee.erp.module.erp.enums.ErpFinanceAssetSourceTypeEnum;
import cn.weitee.erp.module.erp.service.finance.ErpAccountService;
import cn.weitee.erp.module.erp.service.finance.ErpFinanceAssetService;
import cn.weitee.erp.module.erp.service.finance.ErpFinanceExpenseService;
import cn.weitee.erp.module.erp.service.project.ErpProjectService;
import cn.weitee.erp.module.erp.service.purchase.ErpPurchaseInService;
import cn.weitee.erp.module.erp.service.purchase.ErpSupplierService;
import cn.weitee.erp.module.system.api.dept.DeptApi;
import cn.weitee.erp.module.system.api.dept.dto.DeptRespDTO;
import cn.weitee.erp.module.system.api.user.AdminUserApi;
import cn.weitee.erp.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static cn.weitee.erp.framework.common.pojo.CommonResult.success;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertListByFlatMap;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.weitee.erp.module.erp.util.ErpUserIdUtils.parseUserId;

@Tag(name = "管理后台 - ERP 固定资产台账")
@RestController
@RequestMapping("/erp/finance-asset")
@Validated
public class ErpFinanceAssetController {

    @Resource
    private ErpFinanceAssetService financeAssetService;
    @Resource
    private ErpFinanceExpenseService financeExpenseService;
    @Resource
    private ErpPurchaseInService purchaseInService;
    @Resource
    private ErpSupplierService supplierService;
    @Resource
    private ErpAccountService accountService;
    @Resource
    private DeptApi deptApi;
    @Resource
    private ErpProjectService projectService;
    @Resource
    private AdminUserApi adminUserApi;

    @PostMapping("/create")
    @Operation(summary = "创建固定资产")
    @PreAuthorize("@ss.hasPermission('erp:finance-asset:create')")
    public CommonResult<Long> createFinanceAsset(@Valid @RequestBody ErpFinanceAssetSaveReqVO createReqVO) {
        return success(financeAssetService.createFinanceAsset(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新固定资产")
    @PreAuthorize("@ss.hasPermission('erp:finance-asset:update')")
    public CommonResult<Boolean> updateFinanceAsset(@Valid @RequestBody ErpFinanceAssetSaveReqVO updateReqVO) {
        financeAssetService.updateFinanceAsset(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除固定资产")
    @PreAuthorize("@ss.hasPermission('erp:finance-asset:delete')")
    public CommonResult<Boolean> deleteFinanceAsset(@RequestParam("ids") List<Long> ids) {
        financeAssetService.deleteFinanceAsset(ids);
        return success(true);
    }

    @PutMapping("/update-status")
    @Operation(summary = "更新固定资产状态")
    @PreAuthorize("@ss.hasPermission('erp:finance-asset:update-status')")
    public CommonResult<Boolean> updateFinanceAssetStatus(@Valid @RequestBody ErpFinanceAssetStatusReqVO reqVO) {
        financeAssetService.updateFinanceAssetStatus(reqVO.getId(), reqVO.getStatus());
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得固定资产")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('erp:finance-asset:query')")
    public CommonResult<ErpFinanceAssetRespVO> getFinanceAsset(@RequestParam("id") Long id) {
        ErpFinanceAssetDO asset = financeAssetService.getFinanceAsset(id);
        return success(asset == null ? null : BeanUtils.toBean(asset, ErpFinanceAssetRespVO.class));
    }

    @GetMapping("/get-trace")
    @Operation(summary = "获得固定资产追溯")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('erp:finance-asset:query')")
    public CommonResult<ErpFinanceAssetTraceRespVO> getFinanceAssetTrace(@RequestParam("id") Long id) {
        ErpFinanceAssetDO asset = financeAssetService.getFinanceAsset(id);
        if (asset == null) {
            return success(null);
        }

        ErpFinanceAssetTraceRespVO traceRespVO = new ErpFinanceAssetTraceRespVO();
        traceRespVO.setAsset(BeanUtils.toBean(asset, ErpFinanceAssetRespVO.class));
        traceRespVO.setCandidate(buildCandidateTrace(asset));

        if (ErpFinanceAssetSourceTypeEnum.PURCHASE_IN.getType().equals(asset.getSourceType())) {
            traceRespVO.setPurchaseIn(buildPurchaseInTrace(asset.getSourceBizId(), asset.getSourceItemId()));
        } else if (ErpFinanceAssetSourceTypeEnum.FINANCE_EXPENSE.getType().equals(asset.getSourceType())) {
            traceRespVO.setExpense(buildExpenseTrace(asset.getSourceBizId(), asset.getSourceItemId()));
        }
        return success(traceRespVO);
    }

    @GetMapping("/page")
    @Operation(summary = "获得固定资产分页")
    @PreAuthorize("@ss.hasPermission('erp:finance-asset:query')")
    public CommonResult<PageResult<ErpFinanceAssetRespVO>> getFinanceAssetPage(@Valid ErpFinanceAssetPageReqVO pageReqVO) {
        return success(BeanUtils.toBean(financeAssetService.getFinanceAssetPage(pageReqVO), ErpFinanceAssetRespVO.class));
    }

    private ErpFinanceAssetCandidateRespVO buildCandidateTrace(ErpFinanceAssetDO asset) {
        ErpFinanceAssetCandidateDO candidate = financeAssetService.getCandidateByAssetSource(
                asset.getCandidateId(), asset.getSourceType(), asset.getSourceBizId(),
                asset.getSourceItemId(), asset.getSourceBizNo(), asset.getName());
        return candidate == null ? null : BeanUtils.toBean(candidate, ErpFinanceAssetCandidateRespVO.class);
    }

    private ErpFinanceAssetTraceRespVO.PurchaseInSource buildPurchaseInTrace(Long purchaseInId, Long sourceItemId) {
        if (purchaseInId == null) {
            return null;
        }
        ErpPurchaseInDO purchaseIn = purchaseInService.getPurchaseIn(purchaseInId);
        if (purchaseIn == null) {
            return null;
        }
        List<ErpPurchaseInItemDO> items = purchaseInService.getPurchaseInItemListByInId(purchaseInId);
        Map<Long, ErpSupplierDO> supplierMap = purchaseIn.getSupplierId() == null
                ? Collections.emptyMap()
                : supplierService.getSupplierMap(Collections.singleton(purchaseIn.getSupplierId()));
        var source = BeanUtils.toBean(purchaseIn, ErpFinanceAssetTraceRespVO.PurchaseInSource.class);
        MapUtils.findAndThen(supplierMap, purchaseIn.getSupplierId(), supplier -> source.setSupplierName(supplier.getName()));
        source.setItems(BeanUtils.toBean(items, ErpFinanceAssetTraceRespVO.PurchaseInItem.class));
        source.setMatchedItemId(resolveMatchedItemId(items, sourceItemId));
        markMatchedPurchaseInItems(source.getItems(), source.getMatchedItemId());
        if (CollUtil.isNotEmpty(source.getItems())) {
            source.setProductNames(CollUtil.join(source.getItems(), ", ", ErpFinanceAssetTraceRespVO.PurchaseInItem::getProductName));
        }
        return source;
    }

    private ErpFinanceAssetTraceRespVO.ExpenseSource buildExpenseTrace(Long expenseId, Long sourceItemId) {
        if (expenseId == null) {
            return null;
        }
        ErpFinanceExpenseDO expense = financeExpenseService.getFinanceExpense(expenseId);
        if (expense == null) {
            return null;
        }
        List<ErpFinanceExpenseItemDO> items = financeExpenseService.getFinanceExpenseItemListByExpenseId(expenseId);
        ErpFinanceExpenseRespVO expenseRespVO = buildExpenseRespVO(expense, items);
        var source = BeanUtils.toBean(expenseRespVO, ErpFinanceAssetTraceRespVO.ExpenseSource.class);
        source.setItems(BeanUtils.toBean(expenseRespVO.getItems(), ErpFinanceAssetTraceRespVO.ExpenseItem.class));
        source.setMatchedItemId(resolveMatchedItemId(items, sourceItemId));
        markMatchedExpenseItems(source.getItems(), source.getMatchedItemId());
        return source;
    }

    private Long resolveMatchedItemId(List<?> items, Long sourceItemId) {
        if (sourceItemId == null || CollUtil.isEmpty(items)) {
            return null;
        }
        return items.stream().anyMatch(item -> {
            if (item instanceof ErpPurchaseInItemDO) {
                return sourceItemId.equals(((ErpPurchaseInItemDO) item).getId());
            }
            if (item instanceof ErpFinanceExpenseItemDO) {
                return sourceItemId.equals(((ErpFinanceExpenseItemDO) item).getId());
            }
            return false;
        }) ? sourceItemId : null;
    }

    private void markMatchedPurchaseInItems(List<ErpFinanceAssetTraceRespVO.PurchaseInItem> items, Long matchedItemId) {
        if (CollUtil.isEmpty(items)) {
            return;
        }
        items.forEach(item -> item.setMatched(matchedItemId != null && matchedItemId.equals(item.getId())));
    }

    private void markMatchedExpenseItems(List<ErpFinanceAssetTraceRespVO.ExpenseItem> items, Long matchedItemId) {
        if (CollUtil.isEmpty(items)) {
            return;
        }
        items.forEach(item -> item.setMatched(matchedItemId != null && matchedItemId.equals(item.getId())));
    }

    private ErpFinanceExpenseRespVO buildExpenseRespVO(ErpFinanceExpenseDO expense, List<ErpFinanceExpenseItemDO> items) {
        ErpFinanceExpenseRespVO respVO = BeanUtils.toBean(expense, ErpFinanceExpenseRespVO.class);
        Map<Long, DeptRespDTO> deptMap = expense.getDeptId() == null
                ? Collections.emptyMap()
                : deptApi.getDeptMap(Collections.singleton(expense.getDeptId()));
        Map<Long, ErpProjectDO> projectMap = expense.getProjectId() == null
                ? Collections.emptyMap()
                : projectService.getProjectMap(Collections.singleton(expense.getProjectId()));
        Map<Long, ErpSupplierDO> supplierMap = expense.getSupplierId() == null
                ? Collections.emptyMap()
                : supplierService.getSupplierMap(Collections.singleton(expense.getSupplierId()));
        Map<Long, ErpAccountDO> accountMap = expense.getAccountId() == null
                ? Collections.emptyMap()
                : accountService.getAccountMap(Collections.singleton(expense.getAccountId()));
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
                convertListByFlatMap(Collections.singletonList(expense),
                        item -> Stream.of(parseUserId(item.getCreator()), item.getFinanceUserId())));
        MapUtils.findAndThen(deptMap, respVO.getDeptId(), dept -> respVO.setDeptName(dept.getName()));
        MapUtils.findAndThen(projectMap, respVO.getProjectId(), project -> respVO.setProjectName(project.getName()));
        MapUtils.findAndThen(supplierMap, respVO.getSupplierId(), supplier -> respVO.setSupplierName(supplier.getName()));
        MapUtils.findAndThen(accountMap, respVO.getAccountId(), account -> respVO.setAccountName(account.getName()));
        MapUtils.findAndThen(userMap, parseUserId(respVO.getCreator()), user -> respVO.setCreatorName(user.getNickname()));
        MapUtils.findAndThen(userMap, respVO.getFinanceUserId(), user -> respVO.setFinanceUserName(user.getNickname()));
        respVO.setExpenseTypeName(resolveExpenseTypeName(respVO.getExpenseType()));
        respVO.setResearchCategoryName(resolveResearchCategoryName(respVO.getResearchCategory()));
        respVO.setItems(BeanUtils.toBean(items, ErpFinanceExpenseItemRespVO.class));
        return respVO;
    }

    private String resolveExpenseTypeName(Integer expenseType) {
        ErpFinanceExpenseTypeEnum expenseTypeEnum = ErpFinanceExpenseTypeEnum.fromType(expenseType);
        return expenseTypeEnum == null ? null : expenseTypeEnum.getName();
    }

    private String resolveResearchCategoryName(Integer researchCategory) {
        ErpResearchExpenseCategoryEnum categoryEnum = ErpResearchExpenseCategoryEnum.fromType(researchCategory);
        return categoryEnum == null ? null : categoryEnum.getName();
    }
}
