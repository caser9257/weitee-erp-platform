package cn.iocoder.yudao.module.erp.controller.admin.finance;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.expense.ErpFinanceExpenseCancelApprovalReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.expense.ErpFinanceExpenseItemRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.expense.ErpFinanceExpensePageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.expense.ErpFinanceExpenseProjectSummaryReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.expense.ErpFinanceExpenseProjectSummaryRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.expense.ErpFinanceExpenseRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.expense.ErpFinanceExpenseSaveReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.expense.ErpFinanceExpenseSubmitReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.expense.ErpFinanceExpenseTraceRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.expense.ErpFinanceExpenseTypeRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpAccountDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpApStatementDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpApStatementItemDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceExpenseDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceExpenseItemDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinancePaymentAllocateDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinancePaymentDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.project.ErpProjectDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpSupplierDO;
import cn.iocoder.yudao.module.erp.enums.ErpApStatementStatusEnum;
import cn.iocoder.yudao.module.erp.enums.ErpFinanceExpenseAccountingTypeEnum;
import cn.iocoder.yudao.module.erp.enums.ErpFinanceExpenseTypeEnum;
import cn.iocoder.yudao.module.erp.enums.ErpResearchExpenseCategoryEnum;
import cn.iocoder.yudao.module.erp.enums.common.ErpBizTypeEnum;
import cn.iocoder.yudao.module.erp.service.finance.ErpAccountService;
import cn.iocoder.yudao.module.erp.service.finance.ErpApStatementService;
import cn.iocoder.yudao.module.erp.service.finance.ErpFinanceExpenseBpmService;
import cn.iocoder.yudao.module.erp.service.finance.ErpFinanceExpenseService;
import cn.iocoder.yudao.module.erp.service.finance.ErpFinancePaymentService;
import cn.iocoder.yudao.module.erp.service.project.ErpProjectService;
import cn.iocoder.yudao.module.erp.service.purchase.ErpSupplierService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
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

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertListByFlatMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMultiMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.module.erp.util.ErpUserIdUtils.parseUserId;

@Tag(name = "管理后台 - ERP 研发报销 / 零星采购")
@RestController
@RequestMapping("/erp/finance-expense")
@Validated
public class ErpFinanceExpenseController {

    @Resource
    private ErpFinanceExpenseService financeExpenseService;
    @Resource
    private ErpFinanceExpenseBpmService financeExpenseBpmService;
    @Resource
    private ErpFinancePaymentService financePaymentService;
    @Resource
    private ErpApStatementService apStatementService;
    @Resource
    private ErpAccountService accountService;
    @Resource
    private ErpProjectService projectService;
    @Resource
    private ErpSupplierService supplierService;
    @Resource
    private DeptApi deptApi;
    @Resource
    private AdminUserApi adminUserApi;

    @PostMapping("/create")
    @Operation(summary = "创建费用单")
    @PreAuthorize("@ss.hasPermission('erp:finance-expense:create')")
    public CommonResult<Long> createFinanceExpense(@Valid @RequestBody ErpFinanceExpenseSaveReqVO createReqVO) {
        return success(financeExpenseService.createFinanceExpense(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新费用单")
    @PreAuthorize("@ss.hasPermission('erp:finance-expense:update')")
    public CommonResult<Boolean> updateFinanceExpense(@Valid @RequestBody ErpFinanceExpenseSaveReqVO updateReqVO) {
        financeExpenseService.updateFinanceExpense(updateReqVO);
        return success(true);
    }

    @PutMapping("/update-status")
    @Operation(summary = "更新费用单状态")
    @PreAuthorize("@ss.hasPermission('erp:finance-expense:update-status')")
    public CommonResult<Boolean> updateFinanceExpenseStatus(@RequestParam("id") Long id,
                                                            @RequestParam("status") Integer status) {
        financeExpenseService.updateFinanceExpenseStatus(id, status);
        return success(true);
    }

    @PostMapping("/submit")
    @Operation(summary = "提交费用单审批")
    @PreAuthorize("@ss.hasPermission('erp:finance-expense:submit')")
    public CommonResult<String> submitFinanceExpense(@Valid @RequestBody ErpFinanceExpenseSubmitReqVO reqVO) {
        return success(financeExpenseBpmService.submitFinanceExpense(getLoginUserId(), reqVO));
    }

    @DeleteMapping("/cancel-approval")
    @Operation(summary = "撤回费用单审批")
    @PreAuthorize("@ss.hasPermission('erp:finance-expense:cancel-approval')")
    public CommonResult<Boolean> cancelFinanceExpenseApproval(@Valid @RequestBody ErpFinanceExpenseCancelApprovalReqVO reqVO) {
        financeExpenseBpmService.cancelFinanceExpenseApproval(getLoginUserId(), reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除费用单")
    @Parameter(name = "ids", description = "编号数组", required = true)
    @PreAuthorize("@ss.hasPermission('erp:finance-expense:delete')")
    public CommonResult<Boolean> deleteFinanceExpense(@RequestParam("ids") List<Long> ids) {
        financeExpenseService.deleteFinanceExpense(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得费用单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:finance-expense:query')")
    public CommonResult<ErpFinanceExpenseRespVO> getFinanceExpense(@RequestParam("id") Long id) {
        ErpFinanceExpenseDO expense = financeExpenseService.getFinanceExpense(id);
        if (expense == null) {
            return success(null);
        }
        List<ErpFinanceExpenseItemDO> items = financeExpenseService.getFinanceExpenseItemListByExpenseId(id);
        return success(buildExpenseRespVO(expense, items));
    }

    @GetMapping("/get-trace")
    @Operation(summary = "获得费用单追溯")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:finance-expense:query')")
    public CommonResult<ErpFinanceExpenseTraceRespVO> getFinanceExpenseTrace(@RequestParam("id") Long id) {
        ErpFinanceExpenseDO expense = financeExpenseService.getFinanceExpense(id);
        if (expense == null) {
            return success(null);
        }
        List<ErpFinanceExpenseItemDO> items = financeExpenseService.getFinanceExpenseItemListByExpenseId(id);
        ErpApStatementDO statement = apStatementService.getApStatementByBizTypeAndBizId(ErpBizTypeEnum.FINANCE_EXPENSE.getType(), id);
        List<ErpApStatementItemDO> statementItems = statement == null
                ? Collections.emptyList()
                : apStatementService.getApStatementItemListByStatementId(statement.getId());
        List<ErpFinancePaymentAllocateDO> allocates = statement == null
                ? Collections.emptyList()
                : financePaymentService.getFinancePaymentAllocateListByStatementIds(Collections.singleton(statement.getId()));
        Map<Long, ErpFinancePaymentDO> paymentMap = convertMap(
                financePaymentService.getFinancePaymentListByIds(convertSet(allocates, ErpFinancePaymentAllocateDO::getPaymentId)),
                ErpFinancePaymentDO::getId);

        ErpFinanceExpenseTraceRespVO traceRespVO = new ErpFinanceExpenseTraceRespVO();
        traceRespVO.setExpense(buildExpenseRespVO(expense, items));
        traceRespVO.setStatement(buildTraceStatement(statement));
        traceRespVO.setStatementItems(BeanUtils.toBean(statementItems, ErpFinanceExpenseTraceRespVO.StatementItem.class));
        traceRespVO.setAllocates(BeanUtils.toBean(allocates, ErpFinanceExpenseTraceRespVO.AllocateItem.class,
                item -> MapUtils.findAndThen(paymentMap, item.getPaymentId(), payment -> item.setPaymentNo(payment.getNo()))));
        return success(traceRespVO);
    }

    @GetMapping("/page")
    @Operation(summary = "获得费用单分页")
    @PreAuthorize("@ss.hasPermission('erp:finance-expense:query')")
    public CommonResult<PageResult<ErpFinanceExpenseRespVO>> getFinanceExpensePage(@Valid ErpFinanceExpensePageReqVO pageReqVO) {
        PageResult<ErpFinanceExpenseDO> pageResult = financeExpenseService.getFinanceExpensePage(pageReqVO);
        return success(buildExpenseVOPageResult(pageResult));
    }

    @GetMapping("/project-summary")
    @Operation(summary = "获得项目维度费用汇总")
    @PreAuthorize("@ss.hasPermission('erp:finance-expense:query')")
    public CommonResult<List<ErpFinanceExpenseProjectSummaryRespVO>> getProjectSummary(@Valid ErpFinanceExpenseProjectSummaryReqVO reqVO) {
        List<ErpFinanceExpenseProjectSummaryRespVO> list = financeExpenseService.getFinanceExpenseProjectSummary(reqVO);
        Map<Long, ErpProjectDO> projectMap = projectService.getProjectMap(convertSet(list, ErpFinanceExpenseProjectSummaryRespVO::getProjectId));
        list.forEach(item -> MapUtils.findAndThen(projectMap, item.getProjectId(), project -> item.setProjectName(project.getName())));
        return success(list);
    }

    @GetMapping("/expense-types")
    @Operation(summary = "获得费用类型")
    @PreAuthorize("@ss.hasPermission('erp:finance-expense:query')")
    public CommonResult<List<ErpFinanceExpenseTypeRespVO>> getExpenseTypeList() {
        List<ErpFinanceExpenseTypeRespVO> list = java.util.Arrays.stream(ErpFinanceExpenseTypeEnum.values())
                .map(type -> {
                    ErpFinanceExpenseTypeRespVO vo = new ErpFinanceExpenseTypeRespVO();
                    vo.setValue(type.getType());
                    vo.setLabel(type.getName());
                    vo.setProjectRequired(type.isProjectRequired());
                    return vo;
                })
                .toList();
        return success(list);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出费用单 Excel")
    @PreAuthorize("@ss.hasPermission('erp:finance-expense:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportFinanceExpenseExcel(@Valid ErpFinanceExpensePageReqVO pageReqVO,
                                          HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ErpFinanceExpenseRespVO> list = buildExpenseVOPageResult(
                financeExpenseService.getFinanceExpensePage(pageReqVO)).getList();
        ExcelUtils.write(response, "研发报销零星采购.xls", "数据", ErpFinanceExpenseRespVO.class, list);
    }

    private PageResult<ErpFinanceExpenseRespVO> buildExpenseVOPageResult(PageResult<ErpFinanceExpenseDO> pageResult) {
        if (CollUtil.isEmpty(pageResult.getList())) {
            return PageResult.empty(pageResult.getTotal());
        }
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(convertSet(pageResult.getList(), ErpFinanceExpenseDO::getDeptId));
        Map<Long, ErpProjectDO> projectMap = projectService.getProjectMap(convertSet(pageResult.getList(), ErpFinanceExpenseDO::getProjectId));
        Map<Long, ErpSupplierDO> supplierMap = supplierService.getSupplierMap(convertSet(pageResult.getList(), ErpFinanceExpenseDO::getSupplierId));
        Map<Long, ErpAccountDO> accountMap = accountService.getAccountMap(convertSet(pageResult.getList(), ErpFinanceExpenseDO::getAccountId));
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(convertListByFlatMap(pageResult.getList(),
                expense -> Stream.of(parseUserId(expense.getCreator()), expense.getFinanceUserId())));
        Map<Long, List<ErpFinanceExpenseItemDO>> itemMap = convertMultiMap(
                financeExpenseService.getFinanceExpenseItemListByExpenseIds(convertSet(pageResult.getList(), ErpFinanceExpenseDO::getId)),
                ErpFinanceExpenseItemDO::getExpenseId);
        return BeanUtils.toBean(pageResult, ErpFinanceExpenseRespVO.class, expense -> {
            fillExpenseVO(expense, deptMap, projectMap, supplierMap, accountMap, userMap);
            expense.setExpenseTypeName(resolveExpenseTypeName(expense.getExpenseType()));
            expense.setResearchCategoryName(resolveResearchCategoryName(expense.getResearchCategory()));
            expense.setRdAccountingTypeName(resolveRdAccountingTypeName(expense.getRdAccountingType()));
            expense.setItems(BeanUtils.toBean(itemMap.get(expense.getId()), ErpFinanceExpenseItemRespVO.class));
        });
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
        fillExpenseVO(respVO, deptMap, projectMap, supplierMap, accountMap, userMap);
        respVO.setExpenseTypeName(resolveExpenseTypeName(respVO.getExpenseType()));
        respVO.setResearchCategoryName(resolveResearchCategoryName(respVO.getResearchCategory()));
        respVO.setRdAccountingTypeName(resolveRdAccountingTypeName(respVO.getRdAccountingType()));
        respVO.setItems(BeanUtils.toBean(items, ErpFinanceExpenseItemRespVO.class));
        return respVO;
    }

    private void fillExpenseVO(ErpFinanceExpenseRespVO expense,
                               Map<Long, DeptRespDTO> deptMap,
                               Map<Long, ErpProjectDO> projectMap,
                               Map<Long, ErpSupplierDO> supplierMap,
                               Map<Long, ErpAccountDO> accountMap,
                               Map<Long, AdminUserRespDTO> userMap) {
        MapUtils.findAndThen(deptMap, expense.getDeptId(), dept -> expense.setDeptName(dept.getName()));
        MapUtils.findAndThen(projectMap, expense.getProjectId(), project -> expense.setProjectName(project.getName()));
        MapUtils.findAndThen(supplierMap, expense.getSupplierId(), supplier -> expense.setSupplierName(supplier.getName()));
        MapUtils.findAndThen(accountMap, expense.getAccountId(), account -> expense.setAccountName(account.getName()));
        MapUtils.findAndThen(userMap, parseUserId(expense.getCreator()), user -> expense.setCreatorName(user.getNickname()));
        MapUtils.findAndThen(userMap, expense.getFinanceUserId(), user -> expense.setFinanceUserName(user.getNickname()));
    }

    private ErpFinanceExpenseTraceRespVO.Statement buildTraceStatement(ErpApStatementDO statement) {
        if (statement == null) {
            return null;
        }
        ErpFinanceExpenseTraceRespVO.Statement traceStatement = BeanUtils.toBean(statement, ErpFinanceExpenseTraceRespVO.Statement.class,
                item -> item.setStatementId(statement.getId()));
        Map<Long, ErpSupplierDO> supplierMap = statement.getSupplierId() == null
                ? Collections.emptyMap()
                : supplierService.getSupplierMap(Collections.singleton(statement.getSupplierId()));
        Map<Long, ErpAccountDO> accountMap = statement.getAccountId() == null
                ? Collections.emptyMap()
                : accountService.getAccountMap(Collections.singleton(statement.getAccountId()));
        MapUtils.findAndThen(supplierMap, statement.getSupplierId(), supplier -> traceStatement.setSupplierName(supplier.getName()));
        MapUtils.findAndThen(accountMap, statement.getAccountId(), account -> traceStatement.setAccountName(account.getName()));
        traceStatement.setStatusName(resolveStatementStatusName(statement.getStatus()));
        return traceStatement;
    }

    private String resolveExpenseTypeName(Integer expenseType) {
        ErpFinanceExpenseTypeEnum expenseTypeEnum = ErpFinanceExpenseTypeEnum.fromType(expenseType);
        return expenseTypeEnum == null ? null : expenseTypeEnum.getName();
    }

    private String resolveResearchCategoryName(Integer researchCategory) {
        ErpResearchExpenseCategoryEnum categoryEnum = ErpResearchExpenseCategoryEnum.fromType(researchCategory);
        return categoryEnum == null ? null : categoryEnum.getName();
    }

    private String resolveRdAccountingTypeName(Integer rdAccountingType) {
        ErpFinanceExpenseAccountingTypeEnum accountingTypeEnum =
                ErpFinanceExpenseAccountingTypeEnum.fromType(rdAccountingType);
        return accountingTypeEnum == null ? null : accountingTypeEnum.getName();
    }

    private String resolveStatementStatusName(Integer status) {
        return java.util.Arrays.stream(ErpApStatementStatusEnum.values())
                .filter(item -> item.getStatus().equals(status))
                .map(ErpApStatementStatusEnum::getName)
                .findFirst()
                .orElse(null);
    }

}
