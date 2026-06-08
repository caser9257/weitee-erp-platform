package cn.iocoder.yudao.module.erp.service.finance;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceExpenseDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceLedgerDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceSubjectDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceVoucherDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceVoucherEntryDO;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinanceVoucherEntryMapper;
import cn.iocoder.yudao.module.erp.enums.ErpFinanceExpenseAccountingTypeEnum;
import cn.iocoder.yudao.module.erp.enums.common.ErpBizTypeEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

@Service
@Validated
public class ErpFinanceRdMonthEndServiceImpl implements ErpFinanceRdMonthEndService {

    private static final String PROFIT_SUBJECT_CODE = "4103";
    private static final ErrorCode RD_MONTH_END_PERIOD_INVALID = new ErrorCode(1_030_613_000, "研发月末期间格式不正确，应为 yyyy-MM");
    private static final ErrorCode RD_MONTH_END_SUBJECT_MISSING = new ErrorCode(1_030_613_001, "研发月末结转缺少科目({})");

    @Resource
    private ErpFinanceLedgerService financeLedgerService;
    @Resource
    private ErpFinancePeriodService financePeriodService;
    @Resource
    private ErpFinanceExpenseService financeExpenseService;
    @Resource
    private ErpFinanceVoucherService financeVoucherService;
    @Resource
    private ErpFinanceVoucherEntryMapper financeVoucherEntryMapper;
    @Resource
    private ErpFinanceSubjectService financeSubjectService;
    @Resource
    private ErpFinanceAssetService financeAssetService;
    @Resource
    private ErpFinanceAssetDepreciationService financeAssetDepreciationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MonthEndResult executeMonthEnd(String period) {
        YearMonth yearMonth = parsePeriod(period);
        ErpFinanceLedgerDO defaultLedger = financeLedgerService.getDefaultFinanceLedger();
        if (defaultLedger == null) {
            return new MonthEndResult(null, 0);
        }
        financePeriodService.getCurrentOpenPeriod(defaultLedger.getId(), yearMonth.atEndOfMonth());

        Long expenseCarryForwardVoucherId = handleExpenseCarryForward(defaultLedger.getId(), yearMonth);
        List<Long> capitalizeAssetIds = financeAssetService.getActiveResearchCapitalizeAssetIdsByPeriod(period);
        Integer depreciationCount = financeAssetDepreciationService.generateDepreciationForAssets(period, capitalizeAssetIds);
        return new MonthEndResult(expenseCarryForwardVoucherId, ObjectUtil.defaultIfNull(depreciationCount, 0));
    }

    private Long handleExpenseCarryForward(Long ledgerId, YearMonth yearMonth) {
        Long monthEndBizId = buildMonthEndBizId(yearMonth);
        ErpFinanceVoucherDO existedVoucher = financeVoucherService.getVoucherByLedgerAndBiz(
                ledgerId, ErpBizTypeEnum.FINANCE_EXPENSE_MONTH_END.getType(), monthEndBizId);
        if (existedVoucher != null) {
            return existedVoucher.getId();
        }
        List<ErpFinanceExpenseDO> expenseList = financeExpenseService.getApprovedResearchExpenseListByMonth(yearMonth);
        if (CollUtil.isEmpty(expenseList)) {
            return null;
        }
        Map<String, BigDecimal> subjectAmountMap = aggregateExpenseSubjects(ledgerId, expenseList);
        if (subjectAmountMap.isEmpty()) {
            return null;
        }
        validateSubjects(ledgerId, subjectAmountMap.keySet(), PROFIT_SUBJECT_CODE);
        return financeVoucherService.createMonthEndVoucher(ledgerId, yearMonth.toString(), subjectAmountMap, PROFIT_SUBJECT_CODE);
    }

    private Map<String, BigDecimal> aggregateExpenseSubjects(Long ledgerId, List<ErpFinanceExpenseDO> expenseList) {
        Set<Long> voucherIds = new LinkedHashSet<>();
        for (ErpFinanceExpenseDO expense : expenseList) {
            if (!ErpFinanceExpenseAccountingTypeEnum.EXPENSE.getType().equals(expense.getRdAccountingType())) {
                continue;
            }
            ErpFinanceVoucherDO voucher = financeVoucherService.getVoucherByLedgerAndBiz(
                    ledgerId, ErpBizTypeEnum.FINANCE_EXPENSE_EXPENSE.getType(), expense.getId());
            if (voucher == null) {
                voucher = financeVoucherService.getVoucherByLedgerAndBiz(
                        ledgerId, ErpBizTypeEnum.FINANCE_EXPENSE.getType(), expense.getId());
            }
            if (voucher != null) {
                voucherIds.add(voucher.getId());
            }
        }
        if (voucherIds.isEmpty()) {
            return new LinkedHashMap<>();
        }
        List<ErpFinanceVoucherEntryDO> entries = financeVoucherEntryMapper.selectListByVoucherIds(voucherIds);
        Map<String, BigDecimal> subjectAmountMap = new LinkedHashMap<>();
        for (ErpFinanceVoucherEntryDO entry : entries) {
            BigDecimal debitAmount = defaultAmount(entry.getDebitAmount());
            if (debitAmount.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            subjectAmountMap.merge(entry.getSubjectCode(), debitAmount, BigDecimal::add);
        }
        return subjectAmountMap;
    }

    private void validateSubjects(Long ledgerId, Collection<String> expenseSubjectCodes, String profitSubjectCode) {
        Set<String> subjectCodes = new LinkedHashSet<>(expenseSubjectCodes);
        subjectCodes.add(profitSubjectCode);
        Map<String, ErpFinanceSubjectDO> subjectMap = financeSubjectService.getFinanceSubjectMapByLedgerIdAndSubjectCodes(ledgerId, subjectCodes);
        for (String subjectCode : subjectCodes) {
            if (!subjectMap.containsKey(subjectCode)) {
                throw exception(RD_MONTH_END_SUBJECT_MISSING, subjectCode);
            }
        }
    }

    private YearMonth parsePeriod(String period) {
        try {
            return YearMonth.parse(period);
        } catch (Exception ex) {
            throw exception(RD_MONTH_END_PERIOD_INVALID);
        }
    }

    private Long buildMonthEndBizId(YearMonth yearMonth) {
        return yearMonth.getYear() * 100L + yearMonth.getMonthValue();
    }

    private BigDecimal defaultAmount(BigDecimal amount) {
        return ObjectUtil.defaultIfNull(amount, BigDecimal.ZERO);
    }
}
