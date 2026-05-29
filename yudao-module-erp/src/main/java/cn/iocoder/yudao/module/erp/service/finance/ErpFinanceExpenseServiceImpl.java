package cn.iocoder.yudao.module.erp.service.finance;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.expense.ErpFinanceExpensePageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.expense.ErpFinanceExpenseProjectSummaryReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.expense.ErpFinanceExpenseProjectSummaryRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.expense.ErpFinanceExpenseSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpApStatementDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceExpenseDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceExpenseItemDO;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinanceExpenseItemMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinanceExpenseMapper;
import cn.iocoder.yudao.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.iocoder.yudao.module.erp.enums.ErpAuditStatus;
import cn.iocoder.yudao.module.erp.enums.ErpFinanceExpenseAccountingTypeEnum;
import cn.iocoder.yudao.module.erp.enums.ErpFinanceExpenseTypeEnum;
import cn.iocoder.yudao.module.erp.enums.ErrorCodeConstantsExpense;
import cn.iocoder.yudao.module.erp.enums.common.ErpBizTypeEnum;
import cn.iocoder.yudao.module.erp.service.project.ErpProjectService;
import cn.iocoder.yudao.module.erp.service.purchase.ErpSupplierService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.diffList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.getSumValue;

/**
 * ERP 费用报销 Service 实现类
 */
@Service
@Validated
public class ErpFinanceExpenseServiceImpl implements ErpFinanceExpenseService {

    @Resource
    private ErpFinanceExpenseMapper erpFinanceExpenseMapper;
    @Resource
    private ErpFinanceExpenseItemMapper erpFinanceExpenseItemMapper;
    @Resource
    private ErpNoRedisDAO noRedisDAO;
    @Resource
    private DeptApi deptApi;
    @Resource
    private ErpProjectService projectService;
    @Resource
    private ErpSupplierService supplierService;
    @Resource
    private ErpAccountService accountService;
    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private ErpApStatementService apStatementService;
    @Resource
    @Lazy
    private ErpFinanceBizHookService financeBizHookService;
    @Resource
    @Lazy
    private ErpFinanceAssetCandidateService financeAssetCandidateService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createFinanceExpense(ErpFinanceExpenseSaveReqVO createReqVO) {
        validateRefs(createReqVO.getExpenseType(), createReqVO.getRdAccountingType(),
                createReqVO.getDeptId(), createReqVO.getProjectId(),
                createReqVO.getSupplierId(), createReqVO.getAccountId(), createReqVO.getFinanceUserId());
        List<ErpFinanceExpenseItemDO> items = validateItems(createReqVO.getItems(), createReqVO.getExpensePrice());
        String no = noRedisDAO.generate(ErpNoRedisDAO.FINANCE_EXPENSE_NO_PREFIX);
        if (erpFinanceExpenseMapper.selectByNo(no) != null) {
            throw exception(ErrorCodeConstantsExpense.EXPENSE_NO_EXISTS);
        }
        ErpFinanceExpenseDO expense = BeanUtils.toBean(createReqVO, ErpFinanceExpenseDO.class, in -> in
                .setNo(no)
                .setStatus(ErpAuditStatus.PROCESS.getStatus())
                .setPaidPrice(BigDecimal.ZERO)
                .setRemainPrice(defaultAmount(createReqVO.getExpensePrice())));
        erpFinanceExpenseMapper.insert(expense);
        insertExpenseItems(expense.getId(), items);
        return expense.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFinanceExpense(ErpFinanceExpenseSaveReqVO updateReqVO) {
        ErpFinanceExpenseDO expense = validateExpenseExists(updateReqVO.getId());
        if (ErpAuditStatus.APPROVE.getStatus().equals(expense.getStatus())) {
            throw exception(ErrorCodeConstantsExpense.EXPENSE_UPDATE_FAIL_APPROVE, expense.getNo());
        }
        validateRefs(updateReqVO.getExpenseType(), updateReqVO.getRdAccountingType(),
                updateReqVO.getDeptId(), updateReqVO.getProjectId(),
                updateReqVO.getSupplierId(), updateReqVO.getAccountId(), updateReqVO.getFinanceUserId());
        List<ErpFinanceExpenseItemDO> items = validateItems(updateReqVO.getItems(), updateReqVO.getExpensePrice());
        ErpFinanceExpenseDO updateObj = BeanUtils.toBean(updateReqVO, ErpFinanceExpenseDO.class, in -> in
                .setPaidPrice(BigDecimal.ZERO)
                .setRemainPrice(defaultAmount(updateReqVO.getExpensePrice())));
        erpFinanceExpenseMapper.updateById(updateObj);
        syncExpenseItemList(updateReqVO.getId(), items);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFinanceExpenseStatus(Long id, Integer status) {
        boolean approve = ErpAuditStatus.APPROVE.getStatus().equals(status);
        ErpFinanceExpenseDO expense = validateExpenseExists(id);
        validateStatusTransition(expense, approve);
        if (approve) {
            validateExpenseBeforeApprove(expense);
            ensureExpenseItems(expense);
        } else {
            validateExpenseCanRollback(expense);
        }
        int updateCount = erpFinanceExpenseMapper.updateByIdAndStatus(id, expense.getStatus(),
                new ErpFinanceExpenseDO().setStatus(status));
        if (updateCount == 0) {
            throw exception(approve ? ErrorCodeConstantsExpense.EXPENSE_APPROVE_FAIL
                    : ErrorCodeConstantsExpense.EXPENSE_PROCESS_FAIL);
        }
        if (approve) {
            apStatementService.createStatementForFinanceExpense(expense);
            financeBizHookService.handleApprovedBiz(ErpBizTypeEnum.FINANCE_EXPENSE.getType(), expense.getId(),
                    defaultTime(expense.getExpenseTime(), expense.getCreateTime(), expense.getUpdateTime()).toLocalDate());
            financeAssetCandidateService.createCandidateFromExpense(expense.getId());
        } else {
            apStatementService.closeStatementByBiz(ErpBizTypeEnum.FINANCE_EXPENSE.getType(), expense.getId(),
                    "费用单反审核关闭台账");
            financeBizHookService.handleRollbackBiz(ErpBizTypeEnum.FINANCE_EXPENSE.getType(), expense.getId(),
                    null, "费用单反审核关闭台账");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFinanceExpenseStatusByBpm(Long id, String processInstanceId, Integer status, String reason) {
        boolean approve = ErpAuditStatus.APPROVE.getStatus().equals(status);
        boolean reject = ErpAuditStatus.REJECT.getStatus().equals(status);
        ErpFinanceExpenseDO expense = validateExpenseExists(id);
        if (!approve && !reject) {
            throw exception(ErrorCodeConstantsExpense.EXPENSE_PROCESS_FAIL);
        }
        if (approve) {
            validateExpenseBeforeApprove(expense);
            ensureExpenseItems(expense);
        }
        ErpFinanceExpenseDO updateObj = new ErpFinanceExpenseDO()
                .setId(id)
                .setProcessInstanceId(processInstanceId)
                .setStatus(status);
        int updateCount = erpFinanceExpenseMapper.updateByIdAndStatus(id, expense.getStatus(), updateObj);
        if (updateCount == 0) {
            throw exception(approve ? ErrorCodeConstantsExpense.EXPENSE_APPROVE_FAIL
                    : ErrorCodeConstantsExpense.EXPENSE_PROCESS_FAIL);
        }
        if (approve) {
            apStatementService.createStatementForFinanceExpense(expense);
            financeBizHookService.handleApprovedBiz(ErpBizTypeEnum.FINANCE_EXPENSE.getType(), expense.getId(),
                    defaultTime(expense.getExpenseTime(), expense.getCreateTime(), expense.getUpdateTime()).toLocalDate());
            financeAssetCandidateService.createCandidateFromExpense(expense.getId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFinanceExpense(List<Long> ids) {
        List<ErpFinanceExpenseDO> expenses = erpFinanceExpenseMapper.selectByIds(ids);
        if (CollUtil.isEmpty(expenses)) {
            return;
        }
        expenses.forEach(expense -> {
            if (ErpAuditStatus.APPROVE.getStatus().equals(expense.getStatus())) {
                throw exception(ErrorCodeConstantsExpense.EXPENSE_DELETE_FAIL_APPROVE, expense.getNo());
            }
        });
        expenses.forEach(expense -> {
            erpFinanceExpenseMapper.deleteById(expense.getId());
            List<ErpFinanceExpenseItemDO> items = erpFinanceExpenseItemMapper.selectListByExpenseId(expense.getId());
            if (CollUtil.isNotEmpty(items)) {
                erpFinanceExpenseItemMapper.deleteByIds(items.stream().map(ErpFinanceExpenseItemDO::getId).toList());
            }
        });
    }

    @Override
    public ErpFinanceExpenseDO getFinanceExpense(Long id) {
        return erpFinanceExpenseMapper.selectById(id);
    }

    @Override
    public PageResult<ErpFinanceExpenseDO> getFinanceExpensePage(ErpFinanceExpensePageReqVO pageReqVO) {
        return erpFinanceExpenseMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ErpFinanceExpenseItemDO> getFinanceExpenseItemListByExpenseId(Long expenseId) {
        return erpFinanceExpenseItemMapper.selectListByExpenseId(expenseId);
    }

    @Override
    public List<ErpFinanceExpenseItemDO> getFinanceExpenseItemListByExpenseIds(Collection<Long> expenseIds) {
        return erpFinanceExpenseItemMapper.selectListByExpenseIds(expenseIds);
    }

    @Override
    public List<ErpFinanceExpenseProjectSummaryRespVO> getFinanceExpenseProjectSummary(ErpFinanceExpenseProjectSummaryReqVO reqVO) {
        List<ErpFinanceExpenseDO> expenses = erpFinanceExpenseMapper.selectList(new LambdaQueryWrapperX<ErpFinanceExpenseDO>()
                .betweenIfPresent(ErpFinanceExpenseDO::getExpenseTime, reqVO.getExpenseTime())
                .eqIfPresent(ErpFinanceExpenseDO::getProjectId, reqVO.getProjectId())
                .eqIfPresent(ErpFinanceExpenseDO::getDeptId, reqVO.getDeptId())
                .eqIfPresent(ErpFinanceExpenseDO::getExpenseType, reqVO.getExpenseType())
                .eqIfPresent(ErpFinanceExpenseDO::getRdAccountingType, reqVO.getRdAccountingType())
                .eqIfPresent(ErpFinanceExpenseDO::getStatus, reqVO.getStatus())
                .orderByDesc(ErpFinanceExpenseDO::getId));
        if (CollUtil.isEmpty(expenses)) {
            return Collections.emptyList();
        }
        Map<String, ErpFinanceExpenseProjectSummaryRespVO> summaryMap = new LinkedHashMap<>();
        for (ErpFinanceExpenseDO expense : expenses) {
            String key = ObjectUtil.defaultIfNull(expense.getProjectId(), 0L)
                    + "_" + expense.getExpenseType()
                    + "_" + ObjectUtil.defaultIfNull(expense.getRdAccountingType(), 0);
            ErpFinanceExpenseProjectSummaryRespVO summary = summaryMap.computeIfAbsent(key, item -> {
                ErpFinanceExpenseProjectSummaryRespVO respVO = new ErpFinanceExpenseProjectSummaryRespVO();
                respVO.setProjectId(expense.getProjectId());
                respVO.setExpenseType(expense.getExpenseType());
                respVO.setExpenseTypeName(resolveExpenseTypeName(expense.getExpenseType()));
                respVO.setRdAccountingType(expense.getRdAccountingType());
                respVO.setRdAccountingTypeName(resolveRdAccountingTypeName(expense.getRdAccountingType()));
                respVO.setExpenseCount(0L);
                respVO.setTotalExpensePrice(BigDecimal.ZERO);
                respVO.setTotalPaidPrice(BigDecimal.ZERO);
                respVO.setTotalRemainPrice(BigDecimal.ZERO);
                return respVO;
            });
            summary.setExpenseCount(summary.getExpenseCount() + 1);
            summary.setTotalExpensePrice(summary.getTotalExpensePrice().add(defaultAmount(expense.getExpensePrice())));
            summary.setTotalPaidPrice(summary.getTotalPaidPrice().add(defaultAmount(expense.getPaidPrice())));
            summary.setTotalRemainPrice(summary.getTotalRemainPrice().add(defaultAmount(expense.getRemainPrice())));
        }
        return new ArrayList<>(summaryMap.values());
    }

    @Override
    public List<ErpFinanceExpenseDO> getApprovedResearchExpenseListByMonth(YearMonth month) {
        if (month == null) {
            return Collections.emptyList();
        }
        return erpFinanceExpenseMapper.selectList(new LambdaQueryWrapperX<ErpFinanceExpenseDO>()
                .eq(ErpFinanceExpenseDO::getExpenseType, ErpFinanceExpenseTypeEnum.RESEARCH.getType())
                .eq(ErpFinanceExpenseDO::getStatus, ErpAuditStatus.APPROVE.getStatus())
                .ge(ErpFinanceExpenseDO::getExpenseTime, month.atDay(1).atStartOfDay())
                .le(ErpFinanceExpenseDO::getExpenseTime, month.atEndOfMonth().atTime(23, 59, 59))
                .orderByAsc(ErpFinanceExpenseDO::getId));
    }

    private void validateRefs(Integer expenseType, Integer rdAccountingType, Long deptId, Long projectId,
                              Long supplierId, Long accountId, Long financeUserId) {
        ErpFinanceExpenseTypeEnum expenseTypeEnum = ErpFinanceExpenseTypeEnum.fromType(expenseType);
        if (expenseTypeEnum == null) {
            throw exception(ErrorCodeConstantsExpense.EXPENSE_TYPE_INVALID);
        }
        validateRdAccountingType(expenseTypeEnum, rdAccountingType);
        deptApi.validateDeptList(Collections.singleton(deptId));
        if (expenseTypeEnum.isProjectRequired() && projectId == null) {
            throw exception(ErrorCodeConstantsExpense.EXPENSE_PROJECT_REQUIRED);
        }
        if (projectId != null) {
            projectService.validateProject(projectId);
        }
        if (supplierId != null) {
            supplierService.validateSupplier(supplierId);
        }
        accountService.validateAccount(accountId);
        if (financeUserId != null) {
            adminUserApi.validateUser(financeUserId);
        }
    }

    private void validateRdAccountingType(ErpFinanceExpenseTypeEnum expenseTypeEnum, Integer rdAccountingType) {
        if (ErpFinanceExpenseTypeEnum.RESEARCH == expenseTypeEnum) {
            if (rdAccountingType == null) {
                throw exception(ErrorCodeConstantsExpense.EXPENSE_RD_ACCOUNTING_TYPE_REQUIRED);
            }
            if (ErpFinanceExpenseAccountingTypeEnum.fromType(rdAccountingType) == null) {
                throw exception(ErrorCodeConstantsExpense.EXPENSE_RD_ACCOUNTING_TYPE_INVALID);
            }
            return;
        }
        if (rdAccountingType != null) {
            throw exception(ErrorCodeConstantsExpense.EXPENSE_RD_ACCOUNTING_TYPE_INVALID);
        }
    }

    private List<ErpFinanceExpenseItemDO> validateItems(List<ErpFinanceExpenseSaveReqVO.Item> items, BigDecimal expensePrice) {
        if (CollUtil.isEmpty(items)) {
            return Collections.emptyList();
        }
        List<ErpFinanceExpenseItemDO> actualItems = BeanUtils.toBean(items, ErpFinanceExpenseItemDO.class, item ->
                item.setItemName(StrUtil.trim(item.getItemName())));
        actualItems.forEach(item -> {
            if (StrUtil.isBlank(item.getItemName()) || defaultAmount(item.getAmount()).compareTo(BigDecimal.ZERO) <= 0) {
                throw exception(ErrorCodeConstantsExpense.EXPENSE_ITEM_AMOUNT_INVALID);
            }
        });
        BigDecimal totalAmount = getSumValue(actualItems, ErpFinanceExpenseItemDO::getAmount, BigDecimal::add, BigDecimal.ZERO);
        if (totalAmount.compareTo(defaultAmount(expensePrice)) != 0) {
            throw exception(ErrorCodeConstantsExpense.EXPENSE_ITEM_AMOUNT_NOT_MATCH);
        }
        return actualItems;
    }

    private void validateStatusTransition(ErpFinanceExpenseDO expense, boolean approve) {
        if (approve) {
            if (ErpAuditStatus.APPROVE.getStatus().equals(expense.getStatus())) {
                throw exception(ErrorCodeConstantsExpense.EXPENSE_APPROVE_FAIL);
            }
            return;
        }
        if (!ErpAuditStatus.APPROVE.getStatus().equals(expense.getStatus())) {
            throw exception(ErrorCodeConstantsExpense.EXPENSE_PROCESS_FAIL);
        }
    }

    private void validateExpenseBeforeApprove(ErpFinanceExpenseDO expense) {
        if (expense.getSupplierId() == null) {
            throw exception(ErrorCodeConstantsExpense.EXPENSE_SUPPLIER_REQUIRED);
        }
        validateRefs(expense.getExpenseType(), expense.getRdAccountingType(),
                expense.getDeptId(), expense.getProjectId(),
                expense.getSupplierId(), expense.getAccountId(), expense.getFinanceUserId());
    }

    private void validateExpenseCanRollback(ErpFinanceExpenseDO expense) {
        ErpApStatementDO statement = apStatementService.getApStatementByBizTypeAndBizId(
                ErpBizTypeEnum.FINANCE_EXPENSE.getType(), expense.getId());
        if (statement == null) {
            return;
        }
        if (defaultAmount(statement.getPaidAmount()).compareTo(BigDecimal.ZERO) != 0) {
            throw exception(ErrorCodeConstantsExpense.EXPENSE_ROLLBACK_FAIL_PAID, expense.getNo());
        }
    }

    private void ensureExpenseItems(ErpFinanceExpenseDO expense) {
        if (CollUtil.isNotEmpty(erpFinanceExpenseItemMapper.selectListByExpenseId(expense.getId()))) {
            return;
        }
        ErpFinanceExpenseTypeEnum expenseTypeEnum = ErpFinanceExpenseTypeEnum.fromType(expense.getExpenseType());
        ErpFinanceExpenseAccountingTypeEnum accountingTypeEnum =
                ErpFinanceExpenseAccountingTypeEnum.fromType(expense.getRdAccountingType());
        String itemName = expenseTypeEnum == null ? "费用" : expenseTypeEnum.getName();
        if (accountingTypeEnum != null) {
            itemName = itemName + "-" + accountingTypeEnum.getName();
        }
        erpFinanceExpenseItemMapper.insert(new ErpFinanceExpenseItemDO()
                .setExpenseId(expense.getId())
                .setItemName(itemName + "汇总")
                .setAmount(defaultAmount(expense.getExpensePrice()))
                .setRemark(expense.getRemark()));
    }

    private void insertExpenseItems(Long expenseId, List<ErpFinanceExpenseItemDO> items) {
        if (CollUtil.isEmpty(items)) {
            return;
        }
        items.forEach(item -> item.setExpenseId(expenseId));
        erpFinanceExpenseItemMapper.insertBatch(items);
    }

    private void syncExpenseItemList(Long expenseId, List<ErpFinanceExpenseItemDO> newList) {
        List<ErpFinanceExpenseItemDO> oldList = erpFinanceExpenseItemMapper.selectListByExpenseId(expenseId);
        List<List<ErpFinanceExpenseItemDO>> actualDiffList = diffList(oldList, newList,
                (oldVal, newVal) -> ObjectUtil.equal(oldVal.getId(), newVal.getId()));
        if (CollUtil.isNotEmpty(actualDiffList.get(0))) {
            actualDiffList.get(0).forEach(item -> item.setExpenseId(expenseId));
            erpFinanceExpenseItemMapper.insertBatch(actualDiffList.get(0));
        }
        if (CollUtil.isNotEmpty(actualDiffList.get(1))) {
            erpFinanceExpenseItemMapper.updateBatch(actualDiffList.get(1));
        }
        if (CollUtil.isNotEmpty(actualDiffList.get(2))) {
            erpFinanceExpenseItemMapper.deleteByIds(actualDiffList.get(2).stream().map(ErpFinanceExpenseItemDO::getId).toList());
        }
    }

    private ErpFinanceExpenseDO validateExpenseExists(Long id) {
        ErpFinanceExpenseDO expense = erpFinanceExpenseMapper.selectById(id);
        if (expense == null) {
            throw exception(ErrorCodeConstantsExpense.EXPENSE_NOT_EXISTS);
        }
        return expense;
    }

    private BigDecimal defaultAmount(BigDecimal amount) {
        return ObjectUtil.defaultIfNull(amount, BigDecimal.ZERO);
    }

    private LocalDateTime defaultTime(LocalDateTime... values) {
        for (LocalDateTime value : values) {
            if (value != null) {
                return value;
            }
        }
        return LocalDateTime.now();
    }

    private String resolveExpenseTypeName(Integer expenseType) {
        ErpFinanceExpenseTypeEnum expenseTypeEnum = ErpFinanceExpenseTypeEnum.fromType(expenseType);
        return expenseTypeEnum == null ? null : expenseTypeEnum.getName();
    }

    private String resolveRdAccountingTypeName(Integer rdAccountingType) {
        ErpFinanceExpenseAccountingTypeEnum accountingTypeEnum =
                ErpFinanceExpenseAccountingTypeEnum.fromType(rdAccountingType);
        return accountingTypeEnum == null ? null : accountingTypeEnum.getName();
    }
}
