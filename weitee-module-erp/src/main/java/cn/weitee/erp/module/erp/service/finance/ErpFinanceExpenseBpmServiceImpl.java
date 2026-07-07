package cn.weitee.erp.module.erp.service.finance;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.weitee.erp.module.bpm.service.approval.BpmApprovalRuntimeService;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.expense.ErpFinanceExpenseCancelApprovalReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.expense.ErpFinanceExpenseSubmitReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceExpenseDO;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceExpenseMapper;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.enums.ErpFinanceExpenseBpmConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsExpense.EXPENSE_APPROVE_FAIL;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsExpense.EXPENSE_BPM_CANCEL_FAIL;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsExpense.EXPENSE_BPM_SUBMIT_FAIL;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsExpense.EXPENSE_NOT_EXISTS;

@Service
@Validated
@Slf4j
public class ErpFinanceExpenseBpmServiceImpl implements ErpFinanceExpenseBpmService {

    @Resource
    private ErpFinanceExpenseMapper erpFinanceExpenseMapper;
    @Resource
    private ErpFinanceExpenseService financeExpenseService;

    @Resource
    private BpmApprovalRuntimeService approvalRuntimeService;
    @Resource
    private PlatformTransactionManager transactionManager;

    @Override
    public String submitFinanceExpense(Long userId, ErpFinanceExpenseSubmitReqVO reqVO) {
        ErpFinanceExpenseDO expense = getRequiredFinanceExpense(reqVO.getId());
        if (ObjectUtil.equal(expense.getStatus(), ErpAuditStatus.APPROVE.getStatus())) {
            throw exception(EXPENSE_APPROVE_FAIL);
        }
        if (ObjectUtil.equal(expense.getStatus(), ErpAuditStatus.PROCESS.getStatus())
                && StrUtil.isNotBlank(expense.getProcessInstanceId())) {
            throw exception(EXPENSE_BPM_SUBMIT_FAIL);
        }
        Long expenseId = expense.getId();
        executeInRequiredTransaction(() -> erpFinanceExpenseMapper.updateById(new ErpFinanceExpenseDO()
                .setId(expenseId)
                .setStatus(ErpAuditStatus.PROCESS.getStatus())
                .setProcessInstanceId(null)));

        String processInstanceId;
        try {
            processInstanceId = approvalRuntimeService.submit(
                    "erp.finance.expense.submit", expenseId, userId);
        } catch (Exception e) {
            log.error("[submitFinanceExpense] BPM 创建失败，expenseId={}", expenseId, e);
            executeInRequiredTransaction(() -> erpFinanceExpenseMapper.updateById(new ErpFinanceExpenseDO()
                    .setId(expenseId)
                    .setStatus(ErpAuditStatus.FAILED.getStatus())
                    .setProcessInstanceId(null)));
            throw e;
        }
        executeInRequiredTransaction(() -> erpFinanceExpenseMapper.updateById(new ErpFinanceExpenseDO()
                .setId(expenseId)
                .setProcessInstanceId(processInstanceId)));
        return processInstanceId;
    }

    @Override
    public void cancelFinanceExpenseApproval(Long userId, ErpFinanceExpenseCancelApprovalReqVO reqVO) {
        ErpFinanceExpenseDO expense = getRequiredFinanceExpense(reqVO.getId());
        if (!ObjectUtil.equal(expense.getStatus(), ErpAuditStatus.PROCESS.getStatus())
                || StrUtil.isBlank(expense.getProcessInstanceId())) {
            throw exception(EXPENSE_BPM_CANCEL_FAIL);
        }
        approvalRuntimeService.cancel("erp.finance.expense.submit", expense.getId(), userId, reqVO.getReason());
    }

    @Override
    public void handleProcessInstanceResult(Long expenseId, String processInstanceId, Integer status, String reason) {
        // 旧监听器入口保留兼容，结果回写已收敛到 ExpenseResultHandler
    }

    private Map<String, Object> buildVariables(ErpFinanceExpenseDO expense) {
        Map<String, Object> variables = new HashMap<>();
        variables.put(ErpFinanceExpenseBpmConstants.VARIABLE_EXPENSE_ID, expense.getId());
        variables.put(ErpFinanceExpenseBpmConstants.VARIABLE_EXPENSE_NO, expense.getNo());
        variables.put(ErpFinanceExpenseBpmConstants.VARIABLE_EXPENSE_PRICE, expense.getExpensePrice());
        variables.put(ErpFinanceExpenseBpmConstants.VARIABLE_DEPT_ID, expense.getDeptId());
        variables.put(ErpFinanceExpenseBpmConstants.VARIABLE_PROJECT_ID, expense.getProjectId());
        variables.put(ErpFinanceExpenseBpmConstants.VARIABLE_SUPPLIER_ID, expense.getSupplierId());
        variables.put(ErpFinanceExpenseBpmConstants.VARIABLE_FINANCE_USER_ID, expense.getFinanceUserId());
        variables.put(ErpFinanceExpenseBpmConstants.VARIABLE_ACCOUNT_ID, expense.getAccountId());
        return variables;
    }

    private <T> T executeInRequiredTransaction(java.util.function.Supplier<T> supplier) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        return transactionTemplate.execute(status -> supplier.get());
    }

    private void executeInRequiredTransaction(Runnable runnable) {
        executeInRequiredTransaction(() -> {
            runnable.run();
            return null;
        });
    }

    private ErpFinanceExpenseDO getRequiredFinanceExpense(Long expenseId) {
        ErpFinanceExpenseDO expense = erpFinanceExpenseMapper.selectById(expenseId);
        if (expense == null) {
            throw exception(EXPENSE_NOT_EXISTS);
        }
        return expense;
    }

}
