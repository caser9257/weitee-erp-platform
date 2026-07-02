package cn.weitee.erp.module.erp.service.finance;

import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
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
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ErpFinanceReportServiceImplTest {

    @Test
    void getTrialBalance_shouldReturnTotalsAndBalanceFlags() throws Exception {
        ErpFinanceReportServiceImpl service = new ErpFinanceReportServiceImpl();
        setField(service, "financeLedgerService", createProxy(ErpFinanceLedgerService.class, (methodName, args) -> {
            if ("validateFinanceLedger".equals(methodName)) {
                return new ErpFinanceLedgerDO().setId(1L).setName("标准账簿").setStatus(CommonStatusEnum.ENABLE.getStatus());
            }
            return null;
        }));
        setField(service, "financePeriodService", createProxy(ErpFinancePeriodService.class, (methodName, args) -> {
            if ("getFinancePeriod".equals(methodName)) {
                return new ErpFinancePeriodDO().setId(20L).setLedgerId(1L).setPeriodCode("2026-04")
                        .setStartDate(LocalDate.of(2026, 4, 1)).setEndDate(LocalDate.of(2026, 4, 30));
            }
            return null;
        }));
        setField(service, "financeSubjectBalanceMapper", createProxy(ErpFinanceSubjectBalanceMapper.class, (methodName, args) -> {
            if ("selectListByReportReq".equals(methodName)) {
                return List.of(
                        new ErpFinanceSubjectBalanceDO().setSubjectCode("660201").setSubjectName("管理费用-研发费")
                                .setOpeningDebitAmount(BigDecimal.ZERO).setOpeningCreditAmount(BigDecimal.ZERO)
                                .setCurrentDebitAmount(new BigDecimal("300.00")).setCurrentCreditAmount(BigDecimal.ZERO)
                                .setEndingDebitAmount(new BigDecimal("300.00")).setEndingCreditAmount(BigDecimal.ZERO),
                        new ErpFinanceSubjectBalanceDO().setSubjectCode("220201").setSubjectName("其他应付款")
                                .setOpeningDebitAmount(BigDecimal.ZERO).setOpeningCreditAmount(BigDecimal.ZERO)
                                .setCurrentDebitAmount(BigDecimal.ZERO).setCurrentCreditAmount(new BigDecimal("300.00"))
                                .setEndingDebitAmount(BigDecimal.ZERO).setEndingCreditAmount(new BigDecimal("300.00")));
            }
            return null;
        }));

        ErpFinanceReportReqVO reqVO = new ErpFinanceReportReqVO();
        reqVO.setLedgerId(1L);
        reqVO.setPeriodId(20L);

        ErpFinanceTrialBalanceRespVO respVO = service.getTrialBalance(reqVO);

        assertEquals(1L, respVO.getLedgerId());
        assertEquals("标准账簿", respVO.getLedgerName());
        assertEquals("2026-04", respVO.getPeriodCode());
        assertEquals(2, respVO.getSubjectCount());
        assertEquals(new BigDecimal("300.00"), respVO.getTotalCurrentDebitAmount());
        assertEquals(new BigDecimal("300.00"), respVO.getTotalCurrentCreditAmount());
        assertEquals(new BigDecimal("300.00"), respVO.getTotalEndingDebitAmount());
        assertEquals(new BigDecimal("300.00"), respVO.getTotalEndingCreditAmount());
        assertTrue(respVO.getCurrentBalanced());
        assertTrue(respVO.getEndingBalanced());
        assertEquals(2, respVO.getItems().size());
    }

    @Test
    void getTrialBalance_shouldReturnUnbalancedFlagWhenTotalsMismatch() throws Exception {
        ErpFinanceReportServiceImpl service = new ErpFinanceReportServiceImpl();
        setField(service, "financeLedgerService", createProxy(ErpFinanceLedgerService.class, (methodName, args) -> {
            if ("validateFinanceLedger".equals(methodName)) {
                return new ErpFinanceLedgerDO().setId(1L).setName("标准账簿").setStatus(CommonStatusEnum.ENABLE.getStatus());
            }
            return null;
        }));
        setField(service, "financePeriodService", createProxy(ErpFinancePeriodService.class, (methodName, args) -> {
            if ("getFinancePeriod".equals(methodName)) {
                return new ErpFinancePeriodDO().setId(20L).setLedgerId(1L).setPeriodCode("2026-04");
            }
            return null;
        }));
        setField(service, "financeSubjectBalanceMapper", createProxy(ErpFinanceSubjectBalanceMapper.class, (methodName, args) -> {
            if ("selectListByReportReq".equals(methodName)) {
                return List.of(new ErpFinanceSubjectBalanceDO().setSubjectCode("660201").setSubjectName("管理费用-研发费")
                        .setOpeningDebitAmount(BigDecimal.ZERO).setOpeningCreditAmount(BigDecimal.ZERO)
                        .setCurrentDebitAmount(new BigDecimal("300.00")).setCurrentCreditAmount(BigDecimal.ZERO)
                        .setEndingDebitAmount(new BigDecimal("300.00")).setEndingCreditAmount(BigDecimal.ZERO));
            }
            return null;
        }));

        ErpFinanceReportReqVO reqVO = new ErpFinanceReportReqVO();
        reqVO.setLedgerId(1L);
        reqVO.setPeriodId(20L);

        ErpFinanceTrialBalanceRespVO respVO = service.getTrialBalance(reqVO);

        assertFalse(respVO.getCurrentBalanced());
        assertFalse(respVO.getEndingBalanced());
    }

    @Test
    void getBalanceSheet_shouldCalculateItemsAndBalancedFlagByReportMapping() throws Exception {
        ErpFinanceReportServiceImpl service = buildStatementService(
                List.of(
                        buildReportItem(1L, ErpFinanceReportItemCategoryEnum.ASSET.getType(), "BS-ASSET", "资产合计", 10),
                        buildReportItem(2L, ErpFinanceReportItemCategoryEnum.LIABILITY.getType(), "BS-LIABILITY", "负债合计", 20),
                        buildReportItem(3L, ErpFinanceReportItemCategoryEnum.EQUITY.getType(), "BS-EQUITY", "权益合计", 30)),
                List.of(
                        buildMapping(1L, "1002", ErpFinanceReportAmountRuleEnum.ENDING_DEBIT.getType(), 1),
                        buildMapping(2L, "2202", ErpFinanceReportAmountRuleEnum.ENDING_CREDIT.getType(), 1),
                        buildMapping(3L, "4001", ErpFinanceReportAmountRuleEnum.ENDING_CREDIT.getType(), 1)),
                List.of(
                        buildBalance("1002", "银行存款", "1000.00", "0", "0", "0"),
                        buildBalance("2202", "应付账款", "0", "600.00", "0", "0"),
                        buildBalance("4001", "实收资本", "0", "400.00", "0", "0")));

        ErpFinanceStatementRespVO respVO = service.getBalanceSheet(buildReportReqVO());

        assertEquals(ErpFinanceReportTypeEnum.BALANCE_SHEET.getType(), respVO.getReportType());
        assertEquals(new BigDecimal("1000.00"), respVO.getAssetAmount());
        assertEquals(new BigDecimal("600.00"), respVO.getLiabilityAmount());
        assertEquals(new BigDecimal("400.00"), respVO.getEquityAmount());
        assertTrue(respVO.getBalanceSheetBalanced());
        assertEquals(3, respVO.getItems().size());
    }

    @Test
    void getIncomeStatement_shouldCalculateProfitByReportMapping() throws Exception {
        ErpFinanceReportServiceImpl service = buildStatementService(
                List.of(
                        buildReportItem(1L, ErpFinanceReportItemCategoryEnum.REVENUE.getType(), "IS-REVENUE", "收入合计", 10),
                        buildReportItem(2L, ErpFinanceReportItemCategoryEnum.COST_EXPENSE.getType(), "IS-COST", "成本费用合计", 20)),
                List.of(
                        buildMapping(1L, "6001", ErpFinanceReportAmountRuleEnum.CURRENT_CREDIT.getType(), 1),
                        buildMapping(2L, "6401", ErpFinanceReportAmountRuleEnum.CURRENT_DEBIT.getType(), 1)),
                List.of(
                        buildBalance("6001", "主营业务收入", "0", "0", "0", "1200.00"),
                        buildBalance("6401", "主营业务成本", "0", "0", "350.00", "0")));

        ErpFinanceStatementRespVO respVO = service.getIncomeStatement(buildReportReqVO());

        assertEquals(ErpFinanceReportTypeEnum.INCOME_STATEMENT.getType(), respVO.getReportType());
        assertEquals(new BigDecimal("1200.00"), respVO.getRevenueAmount());
        assertEquals(new BigDecimal("350.00"), respVO.getCostExpenseAmount());
        assertEquals(new BigDecimal("850.00"), respVO.getProfitAmount());
    }

    @Test
    void getCashFlowStatement_shouldCalculateNetCashFlowByReportMapping() throws Exception {
        ErpFinanceReportServiceImpl service = buildStatementService(
                List.of(
                        buildReportItem(1L, ErpFinanceReportItemCategoryEnum.CASH_INFLOW.getType(), "CF-INFLOW", "现金流入合计", 10),
                        buildReportItem(2L, ErpFinanceReportItemCategoryEnum.CASH_OUTFLOW.getType(), "CF-OUTFLOW", "现金流出合计", 20)),
                List.of(
                        buildMapping(1L, "CF01", ErpFinanceReportAmountRuleEnum.CURRENT_DEBIT.getType(), 1),
                        buildMapping(2L, "CF02", ErpFinanceReportAmountRuleEnum.CURRENT_CREDIT.getType(), 1)),
                List.of(
                        buildBalance("CF01", "销售收款现金流", "0", "0", "500.00", "0"),
                        buildBalance("CF02", "采购付款现金流", "0", "0", "0", "120.00")));

        ErpFinanceStatementRespVO respVO = service.getCashFlowStatement(buildReportReqVO());

        assertEquals(ErpFinanceReportTypeEnum.CASH_FLOW_STATEMENT.getType(), respVO.getReportType());
        assertEquals(new BigDecimal("500.00"), respVO.getCashInflowAmount());
        assertEquals(new BigDecimal("120.00"), respVO.getCashOutflowAmount());
        assertEquals(new BigDecimal("380.00"), respVO.getNetCashFlowAmount());
    }

    private ErpFinanceReportServiceImpl buildStatementService(List<ErpFinanceReportItemDO> reportItems,
                                                              List<ErpFinanceReportItemSubjectDO> mappings,
                                                              List<ErpFinanceSubjectBalanceDO> balances) throws Exception {
        ErpFinanceReportServiceImpl service = new ErpFinanceReportServiceImpl();
        setField(service, "financeLedgerService", createProxy(ErpFinanceLedgerService.class, (methodName, args) -> {
            if ("validateFinanceLedger".equals(methodName)) {
                return new ErpFinanceLedgerDO().setId(1L).setName("标准账簿").setStatus(CommonStatusEnum.ENABLE.getStatus());
            }
            return null;
        }));
        setField(service, "financePeriodService", createProxy(ErpFinancePeriodService.class, (methodName, args) -> {
            if ("getFinancePeriod".equals(methodName)) {
                return new ErpFinancePeriodDO().setId(20L).setLedgerId(1L).setPeriodCode("2026-04");
            }
            return null;
        }));
        setField(service, "financeSubjectBalanceMapper", createProxy(ErpFinanceSubjectBalanceMapper.class, (methodName, args) -> {
            if ("selectListByLedgerIdAndPeriodId".equals(methodName)) {
                return balances;
            }
            return null;
        }));
        setField(service, "financeReportItemMapper", createProxy(ErpFinanceReportItemMapper.class, (methodName, args) -> {
            if ("selectListByReportReq".equals(methodName)) {
                return reportItems;
            }
            return null;
        }));
        setField(service, "financeReportItemSubjectMapper", createProxy(ErpFinanceReportItemSubjectMapper.class, (methodName, args) -> {
            if ("selectListByItemIds".equals(methodName)) {
                return mappings;
            }
            return null;
        }));
        return service;
    }

    private ErpFinanceReportReqVO buildReportReqVO() {
        ErpFinanceReportReqVO reqVO = new ErpFinanceReportReqVO();
        reqVO.setLedgerId(1L);
        reqVO.setPeriodId(20L);
        return reqVO;
    }

    private ErpFinanceReportItemDO buildReportItem(Long id, Integer category, String code, String name, Integer sort) {
        return new ErpFinanceReportItemDO().setId(id)
                .setLedgerId(1L)
                .setItemCategory(category)
                .setItemCode(code)
                .setItemName(name)
                .setSort(sort);
    }

    private ErpFinanceReportItemSubjectDO buildMapping(Long itemId, String subjectCode, Integer amountRule, Integer amountSign) {
        return new ErpFinanceReportItemSubjectDO().setItemId(itemId)
                .setSubjectCode(subjectCode)
                .setAmountRule(amountRule)
                .setAmountSign(amountSign);
    }

    private ErpFinanceSubjectBalanceDO buildBalance(String subjectCode, String subjectName, String endingDebit,
                                                    String endingCredit, String currentDebit, String currentCredit) {
        return new ErpFinanceSubjectBalanceDO().setSubjectCode(subjectCode).setSubjectName(subjectName)
                .setOpeningDebitAmount(BigDecimal.ZERO).setOpeningCreditAmount(BigDecimal.ZERO)
                .setCurrentDebitAmount(new BigDecimal(currentDebit)).setCurrentCreditAmount(new BigDecimal(currentCredit))
                .setEndingDebitAmount(new BigDecimal(endingDebit)).setEndingCreditAmount(new BigDecimal(endingCredit));
    }

    @SuppressWarnings("unchecked")
    private <T> T createProxy(Class<T> type, MethodHandler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type},
                (proxy, method, args) -> {
                    if (method.getDeclaringClass() == Object.class) {
                        if ("toString".equals(method.getName())) {
                            return type.getSimpleName() + "Proxy";
                        }
                        if ("hashCode".equals(method.getName())) {
                            return System.identityHashCode(proxy);
                        }
                        if ("equals".equals(method.getName())) {
                            return proxy == args[0];
                        }
                    }
                    return handler.handle(method.getName(), args);
                });
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(mapFieldName(fieldName));
        field.setAccessible(true);
        field.set(target, value);
    }

    private String mapFieldName(String fieldName) {
        return switch (fieldName) {
            case "financeSubjectBalanceMapper" -> "erpFinanceSubjectBalanceMapper";
            case "financeReportItemMapper" -> "erpFinanceReportItemMapper";
            case "financeReportItemSubjectMapper" -> "erpFinanceReportItemSubjectMapper";
            default -> fieldName;
        };
    }

    @FunctionalInterface
    private interface MethodHandler {
        Object handle(String methodName, Object[] args);
    }

}
