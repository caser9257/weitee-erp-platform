package cn.weitee.erp.module.erp.service.finance;

import cn.weitee.erp.module.erp.controller.admin.mrp.vo.cost.ErpProductionCostDetailRespVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceDualLedgerAmountDiffLogDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceDualLedgerDiffConfigDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceDualWriteConfigDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceDualWriteLogDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceLedgerMappingDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceVoucherDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceVoucherEntryDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionInboundDO;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceDualLedgerAmountDiffLogMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceDualWriteLogMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceVoucherEntryMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceVoucherMapper;
import cn.weitee.erp.module.erp.enums.ErpFinanceDiffCalculationTypeEnum;
import cn.weitee.erp.module.erp.enums.ErpFinanceDualLedgerDiffItemTypeEnum;
import cn.weitee.erp.module.erp.enums.ErpFinanceDualLedgerDiffSourceTypeEnum;
import cn.weitee.erp.module.erp.enums.ErpFinanceDualWriteStatusEnum;
import cn.weitee.erp.module.erp.enums.common.ErpBizTypeEnum;
import cn.weitee.erp.module.erp.service.finance.diffcalc.AmountDiffCalculatorFactory;
import cn.weitee.erp.module.erp.service.finance.diffcalc.FixedVarianceAmountDiffCalculator;
import cn.weitee.erp.module.erp.service.finance.diffcalc.ProRataAmountDiffCalculator;
import cn.weitee.erp.module.erp.service.finance.diffcalc.SourceMappingAmountDiffCalculator;
import cn.weitee.erp.module.erp.service.mrp.ErpProductionCostService;
import cn.weitee.erp.module.erp.service.mrp.ErpProductionInboundService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class ErpFinanceDualWriteServiceTest {

    @Test
    void dualWriteVoucher_whenDisabled_shouldSkip() throws Exception {
        ErpFinanceDualWriteServiceImpl service = new ErpFinanceDualWriteServiceImpl();
        AtomicReference<ErpFinanceDualWriteLogDO> insertedLogRef = new AtomicReference<>();

        // Mock dualWriteLogMapper
        setField(service, "dualWriteLogMapper", createProxy(ErpFinanceDualWriteLogMapper.class, (methodName, args) -> {
            if ("insert".equals(methodName)) {
                insertedLogRef.set((ErpFinanceDualWriteLogDO) args[0]);
                return 1;
            }
            return null;
        }));

        // Mock dualWriteConfigService - 返回null表示未启用
        setField(service, "dualWriteConfigService", createProxy(ErpFinanceDualWriteConfigService.class, (methodName, args) -> {
            if ("getConfigByLedgerId".equals(methodName)) {
                return null;
            }
            return null;
        }));

        ErpFinanceVoucherDO voucher = new ErpFinanceVoucherDO();
        voucher.setId(1L);
        voucher.setLedgerId(100L);
        voucher.setBizType(1);
        voucher.setBizId(10L);

        List<ErpFinanceVoucherEntryDO> entries = new ArrayList<>();

        // 执行双写 - 应该跳过
        service.dualWriteVoucher(voucher, entries, 100L);

        // 验证没有插入日志
        assertNull(insertedLogRef.get());
    }

    @Test
    void dualWriteVoucher_whenNoMapping_shouldSkip() throws Exception {
        ErpFinanceDualWriteServiceImpl service = new ErpFinanceDualWriteServiceImpl();
        AtomicReference<ErpFinanceDualWriteLogDO> insertedLogRef = new AtomicReference<>();

        // Mock dualWriteLogMapper
        setField(service, "dualWriteLogMapper", createProxy(ErpFinanceDualWriteLogMapper.class, (methodName, args) -> {
            if ("insert".equals(methodName)) {
                insertedLogRef.set((ErpFinanceDualWriteLogDO) args[0]);
                return 1;
            }
            return null;
        }));

        // Mock dualWriteConfigService - 返回启用配置
        setField(service, "dualWriteConfigService", createProxy(ErpFinanceDualWriteConfigService.class, (methodName, args) -> {
            if ("getConfigByLedgerId".equals(methodName)) {
                ErpFinanceDualWriteConfigDO config = new ErpFinanceDualWriteConfigDO();
                config.setEnableStatus(ErpFinanceDualWriteConfigDO.ENABLE_YES);
                return config;
            }
            return null;
        }));

        // Mock ledgerMappingService - 返回空列表
        setField(service, "ledgerMappingService", createProxy(ErpFinanceLedgerMappingService.class, (methodName, args) -> {
            if ("getLedgerMappingsByExternalLedger".equals(methodName)) {
                return Collections.emptyList();
            }
            return null;
        }));

        ErpFinanceVoucherDO voucher = new ErpFinanceVoucherDO();
        voucher.setId(1L);
        voucher.setLedgerId(100L);
        voucher.setBizType(1);
        voucher.setBizId(10L);

        List<ErpFinanceVoucherEntryDO> entries = new ArrayList<>();

        // 执行双写 - 应该跳过
        service.dualWriteVoucher(voucher, entries, 100L);

        // 验证没有插入日志
        assertNull(insertedLogRef.get());
    }

    @Test
    void syncDualWriteBySourceVoucherId_shouldLoadVoucherAndEntriesThenRecalculate() throws Exception {
        ErpFinanceDualWriteServiceImpl service = new ErpFinanceDualWriteServiceImpl();
        AtomicReference<ErpFinanceVoucherEntryDO> updatedDebitEntryRef = new AtomicReference<>();
        AtomicReference<ErpFinanceVoucherDO> updatedVoucherRef = new AtomicReference<>();

        setField(service, "voucherService", createProxy(ErpFinanceVoucherService.class, (methodName, args) -> {
            if ("getVoucher".equals(methodName)) {
                Long voucherId = (Long) args[0];
                if (voucherId == 501L) {
                    return new ErpFinanceVoucherDO()
                            .setId(501L)
                            .setLedgerId(99603L)
                            .setBizType(ErpBizTypeEnum.PRODUCTION_INBOUND.getType())
                            .setBizId(1L);
                }
                if (voucherId == 601L) {
                    return new ErpFinanceVoucherDO()
                            .setId(601L)
                            .setLedgerId(99604L)
                            .setBizType(ErpBizTypeEnum.PRODUCTION_INBOUND.getType())
                            .setBizId(1L);
                }
            }
            if ("getVoucherEntryListByVoucherId".equals(methodName)) {
                Long voucherId = (Long) args[0];
                if (voucherId == 501L) {
                    return Arrays.asList(
                            new ErpFinanceVoucherEntryDO().setId(51L).setVoucherId(501L).setEntryNo(1)
                                    .setSummary("自制入库").setSubjectCode("1405").setSubjectName("库存商品")
                                    .setDebitAmount(new BigDecimal("900.00")).setCreditAmount(BigDecimal.ZERO),
                            new ErpFinanceVoucherEntryDO().setId(52L).setVoucherId(501L).setEntryNo(2)
                                    .setSummary("自制入库").setSubjectCode("5001").setSubjectName("生产成本")
                                    .setDebitAmount(BigDecimal.ZERO).setCreditAmount(new BigDecimal("900.00"))
                    );
                }
                if (voucherId == 601L) {
                    return Arrays.asList(
                            new ErpFinanceVoucherEntryDO().setId(61L).setVoucherId(601L).setEntryNo(1)
                                    .setSummary("自制入库").setSubjectCode("1405").setSubjectName("库存商品")
                                    .setDebitAmount(new BigDecimal("900.00")).setCreditAmount(BigDecimal.ZERO),
                            new ErpFinanceVoucherEntryDO().setId(62L).setVoucherId(601L).setEntryNo(2)
                                    .setSummary("自制入库").setSubjectCode("5001").setSubjectName("生产成本")
                                    .setDebitAmount(BigDecimal.ZERO).setCreditAmount(new BigDecimal("900.00"))
                    );
                }
            }
            if ("getVoucherByLedgerAndBiz".equals(methodName)) {
                return new ErpFinanceVoucherDO()
                        .setId(601L)
                        .setLedgerId(99604L)
                        .setBizType(ErpBizTypeEnum.PRODUCTION_INBOUND.getType())
                        .setBizId(1L);
            }
            return null;
        }));
        setField(service, "dualWriteConfigService", createProxy(ErpFinanceDualWriteConfigService.class, (methodName, args) -> {
            if ("getConfigByLedgerId".equals(methodName)) {
                return new ErpFinanceDualWriteConfigDO().setEnableStatus(ErpFinanceDualWriteConfigDO.ENABLE_YES);
            }
            return null;
        }));
        setField(service, "ledgerMappingService", createProxy(ErpFinanceLedgerMappingService.class, (methodName, args) -> {
            if ("getLedgerMappingsByExternalLedger".equals(methodName)) {
                return List.of(new ErpFinanceLedgerMappingDO().setExternalLedgerId(99603L).setInternalLedgerId(99604L));
            }
            return null;
        }));
        setField(service, "financeLedgerService", createProxy(ErpFinanceLedgerService.class, (methodName, args) -> null));
        setField(service, "voucherEntryMapper", createProxy(ErpFinanceVoucherEntryMapper.class, (methodName, args) -> {
            if ("updateById".equals(methodName)) {
                ErpFinanceVoucherEntryDO entry = (ErpFinanceVoucherEntryDO) args[0];
                if (entry.getDebitAmount() != null && entry.getDebitAmount().compareTo(BigDecimal.ZERO) > 0) {
                    updatedDebitEntryRef.set(entry);
                }
                return 1;
            }
            return null;
        }));
        setField(service, "voucherMapper", createProxy(ErpFinanceVoucherMapper.class, (methodName, args) -> {
            if ("updateById".equals(methodName)) {
                updatedVoucherRef.set((ErpFinanceVoucherDO) args[0]);
                return 1;
            }
            return null;
        }));
        setField(service, "dualLedgerAmountDiffLogMapper", createProxy(ErpFinanceDualLedgerAmountDiffLogMapper.class, (methodName, args) -> null));
        setField(service, "dualLedgerDiffConfigService", createProxy(ErpFinanceDualLedgerDiffConfigService.class, (methodName, args) -> {
            if ("getDualLedgerDiffConfigList".equals(methodName)) {
                return List.of(
                        ErpFinanceDualLedgerDiffConfigDO.builder()
                                .bizType(ErpBizTypeEnum.PRODUCTION_INBOUND.getType())
                                .diffItemType(ErpFinanceDualLedgerDiffItemTypeEnum.LABOR.getType())
                                .calculationType(ErpFinanceDiffCalculationTypeEnum.PRO_RATA.getType())
                                .ratio(new BigDecimal("1.1500"))
                                .build()
                );
            }
            return null;
        }));
        setField(service, "productionInboundService", createProxy(ErpProductionInboundService.class, (methodName, args) -> {
            if ("getProductionInbound".equals(methodName)) {
                return new ErpProductionInboundDO().setId(1L).setProductionOrderId(990051L).setTotalCost(new BigDecimal("900.00"));
            }
            return null;
        }));
        setField(service, "productionCostService", createProxy(ErpProductionCostService.class, (methodName, args) -> {
            if ("getCostDetail".equals(methodName)) {
                ErpProductionCostDetailRespVO detail = new ErpProductionCostDetailRespVO();
                detail.setLaborCost(new BigDecimal("200.00"));
                detail.setTotalCost(new BigDecimal("900.00"));
                return detail;
            }
            return null;
        }));
        AmountDiffCalculatorFactory factory = new AmountDiffCalculatorFactory();
        factory.init(Arrays.asList(
                new ProRataAmountDiffCalculator(),
                new FixedVarianceAmountDiffCalculator(),
                new SourceMappingAmountDiffCalculator()
        ));
        setField(service, "amountDiffCalculatorFactory", factory);
        setField(service, "dualWriteLogMapper", createProxy(ErpFinanceDualWriteLogMapper.class, (methodName, args) -> 1));

        service.syncDualWriteBySourceVoucherId(501L);

        assertNotNull(updatedDebitEntryRef.get());
        assertEquals(new BigDecimal("930.00"), updatedDebitEntryRef.get().getDebitAmount());
        assertNotNull(updatedVoucherRef.get());
        assertEquals(new BigDecimal("930.00"), updatedVoucherRef.get().getTotalDebitAmount());
    }

    @Test
    void dualWriteVoucher_whenTargetVoucherAlreadyExists_shouldStillRecalculateAmountDiff() throws Exception {
        ErpFinanceDualWriteServiceImpl service = new ErpFinanceDualWriteServiceImpl();
        AtomicReference<ErpFinanceVoucherEntryDO> updatedDebitEntryRef = new AtomicReference<>();
        AtomicReference<ErpFinanceVoucherEntryDO> updatedCreditEntryRef = new AtomicReference<>();
        AtomicReference<ErpFinanceVoucherDO> updatedVoucherRef = new AtomicReference<>();
        List<ErpFinanceDualLedgerAmountDiffLogDO> insertedDiffLogs = new ArrayList<>();

        setField(service, "dualWriteConfigService", createProxy(ErpFinanceDualWriteConfigService.class, (methodName, args) -> {
            if ("getConfigByLedgerId".equals(methodName)) {
                ErpFinanceDualWriteConfigDO config = new ErpFinanceDualWriteConfigDO();
                config.setEnableStatus(ErpFinanceDualWriteConfigDO.ENABLE_YES);
                return config;
            }
            return null;
        }));
        setField(service, "ledgerMappingService", createProxy(ErpFinanceLedgerMappingService.class, (methodName, args) -> {
            if ("getLedgerMappingsByExternalLedger".equals(methodName)) {
                return List.of(new ErpFinanceLedgerMappingDO().setExternalLedgerId(99603L).setInternalLedgerId(99604L));
            }
            return null;
        }));
        setField(service, "financeLedgerService", createProxy(ErpFinanceLedgerService.class, (methodName, args) -> null));
        setField(service, "voucherService", createProxy(ErpFinanceVoucherService.class, (methodName, args) -> {
            if ("getVoucherByLedgerAndBiz".equals(methodName)) {
                return new ErpFinanceVoucherDO()
                        .setId(202L)
                        .setLedgerId(99604L)
                        .setBizType(ErpBizTypeEnum.PRODUCTION_INBOUND.getType())
                        .setBizId(1L);
            }
            if ("getVoucherEntryListByVoucherId".equals(methodName)) {
                return Arrays.asList(
                        new ErpFinanceVoucherEntryDO().setId(11L).setVoucherId(202L).setEntryNo(1)
                                .setSummary("自制入库").setSubjectCode("1405").setSubjectName("库存商品")
                                .setDebitAmount(new BigDecimal("900.00")).setCreditAmount(BigDecimal.ZERO),
                        new ErpFinanceVoucherEntryDO().setId(12L).setVoucherId(202L).setEntryNo(2)
                                .setSummary("自制入库").setSubjectCode("5001").setSubjectName("生产成本")
                                .setDebitAmount(BigDecimal.ZERO).setCreditAmount(new BigDecimal("900.00"))
                );
            }
            return null;
        }));
        setField(service, "voucherEntryMapper", createProxy(ErpFinanceVoucherEntryMapper.class, (methodName, args) -> {
            if ("updateById".equals(methodName)) {
                ErpFinanceVoucherEntryDO entry = (ErpFinanceVoucherEntryDO) args[0];
                if (entry.getDebitAmount() != null && entry.getDebitAmount().compareTo(BigDecimal.ZERO) > 0) {
                    updatedDebitEntryRef.set(entry);
                }
                if (entry.getCreditAmount() != null && entry.getCreditAmount().compareTo(BigDecimal.ZERO) > 0) {
                    updatedCreditEntryRef.set(entry);
                }
                return 1;
            }
            return null;
        }));
        setField(service, "voucherMapper", createProxy(ErpFinanceVoucherMapper.class, (methodName, args) -> {
            if ("updateById".equals(methodName)) {
                updatedVoucherRef.set((ErpFinanceVoucherDO) args[0]);
                return 1;
            }
            return null;
        }));
        setField(service, "dualLedgerAmountDiffLogMapper", createProxy(ErpFinanceDualLedgerAmountDiffLogMapper.class, (methodName, args) -> {
            if ("deleteByBizTypeAndBizId".equals(methodName)) {
                insertedDiffLogs.clear();
            }
            if ("insertBatch".equals(methodName)) {
                insertedDiffLogs.addAll((List<ErpFinanceDualLedgerAmountDiffLogDO>) args[0]);
            }
            return null;
        }));
        setField(service, "dualLedgerDiffConfigService", createProxy(ErpFinanceDualLedgerDiffConfigService.class, (methodName, args) -> {
            if ("getDualLedgerDiffConfigList".equals(methodName)) {
                return Arrays.asList(
                        ErpFinanceDualLedgerDiffConfigDO.builder()
                                .bizType(ErpBizTypeEnum.PRODUCTION_INBOUND.getType())
                                .diffItemType(ErpFinanceDualLedgerDiffItemTypeEnum.LABOR.getType())
                                .internalSourceType(ErpFinanceDualLedgerDiffSourceTypeEnum.COST_ITEM.getType())
                                .internalSourceValue(ErpFinanceDualLedgerDiffItemTypeEnum.LABOR.getType())
                                .calculationType(ErpFinanceDiffCalculationTypeEnum.PRO_RATA.getType())
                                .ratio(new BigDecimal("1.1500"))
                                .build(),
                        ErpFinanceDualLedgerDiffConfigDO.builder()
                                .bizType(ErpBizTypeEnum.PRODUCTION_INBOUND.getType())
                                .diffItemType(ErpFinanceDualLedgerDiffItemTypeEnum.DEPRECIATION.getType())
                                .internalSourceType(ErpFinanceDualLedgerDiffSourceTypeEnum.ASSET_DEPRECIATION.getType())
                                .calculationType(ErpFinanceDiffCalculationTypeEnum.FIXED_VARIANCE.getType())
                                .fixedAmount(new BigDecimal("-300.00"))
                                .build(),
                        ErpFinanceDualLedgerDiffConfigDO.builder()
                                .bizType(ErpBizTypeEnum.PRODUCTION_INBOUND.getType())
                                .diffItemType(ErpFinanceDualLedgerDiffItemTypeEnum.MANUFACTURING_OVERHEAD.getType())
                                .internalSourceType(ErpFinanceDualLedgerDiffSourceTypeEnum.COST_ITEM.getType())
                                .internalSourceValue(ErpFinanceDualLedgerDiffItemTypeEnum.MANUFACTURING_OVERHEAD.getType())
                                .calculationType(ErpFinanceDiffCalculationTypeEnum.PRO_RATA.getType())
                                .ratio(new BigDecimal("1.1500"))
                                .build()
                );
            }
            return null;
        }));
        setField(service, "productionInboundService", createProxy(ErpProductionInboundService.class, (methodName, args) -> {
            if ("getProductionInbound".equals(methodName)) {
                return new ErpProductionInboundDO().setId(1L).setProductionOrderId(990051L).setTotalCost(new BigDecimal("900.00"));
            }
            return null;
        }));
        setField(service, "productionCostService", createProxy(ErpProductionCostService.class, (methodName, args) -> {
            if ("getCostDetail".equals(methodName)) {
                ErpProductionCostDetailRespVO detail = new ErpProductionCostDetailRespVO();
                detail.setLaborCost(new BigDecimal("200.00"));
                detail.setDepreciationCost(new BigDecimal("50.00"));
                detail.setPowerCost(new BigDecimal("30.00"));
                detail.setOtherCost(new BigDecimal("20.00"));
                detail.setTotalCost(new BigDecimal("900.00"));
                return detail;
            }
            return null;
        }));
        AmountDiffCalculatorFactory factory = new AmountDiffCalculatorFactory();
        factory.init(Arrays.asList(
                new ProRataAmountDiffCalculator(),
                new FixedVarianceAmountDiffCalculator(),
                new SourceMappingAmountDiffCalculator()
        ));
        setField(service, "amountDiffCalculatorFactory", factory);
        setField(service, "dualWriteLogMapper", createProxy(ErpFinanceDualWriteLogMapper.class, (methodName, args) -> {
            if ("insert".equals(methodName)) {
                return 1;
            }
            return null;
        }));

        ErpFinanceVoucherDO sourceVoucher = new ErpFinanceVoucherDO()
                .setId(101L)
                .setLedgerId(99603L)
                .setBizType(ErpBizTypeEnum.PRODUCTION_INBOUND.getType())
                .setBizId(1L);
        List<ErpFinanceVoucherEntryDO> sourceEntries = Arrays.asList(
                new ErpFinanceVoucherEntryDO().setEntryNo(1).setSubjectCode("1405").setSubjectName("库存商品")
                        .setSummary("自制入库").setDebitAmount(new BigDecimal("900.00")).setCreditAmount(BigDecimal.ZERO),
                new ErpFinanceVoucherEntryDO().setEntryNo(2).setSubjectCode("5001").setSubjectName("生产成本")
                        .setSummary("自制入库").setDebitAmount(BigDecimal.ZERO).setCreditAmount(new BigDecimal("900.00"))
        );

        service.dualWriteVoucher(sourceVoucher, sourceEntries, 99603L);

        assertNotNull(updatedDebitEntryRef.get());
        assertNotNull(updatedCreditEntryRef.get());
        assertNotNull(updatedVoucherRef.get());
        assertEquals(new BigDecimal("1237.50"), updatedDebitEntryRef.get().getDebitAmount());
        assertEquals(new BigDecimal("1237.50"), updatedCreditEntryRef.get().getCreditAmount());
        assertEquals(new BigDecimal("1237.50"), updatedVoucherRef.get().getTotalDebitAmount());
        assertEquals(new BigDecimal("1237.50"), updatedVoucherRef.get().getTotalCreditAmount());
        assertEquals(3, insertedDiffLogs.size());
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @SuppressWarnings("unchecked")
    private static <T> T createProxy(Class<T> interfaceType, java.util.function.BiFunction<String, Object[], Object> handler) {
        return (T) Proxy.newProxyInstance(
                interfaceType.getClassLoader(),
                new Class<?>[]{interfaceType},
                (proxy, method, args) -> handler.apply(method.getName(), args)
        );
    }
}
