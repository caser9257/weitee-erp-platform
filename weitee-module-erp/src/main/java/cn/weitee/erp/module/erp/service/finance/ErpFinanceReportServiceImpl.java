package cn.weitee.erp.module.erp.service.finance;

import cn.hutool.core.util.ObjectUtil;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.report.ErpFinanceReportReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.report.ErpFinanceStatementRespVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.report.ErpFinanceTrialBalanceRespVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceLedgerDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinancePeriodDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceReportItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceReportItemSubjectDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceSubjectBalanceDO;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceReportItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceReportItemSubjectMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceSubjectBalanceMapper;
import cn.weitee.erp.module.erp.enums.ErpFinanceReportAmountRuleEnum;
import cn.weitee.erp.module.erp.enums.ErpFinanceReportItemCategoryEnum;
import cn.weitee.erp.module.erp.enums.ErpFinanceReportTypeEnum;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertList;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertMultiMap;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsFinanceLedger.FINANCE_PERIOD_NOT_EXISTS;

@Service
@Validated
public class ErpFinanceReportServiceImpl implements ErpFinanceReportService {

    @Resource
    private ErpFinanceSubjectBalanceMapper erpFinanceSubjectBalanceMapper;
    @Resource
    private ErpFinanceReportItemMapper erpFinanceReportItemMapper;
    @Resource
    private ErpFinanceReportItemSubjectMapper erpFinanceReportItemSubjectMapper;
    @Resource
    private ErpFinanceLedgerService financeLedgerService;
    @Resource
    private ErpFinancePeriodService financePeriodService;

    @Override
    public ErpFinanceTrialBalanceRespVO getTrialBalance(ErpFinanceReportReqVO reqVO) {
        ErpFinanceLedgerDO ledger = financeLedgerService.validateFinanceLedger(reqVO.getLedgerId());
        ErpFinancePeriodDO period = validatePeriod(reqVO.getLedgerId(), reqVO.getPeriodId());
        List<ErpFinanceSubjectBalanceDO> balances = erpFinanceSubjectBalanceMapper.selectListByReportReq(reqVO);

        ErpFinanceTrialBalanceRespVO respVO = new ErpFinanceTrialBalanceRespVO();
        respVO.setLedgerId(ledger.getId());
        respVO.setLedgerName(ledger.getName());
        respVO.setPeriodId(period.getId());
        respVO.setPeriodCode(period.getPeriodCode());
        respVO.setItems(convertList(balances, item -> BeanUtils.toBean(item, ErpFinanceTrialBalanceRespVO.Item.class)));
        respVO.setSubjectCount(balances.size());
        fillTotals(respVO, balances);
        return respVO;
    }

    @Override
    public ErpFinanceStatementRespVO getBalanceSheet(ErpFinanceReportReqVO reqVO) {
        return getStatement(reqVO, ErpFinanceReportTypeEnum.BALANCE_SHEET);
    }

    @Override
    public ErpFinanceStatementRespVO getIncomeStatement(ErpFinanceReportReqVO reqVO) {
        return getStatement(reqVO, ErpFinanceReportTypeEnum.INCOME_STATEMENT);
    }

    @Override
    public ErpFinanceStatementRespVO getCashFlowStatement(ErpFinanceReportReqVO reqVO) {
        return getStatement(reqVO, ErpFinanceReportTypeEnum.CASH_FLOW_STATEMENT);
    }

    private ErpFinanceStatementRespVO getStatement(ErpFinanceReportReqVO reqVO, ErpFinanceReportTypeEnum reportType) {
        ErpFinanceLedgerDO ledger = financeLedgerService.validateFinanceLedger(reqVO.getLedgerId());
        ErpFinancePeriodDO period = validatePeriod(reqVO.getLedgerId(), reqVO.getPeriodId());
        List<ErpFinanceReportItemDO> reportItems = erpFinanceReportItemMapper.selectListByReportReq(reqVO, reportType.getType());
        Map<Long, List<ErpFinanceReportItemSubjectDO>> mappingMap = convertMultiMap(
                erpFinanceReportItemSubjectMapper.selectListByItemIds(convertSet(reportItems, ErpFinanceReportItemDO::getId)),
                ErpFinanceReportItemSubjectDO::getItemId);
        Map<String, ErpFinanceSubjectBalanceDO> balanceMap = convertMap(
                erpFinanceSubjectBalanceMapper.selectListByLedgerIdAndPeriodId(reqVO.getLedgerId(), reqVO.getPeriodId()),
                ErpFinanceSubjectBalanceDO::getSubjectCode);

        ErpFinanceStatementRespVO respVO = new ErpFinanceStatementRespVO();
        respVO.setLedgerId(ledger.getId());
        respVO.setLedgerName(ledger.getName());
        respVO.setPeriodId(period.getId());
        respVO.setPeriodCode(period.getPeriodCode());
        respVO.setReportType(reportType.getType());
        respVO.setReportTypeName(reportType.getName());
        respVO.setItems(convertList(reportItems, item -> buildStatementItem(item, mappingMap.get(item.getId()), balanceMap)));
        fillStatementTotals(respVO, reportType);
        return respVO;
    }

    private ErpFinanceStatementRespVO.Item buildStatementItem(ErpFinanceReportItemDO item,
                                                              List<ErpFinanceReportItemSubjectDO> mappings,
                                                              Map<String, ErpFinanceSubjectBalanceDO> balanceMap) {
        ErpFinanceStatementRespVO.Item respItem = BeanUtils.toBean(item, ErpFinanceStatementRespVO.Item.class, target -> {
            target.setItemId(item.getId());
            target.setItemCategoryName(resolveItemCategoryName(item.getItemCategory()));
        });
        BigDecimal amount = BigDecimal.ZERO;
        if (mappings != null) {
            for (ErpFinanceReportItemSubjectDO mapping : mappings) {
                BigDecimal subjectAmount = resolveAmount(balanceMap.get(mapping.getSubjectCode()), mapping.getAmountRule());
                amount = amount.add(subjectAmount.multiply(BigDecimal.valueOf(ObjectUtil.defaultIfNull(mapping.getAmountSign(), 1))));
            }
        }
        respItem.setAmount(amount);
        return respItem;
    }

    private void fillStatementTotals(ErpFinanceStatementRespVO respVO, ErpFinanceReportTypeEnum reportType) {
        BigDecimal assetAmount = BigDecimal.ZERO;
        BigDecimal liabilityAmount = BigDecimal.ZERO;
        BigDecimal equityAmount = BigDecimal.ZERO;
        BigDecimal revenueAmount = BigDecimal.ZERO;
        BigDecimal costExpenseAmount = BigDecimal.ZERO;
        BigDecimal cashInflowAmount = BigDecimal.ZERO;
        BigDecimal cashOutflowAmount = BigDecimal.ZERO;
        for (ErpFinanceStatementRespVO.Item item : ObjectUtil.defaultIfNull(respVO.getItems(), List.<ErpFinanceStatementRespVO.Item>of())) {
            BigDecimal amount = defaultAmount(item.getAmount());
            if (ErpFinanceReportItemCategoryEnum.ASSET.getType().equals(item.getItemCategory())) {
                assetAmount = assetAmount.add(amount);
            } else if (ErpFinanceReportItemCategoryEnum.LIABILITY.getType().equals(item.getItemCategory())) {
                liabilityAmount = liabilityAmount.add(amount);
            } else if (ErpFinanceReportItemCategoryEnum.EQUITY.getType().equals(item.getItemCategory())) {
                equityAmount = equityAmount.add(amount);
            } else if (ErpFinanceReportItemCategoryEnum.REVENUE.getType().equals(item.getItemCategory())) {
                revenueAmount = revenueAmount.add(amount);
            } else if (ErpFinanceReportItemCategoryEnum.COST_EXPENSE.getType().equals(item.getItemCategory())) {
                costExpenseAmount = costExpenseAmount.add(amount);
            } else if (ErpFinanceReportItemCategoryEnum.CASH_INFLOW.getType().equals(item.getItemCategory())) {
                cashInflowAmount = cashInflowAmount.add(amount);
            } else if (ErpFinanceReportItemCategoryEnum.CASH_OUTFLOW.getType().equals(item.getItemCategory())) {
                cashOutflowAmount = cashOutflowAmount.add(amount);
            }
        }
        if (ErpFinanceReportTypeEnum.BALANCE_SHEET == reportType) {
            respVO.setAssetAmount(assetAmount);
            respVO.setLiabilityAmount(liabilityAmount);
            respVO.setEquityAmount(equityAmount);
            respVO.setBalanceSheetBalanced(assetAmount.compareTo(liabilityAmount.add(equityAmount)) == 0);
        } else if (ErpFinanceReportTypeEnum.INCOME_STATEMENT == reportType) {
            respVO.setRevenueAmount(revenueAmount);
            respVO.setCostExpenseAmount(costExpenseAmount);
            respVO.setProfitAmount(revenueAmount.subtract(costExpenseAmount));
        } else if (ErpFinanceReportTypeEnum.CASH_FLOW_STATEMENT == reportType) {
            respVO.setCashInflowAmount(cashInflowAmount);
            respVO.setCashOutflowAmount(cashOutflowAmount);
            respVO.setNetCashFlowAmount(cashInflowAmount.subtract(cashOutflowAmount));
        }
    }

    private void fillTotals(ErpFinanceTrialBalanceRespVO respVO, List<ErpFinanceSubjectBalanceDO> balances) {
        BigDecimal totalOpeningDebitAmount = BigDecimal.ZERO;
        BigDecimal totalOpeningCreditAmount = BigDecimal.ZERO;
        BigDecimal totalCurrentDebitAmount = BigDecimal.ZERO;
        BigDecimal totalCurrentCreditAmount = BigDecimal.ZERO;
        BigDecimal totalEndingDebitAmount = BigDecimal.ZERO;
        BigDecimal totalEndingCreditAmount = BigDecimal.ZERO;
        for (ErpFinanceSubjectBalanceDO balance : balances) {
            totalOpeningDebitAmount = totalOpeningDebitAmount.add(defaultAmount(balance.getOpeningDebitAmount()));
            totalOpeningCreditAmount = totalOpeningCreditAmount.add(defaultAmount(balance.getOpeningCreditAmount()));
            totalCurrentDebitAmount = totalCurrentDebitAmount.add(defaultAmount(balance.getCurrentDebitAmount()));
            totalCurrentCreditAmount = totalCurrentCreditAmount.add(defaultAmount(balance.getCurrentCreditAmount()));
            totalEndingDebitAmount = totalEndingDebitAmount.add(defaultAmount(balance.getEndingDebitAmount()));
            totalEndingCreditAmount = totalEndingCreditAmount.add(defaultAmount(balance.getEndingCreditAmount()));
        }
        respVO.setTotalOpeningDebitAmount(totalOpeningDebitAmount);
        respVO.setTotalOpeningCreditAmount(totalOpeningCreditAmount);
        respVO.setTotalCurrentDebitAmount(totalCurrentDebitAmount);
        respVO.setTotalCurrentCreditAmount(totalCurrentCreditAmount);
        respVO.setTotalEndingDebitAmount(totalEndingDebitAmount);
        respVO.setTotalEndingCreditAmount(totalEndingCreditAmount);
        respVO.setCurrentBalanced(totalCurrentDebitAmount.compareTo(totalCurrentCreditAmount) == 0);
        respVO.setEndingBalanced(totalEndingDebitAmount.compareTo(totalEndingCreditAmount) == 0);
    }

    private ErpFinancePeriodDO validatePeriod(Long ledgerId, Long periodId) {
        ErpFinancePeriodDO period = financePeriodService.getFinancePeriod(periodId);
        if (period == null || !ObjectUtil.equal(period.getLedgerId(), ledgerId)) {
            throw exception(FINANCE_PERIOD_NOT_EXISTS);
        }
        return period;
    }

    private BigDecimal defaultAmount(BigDecimal amount) {
        return ObjectUtil.defaultIfNull(amount, BigDecimal.ZERO);
    }

    private BigDecimal resolveAmount(ErpFinanceSubjectBalanceDO balance, Integer amountRule) {
        if (balance == null || amountRule == null) {
            return BigDecimal.ZERO;
        }
        if (ErpFinanceReportAmountRuleEnum.OPENING_DEBIT.getType().equals(amountRule)) {
            return defaultAmount(balance.getOpeningDebitAmount());
        }
        if (ErpFinanceReportAmountRuleEnum.OPENING_CREDIT.getType().equals(amountRule)) {
            return defaultAmount(balance.getOpeningCreditAmount());
        }
        if (ErpFinanceReportAmountRuleEnum.CURRENT_DEBIT.getType().equals(amountRule)) {
            return defaultAmount(balance.getCurrentDebitAmount());
        }
        if (ErpFinanceReportAmountRuleEnum.CURRENT_CREDIT.getType().equals(amountRule)) {
            return defaultAmount(balance.getCurrentCreditAmount());
        }
        if (ErpFinanceReportAmountRuleEnum.ENDING_DEBIT.getType().equals(amountRule)) {
            return defaultAmount(balance.getEndingDebitAmount());
        }
        if (ErpFinanceReportAmountRuleEnum.ENDING_CREDIT.getType().equals(amountRule)) {
            return defaultAmount(balance.getEndingCreditAmount());
        }
        if (ErpFinanceReportAmountRuleEnum.CURRENT_NET_DEBIT.getType().equals(amountRule)) {
            return defaultAmount(balance.getCurrentDebitAmount()).subtract(defaultAmount(balance.getCurrentCreditAmount()));
        }
        if (ErpFinanceReportAmountRuleEnum.CURRENT_NET_CREDIT.getType().equals(amountRule)) {
            return defaultAmount(balance.getCurrentCreditAmount()).subtract(defaultAmount(balance.getCurrentDebitAmount()));
        }
        if (ErpFinanceReportAmountRuleEnum.ENDING_NET_DEBIT.getType().equals(amountRule)) {
            return defaultAmount(balance.getEndingDebitAmount()).subtract(defaultAmount(balance.getEndingCreditAmount()));
        }
        if (ErpFinanceReportAmountRuleEnum.ENDING_NET_CREDIT.getType().equals(amountRule)) {
            return defaultAmount(balance.getEndingCreditAmount()).subtract(defaultAmount(balance.getEndingDebitAmount()));
        }
        return BigDecimal.ZERO;
    }

    private String resolveItemCategoryName(Integer itemCategory) {
        for (ErpFinanceReportItemCategoryEnum value : ErpFinanceReportItemCategoryEnum.values()) {
            if (value.getType().equals(itemCategory)) {
                return value.getName();
            }
        }
        return null;
    }

}
