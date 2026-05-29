package cn.iocoder.yudao.module.erp.service.finance;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.generalledger.ErpFinanceGeneralLedgerRebuildReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.generalledger.ErpFinanceGeneralLedgerRebuildRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.generalledger.ErpFinanceGeneralLedgerDetailReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.generalledger.ErpFinanceGeneralLedgerDetailRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.generalledger.ErpFinanceSubjectBalancePageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceLedgerDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinancePeriodDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceSubjectBalanceDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceVoucherDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceVoucherEntryDO;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinanceSubjectBalanceMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinanceVoucherEntryMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinanceVoucherMapper;
import cn.iocoder.yudao.module.erp.enums.ErpFinanceVoucherStatusEnum;
import cn.iocoder.yudao.module.erp.enums.common.ErpBizTypeEnum;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ErpFinanceGeneralLedgerServiceImplTest {

    @Test
    void applyPostedVoucher_shouldInsertBalanceAndCascadeLaterPeriods() throws Exception {
        ErpFinanceGeneralLedgerServiceImpl service = new ErpFinanceGeneralLedgerServiceImpl();
        List<ErpFinanceSubjectBalanceDO> balances = new ArrayList<>();
        balances.add(new ErpFinanceSubjectBalanceDO()
                .setId(1L)
                .setLedgerId(1L)
                .setPeriodId(21L)
                .setPeriodSort(202605)
                .setSubjectCode("660201")
                .setSubjectName("管理费用-研发费")
                .setOpeningDebitAmount(new BigDecimal("100.00"))
                .setOpeningCreditAmount(BigDecimal.ZERO)
                .setCurrentDebitAmount(new BigDecimal("20.00"))
                .setCurrentCreditAmount(BigDecimal.ZERO)
                .setEndingDebitAmount(new BigDecimal("120.00"))
                .setEndingCreditAmount(BigDecimal.ZERO));
        setField(service, "financePeriodService", createProxy(ErpFinancePeriodService.class, (methodName, args) -> {
            if ("getFinancePeriod".equals(methodName)) {
                return new ErpFinancePeriodDO().setId(20L).setLedgerId(1L).setPeriodSort(202604)
                        .setPeriodCode("2026-04").setStartDate(LocalDate.of(2026, 4, 1)).setEndDate(LocalDate.of(2026, 4, 30));
            }
            return null;
        }));
        setField(service, "financeSubjectBalanceMapper", createProxy(ErpFinanceSubjectBalanceMapper.class, (methodName, args) -> {
            if ("selectByLedgerIdAndPeriodIdAndSubjectCode".equals(methodName)) {
                return balances.stream().filter(item -> Objects.equals(item.getLedgerId(), args[0])
                        && Objects.equals(item.getPeriodId(), args[1])
                        && Objects.equals(item.getSubjectCode(), args[2])).findFirst().orElse(null);
            }
            if ("selectLatestBeforePeriod".equals(methodName)) {
                return balances.stream()
                        .filter(item -> Objects.equals(item.getLedgerId(), args[0])
                                && Objects.equals(item.getSubjectCode(), args[1])
                                && item.getPeriodSort() < (Integer) args[2])
                        .max(java.util.Comparator.comparing(ErpFinanceSubjectBalanceDO::getPeriodSort))
                        .orElse(null);
            }
            if ("selectListAfterPeriod".equals(methodName)) {
                return balances.stream()
                        .filter(item -> Objects.equals(item.getLedgerId(), args[0])
                                && Objects.equals(item.getSubjectCode(), args[1])
                                && item.getPeriodSort() > (Integer) args[2])
                        .sorted(java.util.Comparator.comparing(ErpFinanceSubjectBalanceDO::getPeriodSort))
                        .toList();
            }
            if ("insert".equals(methodName)) {
                ErpFinanceSubjectBalanceDO balance = (ErpFinanceSubjectBalanceDO) args[0];
                balance.setId(2L);
                balances.add(balance);
                return 1;
            }
            if ("updateById".equals(methodName)) {
                ErpFinanceSubjectBalanceDO updateObj = (ErpFinanceSubjectBalanceDO) args[0];
                balances.replaceAll(item -> Objects.equals(item.getId(), updateObj.getId()) ? updateObj : item);
                return 1;
            }
            return null;
        }));

        service.applyPostedVoucher(new ErpFinanceVoucherDO()
                        .setId(11L)
                        .setLedgerId(1L)
                        .setPeriodId(20L)
                        .setVoucherTime(LocalDateTime.of(2026, 4, 29, 10, 0)),
                List.of(new ErpFinanceVoucherEntryDO()
                        .setVoucherId(11L)
                        .setEntryNo(1)
                        .setSubjectCode("660201")
                        .setSubjectName("管理费用-研发费")
                        .setDebitAmount(new BigDecimal("300.00"))
                        .setCreditAmount(BigDecimal.ZERO)));

        ErpFinanceSubjectBalanceDO currentBalance = balances.stream()
                .filter(item -> Objects.equals(item.getPeriodId(), 20L) && Objects.equals(item.getSubjectCode(), "660201"))
                .findFirst().orElseThrow();
        ErpFinanceSubjectBalanceDO nextBalance = balances.stream()
                .filter(item -> Objects.equals(item.getPeriodId(), 21L) && Objects.equals(item.getSubjectCode(), "660201"))
                .findFirst().orElseThrow();
        assertEquals(new BigDecimal("300.00"), currentBalance.getCurrentDebitAmount());
        assertEquals(new BigDecimal("300.00"), currentBalance.getEndingDebitAmount());
        assertEquals(new BigDecimal("300.00"), nextBalance.getOpeningDebitAmount());
        assertEquals(new BigDecimal("320.00"), nextBalance.getEndingDebitAmount());
    }

    @Test
    void rollbackPostedVoucher_shouldSubtractBalanceAndCascadeLaterPeriods() throws Exception {
        ErpFinanceGeneralLedgerServiceImpl service = new ErpFinanceGeneralLedgerServiceImpl();
        List<ErpFinanceSubjectBalanceDO> balances = new ArrayList<>();
        balances.add(new ErpFinanceSubjectBalanceDO()
                .setId(1L)
                .setLedgerId(1L)
                .setPeriodId(20L)
                .setPeriodSort(202604)
                .setSubjectCode("660201")
                .setSubjectName("管理费用-研发费")
                .setOpeningDebitAmount(new BigDecimal("100.00"))
                .setOpeningCreditAmount(BigDecimal.ZERO)
                .setCurrentDebitAmount(new BigDecimal("300.00"))
                .setCurrentCreditAmount(BigDecimal.ZERO)
                .setEndingDebitAmount(new BigDecimal("400.00"))
                .setEndingCreditAmount(BigDecimal.ZERO));
        balances.add(new ErpFinanceSubjectBalanceDO()
                .setId(2L)
                .setLedgerId(1L)
                .setPeriodId(21L)
                .setPeriodSort(202605)
                .setSubjectCode("660201")
                .setSubjectName("管理费用-研发费")
                .setOpeningDebitAmount(new BigDecimal("400.00"))
                .setOpeningCreditAmount(BigDecimal.ZERO)
                .setCurrentDebitAmount(new BigDecimal("20.00"))
                .setCurrentCreditAmount(BigDecimal.ZERO)
                .setEndingDebitAmount(new BigDecimal("420.00"))
                .setEndingCreditAmount(BigDecimal.ZERO));
        setField(service, "financePeriodService", createProxy(ErpFinancePeriodService.class, (methodName, args) -> {
            if ("getFinancePeriod".equals(methodName)) {
                return new ErpFinancePeriodDO().setId(20L).setLedgerId(1L).setPeriodSort(202604)
                        .setPeriodCode("2026-04").setStartDate(LocalDate.of(2026, 4, 1)).setEndDate(LocalDate.of(2026, 4, 30));
            }
            return null;
        }));
        setField(service, "financeSubjectBalanceMapper", createProxy(ErpFinanceSubjectBalanceMapper.class, (methodName, args) -> {
            if ("selectByLedgerIdAndPeriodIdAndSubjectCode".equals(methodName)) {
                return balances.stream().filter(item -> Objects.equals(item.getLedgerId(), args[0])
                        && Objects.equals(item.getPeriodId(), args[1])
                        && Objects.equals(item.getSubjectCode(), args[2])).findFirst().orElse(null);
            }
            if ("selectLatestBeforePeriod".equals(methodName)) {
                return balances.stream()
                        .filter(item -> Objects.equals(item.getLedgerId(), args[0])
                                && Objects.equals(item.getSubjectCode(), args[1])
                                && item.getPeriodSort() < (Integer) args[2])
                        .max(java.util.Comparator.comparing(ErpFinanceSubjectBalanceDO::getPeriodSort))
                        .orElse(null);
            }
            if ("selectListAfterPeriod".equals(methodName)) {
                return balances.stream()
                        .filter(item -> Objects.equals(item.getLedgerId(), args[0])
                                && Objects.equals(item.getSubjectCode(), args[1])
                                && item.getPeriodSort() > (Integer) args[2])
                        .sorted(java.util.Comparator.comparing(ErpFinanceSubjectBalanceDO::getPeriodSort))
                        .toList();
            }
            if ("updateById".equals(methodName)) {
                ErpFinanceSubjectBalanceDO updateObj = (ErpFinanceSubjectBalanceDO) args[0];
                balances.replaceAll(item -> Objects.equals(item.getId(), updateObj.getId()) ? updateObj : item);
                return 1;
            }
            return null;
        }));

        service.rollbackPostedVoucher(new ErpFinanceVoucherDO()
                        .setId(11L)
                        .setLedgerId(1L)
                        .setPeriodId(20L)
                        .setVoucherTime(LocalDateTime.of(2026, 4, 29, 10, 0)),
                List.of(new ErpFinanceVoucherEntryDO()
                        .setVoucherId(11L)
                        .setEntryNo(1)
                        .setSubjectCode("660201")
                        .setSubjectName("管理费用-研发费")
                        .setDebitAmount(new BigDecimal("300.00"))
                        .setCreditAmount(BigDecimal.ZERO)));

        ErpFinanceSubjectBalanceDO currentBalance = balances.stream()
                .filter(item -> Objects.equals(item.getPeriodId(), 20L) && Objects.equals(item.getSubjectCode(), "660201"))
                .findFirst().orElseThrow();
        ErpFinanceSubjectBalanceDO nextBalance = balances.stream()
                .filter(item -> Objects.equals(item.getPeriodId(), 21L) && Objects.equals(item.getSubjectCode(), "660201"))
                .findFirst().orElseThrow();
        assertEquals(new BigDecimal("100.00"), currentBalance.getEndingDebitAmount());
        assertEquals(new BigDecimal("100.00"), nextBalance.getOpeningDebitAmount());
        assertEquals(new BigDecimal("120.00"), nextBalance.getEndingDebitAmount());
    }

    @Test
    void getGeneralLedgerDetail_shouldBuildOpeningAndRunningBalance() throws Exception {
        ErpFinanceGeneralLedgerServiceImpl service = new ErpFinanceGeneralLedgerServiceImpl();
        setField(service, "financeLedgerService", createProxy(ErpFinanceLedgerService.class, (methodName, args) -> {
            if ("validateFinanceLedger".equals(methodName)) {
                return new ErpFinanceLedgerDO().setId(1L).setName("标准账簿").setStatus(CommonStatusEnum.ENABLE.getStatus());
            }
            return null;
        }));
        setField(service, "financePeriodService", createProxy(ErpFinancePeriodService.class, (methodName, args) -> {
            if ("getFinancePeriod".equals(methodName)) {
                return new ErpFinancePeriodDO().setId(20L).setLedgerId(1L).setPeriodSort(202604)
                        .setPeriodCode("2026-04").setStartDate(LocalDate.of(2026, 4, 1)).setEndDate(LocalDate.of(2026, 4, 30));
            }
            return null;
        }));
        setField(service, "financeSubjectBalanceMapper", createProxy(ErpFinanceSubjectBalanceMapper.class, (methodName, args) -> {
            if ("selectByLedgerIdAndPeriodIdAndSubjectCode".equals(methodName)) {
                return new ErpFinanceSubjectBalanceDO()
                        .setLedgerId(1L)
                        .setPeriodId(20L)
                        .setSubjectCode("660201")
                        .setSubjectName("管理费用-研发费")
                        .setOpeningDebitAmount(new BigDecimal("100.00"))
                        .setOpeningCreditAmount(BigDecimal.ZERO)
                        .setCurrentDebitAmount(new BigDecimal("80.00"))
                        .setCurrentCreditAmount(BigDecimal.ZERO)
                        .setEndingDebitAmount(new BigDecimal("180.00"))
                        .setEndingCreditAmount(BigDecimal.ZERO);
            }
            if ("selectPage".equals(methodName)) {
                return new PageResult<>(List.of(), 0L);
            }
            return null;
        }));
        setField(service, "financeVoucherMapper", createProxy(ErpFinanceVoucherMapper.class, (methodName, args) -> {
            if ("selectPostedListByLedgerIdAndPeriodId".equals(methodName)) {
                return List.of(
                        new ErpFinanceVoucherDO().setId(1L).setVoucherNo("CWPZ1").setLedgerId(1L).setPeriodId(20L)
                                .setBizType(ErpBizTypeEnum.FINANCE_EXPENSE.getType()).setBizNo("BIZ-1")
                                .setStatus(ErpFinanceVoucherStatusEnum.POSTED.getStatus())
                                .setVoucherTime(LocalDateTime.of(2026, 4, 10, 9, 0)),
                        new ErpFinanceVoucherDO().setId(2L).setVoucherNo("CWPZ2").setLedgerId(1L).setPeriodId(20L)
                                .setBizType(ErpBizTypeEnum.FINANCE_EXPENSE.getType()).setBizNo("BIZ-2")
                                .setStatus(ErpFinanceVoucherStatusEnum.REVERSED.getStatus())
                                .setVoucherTime(LocalDateTime.of(2026, 4, 20, 9, 0)));
            }
            return null;
        }));
        setField(service, "financeVoucherEntryMapper", createProxy(ErpFinanceVoucherEntryMapper.class, (methodName, args) -> {
            if ("selectListByVoucherIdsAndSubjectCode".equals(methodName)) {
                return List.of(
                        new ErpFinanceVoucherEntryDO().setVoucherId(1L).setEntryNo(1).setSummary("前置分录")
                                .setSubjectCode("660201").setSubjectName("管理费用-研发费")
                                .setDebitAmount(new BigDecimal("50.00")).setCreditAmount(BigDecimal.ZERO),
                        new ErpFinanceVoucherEntryDO().setVoucherId(2L).setEntryNo(1).setSummary("区间分录")
                                .setSubjectCode("660201").setSubjectName("管理费用-研发费")
                                .setDebitAmount(new BigDecimal("30.00")).setCreditAmount(BigDecimal.ZERO));
            }
            return null;
        }));

        ErpFinanceGeneralLedgerDetailReqVO reqVO = new ErpFinanceGeneralLedgerDetailReqVO();
        reqVO.setLedgerId(1L);
        reqVO.setPeriodId(20L);
        reqVO.setSubjectCode("660201");
        reqVO.setVoucherTime(new LocalDateTime[]{
                LocalDateTime.of(2026, 4, 15, 0, 0),
                LocalDateTime.of(2026, 4, 30, 23, 59)
        });

        ErpFinanceGeneralLedgerDetailRespVO respVO = service.getGeneralLedgerDetail(reqVO);

        assertEquals(new BigDecimal("150.00"), respVO.getOpeningDebitAmount());
        assertEquals(new BigDecimal("30.00"), respVO.getTotalDebitAmount());
        assertEquals(new BigDecimal("180.00"), respVO.getEndingDebitAmount());
        assertEquals(1, respVO.getItems().size());
        assertEquals("CWPZ2", respVO.getItems().get(0).getVoucherNo());
        assertEquals(new BigDecimal("180.00"), respVO.getItems().get(0).getRunningDebitAmount());
    }

    @Test
    void rebuildSubjectBalance_shouldClearLedgerBalancesAndReplayPostedVouchers() throws Exception {
        ErpFinanceGeneralLedgerServiceImpl service = new ErpFinanceGeneralLedgerServiceImpl();
        AtomicBoolean deleted = new AtomicBoolean(false);
        List<ErpFinanceSubjectBalanceDO> insertedBalances = new ArrayList<>();
        setField(service, "financeLedgerService", createProxy(ErpFinanceLedgerService.class, (methodName, args) -> {
            if ("validateFinanceLedger".equals(methodName)) {
                return new ErpFinanceLedgerDO().setId(1L).setName("标准账簿").setStatus(CommonStatusEnum.ENABLE.getStatus());
            }
            return null;
        }));
        setField(service, "financePeriodService", createProxy(ErpFinancePeriodService.class, (methodName, args) -> {
            if ("getFinancePeriod".equals(methodName)) {
                return new ErpFinancePeriodDO().setId((Long) args[0]).setLedgerId(1L).setPeriodSort(202604)
                        .setPeriodCode("2026-04").setStartDate(LocalDate.of(2026, 4, 1)).setEndDate(LocalDate.of(2026, 4, 30));
            }
            return null;
        }));
        setField(service, "financeVoucherMapper", createProxy(ErpFinanceVoucherMapper.class, (methodName, args) -> {
            if ("selectPostedListByLedgerId".equals(methodName)) {
                return List.of(new ErpFinanceVoucherDO().setId(11L).setLedgerId(1L).setPeriodId(20L)
                        .setVoucherTime(LocalDateTime.of(2026, 4, 10, 9, 0))
                        .setStatus(ErpFinanceVoucherStatusEnum.POSTED.getStatus()));
            }
            return null;
        }));
        setField(service, "financeVoucherEntryMapper", createProxy(ErpFinanceVoucherEntryMapper.class, (methodName, args) -> {
            if ("selectListByVoucherId".equals(methodName)) {
                return List.of(new ErpFinanceVoucherEntryDO().setVoucherId(11L).setSubjectCode("660201")
                        .setSubjectName("管理费用-研发费").setDebitAmount(new BigDecimal("300.00")).setCreditAmount(BigDecimal.ZERO));
            }
            return null;
        }));
        setField(service, "financeSubjectBalanceMapper", createProxy(ErpFinanceSubjectBalanceMapper.class, (methodName, args) -> {
            if ("deleteByLedgerId".equals(methodName)) {
                deleted.set(true);
                return 2;
            }
            if ("selectByLedgerIdAndPeriodIdAndSubjectCode".equals(methodName) || "selectLatestBeforePeriod".equals(methodName)) {
                return null;
            }
            if ("selectListAfterPeriod".equals(methodName)) {
                return List.of();
            }
            if ("insert".equals(methodName)) {
                insertedBalances.add((ErpFinanceSubjectBalanceDO) args[0]);
                return 1;
            }
            return null;
        }));

        ErpFinanceGeneralLedgerRebuildReqVO reqVO = new ErpFinanceGeneralLedgerRebuildReqVO();
        reqVO.setLedgerId(1L);
        ErpFinanceGeneralLedgerRebuildRespVO respVO = service.rebuildSubjectBalance(reqVO);

        assertEquals(true, deleted.get());
        assertEquals(1, respVO.getVoucherCount());
        assertEquals(1, respVO.getEntryCount());
        assertEquals(1, respVO.getSubjectCount());
        assertEquals("660201", insertedBalances.get(0).getSubjectCode());
        assertEquals(new BigDecimal("300.00"), insertedBalances.get(0).getEndingDebitAmount());
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
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @FunctionalInterface
    private interface MethodHandler {
        Object handle(String methodName, Object[] args);
    }

}
