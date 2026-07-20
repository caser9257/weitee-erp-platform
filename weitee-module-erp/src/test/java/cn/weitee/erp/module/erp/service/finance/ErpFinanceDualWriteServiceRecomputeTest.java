package cn.weitee.erp.module.erp.service.finance;

import cn.weitee.erp.module.erp.controller.admin.mrp.vo.cost.ErpProductionCostDetailRespVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceDualLedgerDiffConfigDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceDualLedgerAmountDiffLogDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceDualWriteConfigDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceDualWriteLogDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceLedgerMappingDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceVoucherDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceVoucherEntryDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionInboundDO;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceDualLedgerAmountDiffLogMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceDualWriteLogMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceVoucherMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceVoucherEntryMapper;
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
import cn.weitee.erp.framework.common.exception.ServiceException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsFinanceLedger.FINANCE_DUAL_LEDGER_DIFF_CALCULATION_DIRECTION_INVALID;

/**
 * 双写服务重算功能单元测试
 */
class ErpFinanceDualWriteServiceRecomputeTest {

    @Test
    void calculateExternalAmount_shouldRejectReverseResult() throws Exception {
        ErpFinanceDualWriteServiceImpl service = new ErpFinanceDualWriteServiceImpl();
        AmountDiffCalculatorFactory factory = new AmountDiffCalculatorFactory();
        factory.init(Arrays.asList(
                new ProRataAmountDiffCalculator(),
                new FixedVarianceAmountDiffCalculator(),
                new SourceMappingAmountDiffCalculator()
        ));
        setField(service, "amountDiffCalculatorFactory", factory);

        ErpFinanceDualLedgerDiffConfigDO config = ErpFinanceDualLedgerDiffConfigDO.builder()
                .calculationType(ErpFinanceDiffCalculationTypeEnum.PRO_RATA.getType())
                .ratio(new BigDecimal("0.85"))
                .build();
        Method method = ErpFinanceDualWriteServiceImpl.class
                .getDeclaredMethod("calculateExternalAmount", ErpFinanceDualLedgerDiffConfigDO.class, BigDecimal.class);
        method.setAccessible(true);

        InvocationTargetException ex = assertThrows(InvocationTargetException.class,
                () -> method.invoke(service, config, new BigDecimal("100.00")));

        assertInstanceOf(ServiceException.class, ex.getCause());
        assertEquals(FINANCE_DUAL_LEDGER_DIFF_CALCULATION_DIRECTION_INVALID.getCode(),
                ((ServiceException) ex.getCause()).getCode());
    }

    @Test
    void recomputeByBizId_shouldUseProductionCostDetailForProductionInbound() throws Exception {
        ErpFinanceDualWriteServiceImpl service = new ErpFinanceDualWriteServiceImpl();
        AtomicReference<ErpFinanceVoucherEntryDO> updatedDebitEntryRef = new AtomicReference<>();
        AtomicReference<ErpFinanceVoucherEntryDO> updatedCreditEntryRef = new AtomicReference<>();
        AtomicReference<ErpFinanceVoucherDO> updatedVoucherRef = new AtomicReference<>();
        List<ErpFinanceDualLedgerAmountDiffLogDO> insertedLogs = new ArrayList<>();

        setField(service, "dualWriteLogMapper", createProxy(ErpFinanceDualWriteLogMapper.class, (methodName, args) -> {
            if ("selectLatestByBizTypeAndBizId".equals(methodName)) {
                return ErpFinanceDualWriteLogDO.builder()
                        .id(10L)
                        .sourceVoucherId(301L)
                        .targetVoucherId(302L)
                        .bizType(ErpBizTypeEnum.PRODUCTION_INBOUND.getType())
                        .bizId(700L)
                        .status(ErpFinanceDualWriteStatusEnum.SUCCESS.getStatus())
                        .build();
            }
            return null;
        }));

        setField(service, "voucherService", createProxy(ErpFinanceVoucherService.class, (methodName, args) -> {
            if ("getVoucher".equals(methodName)) {
                Long voucherId = (Long) args[0];
                return ErpFinanceVoucherDO.builder()
                        .id(voucherId)
                        .bizType(ErpBizTypeEnum.PRODUCTION_INBOUND.getType())
                        .bizId(700L)
                        .build();
            }
            if ("getVoucherEntryListByVoucherId".equals(methodName)) {
                Long voucherId = (Long) args[0];
                if (voucherId == 301L || voucherId == 302L) {
                    return Arrays.asList(
                            ErpFinanceVoucherEntryDO.builder()
                                    .id(voucherId == 301L ? 11L : 21L)
                                    .voucherId(voucherId)
                                    .entryNo(1)
                                    .summary("自制入库")
                                    .subjectCode("1405")
                                    .subjectName("库存商品")
                                    .debitAmount(new BigDecimal("900.00"))
                                    .creditAmount(BigDecimal.ZERO)
                                    .build(),
                            ErpFinanceVoucherEntryDO.builder()
                                    .id(voucherId == 301L ? 12L : 22L)
                                    .voucherId(voucherId)
                                    .entryNo(2)
                                    .summary("自制入库")
                                    .subjectCode("5001")
                                    .subjectName("生产成本")
                                    .debitAmount(BigDecimal.ZERO)
                                    .creditAmount(new BigDecimal("900.00"))
                                    .build()
                    );
                }
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
                insertedLogs.clear();
                return null;
            }
            if ("insertBatch".equals(methodName)) {
                insertedLogs.addAll((List<ErpFinanceDualLedgerAmountDiffLogDO>) args[0]);
                return null;
            }
            return null;
        }));

        setField(service, "dualLedgerDiffConfigService", createProxy(ErpFinanceDualLedgerDiffConfigService.class, (methodName, args) -> {
            if ("getDualLedgerDiffConfigList".equals(methodName)) {
                return Arrays.asList(
                        ErpFinanceDualLedgerDiffConfigDO.builder()
                                .id(101L)
                                .bizType(ErpBizTypeEnum.PRODUCTION_INBOUND.getType())
                                .diffItemType(ErpFinanceDualLedgerDiffItemTypeEnum.LABOR.getType())
                                .internalSourceType(ErpFinanceDualLedgerDiffSourceTypeEnum.COST_ITEM.getType())
                                .internalSourceValue(ErpFinanceDualLedgerDiffItemTypeEnum.LABOR.getType())
                                .calculationType(ErpFinanceDiffCalculationTypeEnum.PRO_RATA.getType())
                                .ratio(new BigDecimal("1.15"))
                                .build(),
                        ErpFinanceDualLedgerDiffConfigDO.builder()
                                .id(102L)
                                .bizType(ErpBizTypeEnum.PRODUCTION_INBOUND.getType())
                                .diffItemType(ErpFinanceDualLedgerDiffItemTypeEnum.DEPRECIATION.getType())
                                .internalSourceType(ErpFinanceDualLedgerDiffSourceTypeEnum.COST_ITEM.getType())
                                .internalSourceValue(ErpFinanceDualLedgerDiffItemTypeEnum.DEPRECIATION.getType())
                                .calculationType(ErpFinanceDiffCalculationTypeEnum.FIXED_VARIANCE.getType())
                                .fixedAmount(new BigDecimal("-30.00"))
                                .build(),
                        ErpFinanceDualLedgerDiffConfigDO.builder()
                                .id(103L)
                                .bizType(ErpBizTypeEnum.PRODUCTION_INBOUND.getType())
                                .diffItemType(ErpFinanceDualLedgerDiffItemTypeEnum.MANUFACTURING_OVERHEAD.getType())
                                .internalSourceType(ErpFinanceDualLedgerDiffSourceTypeEnum.COST_ITEM.getType())
                                .internalSourceValue(ErpFinanceDualLedgerDiffItemTypeEnum.MANUFACTURING_OVERHEAD.getType())
                                .calculationType(ErpFinanceDiffCalculationTypeEnum.PRO_RATA.getType())
                                .ratio(new BigDecimal("1.15"))
                                .build()
                );
            }
            return null;
        }));

        setField(service, "productionInboundService", createProxy(ErpProductionInboundService.class, (methodName, args) -> {
            if ("getProductionInbound".equals(methodName)) {
                return new ErpProductionInboundDO()
                        .setId(700L)
                        .setProductionOrderId(880L)
                        .setTotalCost(new BigDecimal("900.00"));
            }
            return null;
        }));
        setField(service, "productionCostService", createProxy(ErpProductionCostService.class, (methodName, args) -> {
            if ("getCostDetail".equals(methodName)) {
                return buildProductionCostDetail(
                        new BigDecimal("200.00"),
                        new BigDecimal("100.00"),
                        BigDecimal.ZERO,
                        new BigDecimal("100.00"),
                        new BigDecimal("900.00"));
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

        boolean result = service.recomputeByBizId(ErpBizTypeEnum.PRODUCTION_INBOUND.getType(), 700L);

        assertTrue(result);
        assertNotNull(updatedDebitEntryRef.get());
        assertNotNull(updatedCreditEntryRef.get());
        assertNotNull(updatedVoucherRef.get());
        assertEquals(new BigDecimal("975.00"), updatedDebitEntryRef.get().getDebitAmount());
        assertEquals(new BigDecimal("975.00"), updatedCreditEntryRef.get().getCreditAmount());
        assertEquals(new BigDecimal("975.00"), updatedVoucherRef.get().getTotalDebitAmount());
        assertEquals(new BigDecimal("975.00"), updatedVoucherRef.get().getTotalCreditAmount());
        assertEquals(3, insertedLogs.size());
    }

    @Test
    void recomputeByBizId_shouldNotDoubleCountPowerCostForOtherCost() throws Exception {
        ErpFinanceDualWriteServiceImpl service = new ErpFinanceDualWriteServiceImpl();
        AtomicReference<ErpFinanceVoucherDO> updatedVoucherRef = new AtomicReference<>();
        List<ErpFinanceDualLedgerAmountDiffLogDO> insertedLogs = new ArrayList<>();

        setField(service, "dualWriteLogMapper", createProxy(ErpFinanceDualWriteLogMapper.class, (methodName, args) -> {
            if ("selectLatestByBizTypeAndBizId".equals(methodName)) {
                return ErpFinanceDualWriteLogDO.builder()
                        .id(20L)
                        .sourceVoucherId(801L)
                        .targetVoucherId(802L)
                        .bizType(ErpBizTypeEnum.PRODUCTION_INBOUND.getType())
                        .bizId(900L)
                        .status(ErpFinanceDualWriteStatusEnum.SUCCESS.getStatus())
                        .build();
            }
            return null;
        }));
        setField(service, "voucherService", createProxy(ErpFinanceVoucherService.class, (methodName, args) -> {
            if ("getVoucher".equals(methodName)) {
                Long voucherId = (Long) args[0];
                if (voucherId == 801L) {
                    return ErpFinanceVoucherDO.builder().id(801L)
                            .bizType(ErpBizTypeEnum.PRODUCTION_INBOUND.getType())
                            .bizId(900L)
                            .build();
                }
                if (voucherId == 802L) {
                    return ErpFinanceVoucherDO.builder().id(802L)
                            .bizType(ErpBizTypeEnum.PRODUCTION_INBOUND.getType())
                            .bizId(900L)
                            .build();
                }
            }
            if ("getVoucherEntryListByVoucherId".equals(methodName)) {
                Long voucherId = (Long) args[0];
                if (voucherId == 801L || voucherId == 802L) {
                    return Arrays.asList(
                            ErpFinanceVoucherEntryDO.builder()
                                    .id(voucherId == 801L ? 811L : 821L)
                                    .voucherId(voucherId)
                                    .entryNo(1)
                                    .subjectCode("1405")
                                    .subjectName("库存商品")
                                    .summary("自制入库-电费")
                                    .debitAmount(new BigDecimal("50.00"))
                                    .creditAmount(BigDecimal.ZERO)
                                    .build(),
                            ErpFinanceVoucherEntryDO.builder()
                                    .id(voucherId == 801L ? 812L : 822L)
                                    .voucherId(voucherId)
                                    .entryNo(2)
                                    .subjectCode("5001")
                                    .subjectName("生产成本")
                                    .summary("自制入库-结转")
                                    .debitAmount(BigDecimal.ZERO)
                                    .creditAmount(new BigDecimal("50.00"))
                                    .build()
                    );
                }
            }
            return null;
        }));
        setField(service, "voucherEntryMapper", createProxy(ErpFinanceVoucherEntryMapper.class, (methodName, args) -> {
            if ("updateById".equals(methodName)) {
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
                insertedLogs.clear();
                return null;
            }
            if ("insertBatch".equals(methodName)) {
                insertedLogs.addAll((List<ErpFinanceDualLedgerAmountDiffLogDO>) args[0]);
                return null;
            }
            return null;
        }));
        setField(service, "dualLedgerDiffConfigService", createProxy(ErpFinanceDualLedgerDiffConfigService.class, (methodName, args) -> {
            if ("getDualLedgerDiffConfigList".equals(methodName)) {
                return Arrays.asList(
                        ErpFinanceDualLedgerDiffConfigDO.builder()
                                .id(201L)
                                .bizType(ErpBizTypeEnum.PRODUCTION_INBOUND.getType())
                                .diffItemType(ErpFinanceDualLedgerDiffItemTypeEnum.POWER.getType())
                                .internalSourceType(ErpFinanceDualLedgerDiffSourceTypeEnum.COST_ITEM.getType())
                                .internalSourceValue(ErpFinanceDualLedgerDiffItemTypeEnum.POWER.getType())
                                .calculationType(ErpFinanceDiffCalculationTypeEnum.PRO_RATA.getType())
                                .ratio(new BigDecimal("1.1500"))
                                .build(),
                        ErpFinanceDualLedgerDiffConfigDO.builder()
                                .id(202L)
                                .bizType(ErpBizTypeEnum.PRODUCTION_INBOUND.getType())
                                .diffItemType(ErpFinanceDualLedgerDiffItemTypeEnum.OTHER.getType())
                                .internalSourceType(ErpFinanceDualLedgerDiffSourceTypeEnum.COST_ITEM.getType())
                                .internalSourceValue(ErpFinanceDualLedgerDiffItemTypeEnum.OTHER.getType())
                                .calculationType(ErpFinanceDiffCalculationTypeEnum.PRO_RATA.getType())
                                .ratio(new BigDecimal("1.1500"))
                                .build()
                );
            }
            return null;
        }));
        setField(service, "productionInboundService", createProxy(ErpProductionInboundService.class, (methodName, args) -> {
            if ("getProductionInbound".equals(methodName)) {
                return new ErpProductionInboundDO()
                        .setId(900L)
                        .setProductionOrderId(990901L)
                        .setTotalCost(new BigDecimal("50.00"));
            }
            return null;
        }));
        setField(service, "productionCostService", createProxy(ErpProductionCostService.class, (methodName, args) -> {
            if ("getCostDetail".equals(methodName)) {
                return buildProductionCostDetail(
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        new BigDecimal("20.00"),
                        new BigDecimal("30.00"),
                        new BigDecimal("50.00"));
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

        boolean result = service.recomputeByBizId(ErpBizTypeEnum.PRODUCTION_INBOUND.getType(), 900L);

        assertTrue(result);
        assertNotNull(updatedVoucherRef.get());
        assertEquals(new BigDecimal("57.50"), updatedVoucherRef.get().getTotalDebitAmount());
        assertEquals(new BigDecimal("57.50"), updatedVoucherRef.get().getTotalCreditAmount());
        assertEquals(2, insertedLogs.size());
        BigDecimal diffAmountSum = insertedLogs.stream()
                .map(ErpFinanceDualLedgerAmountDiffLogDO::getDiffAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(new BigDecimal("-7.50"), diffAmountSum);
    }

    @Test
    void recomputeByBizId_shouldNotDoubleCountPowerCostForManufacturingOverhead() throws Exception {
        ErpFinanceDualWriteServiceImpl service = new ErpFinanceDualWriteServiceImpl();
        AtomicReference<ErpFinanceVoucherDO> updatedVoucherRef = new AtomicReference<>();

        setField(service, "dualWriteLogMapper", createProxy(ErpFinanceDualWriteLogMapper.class, (methodName, args) -> {
            if ("selectLatestByBizTypeAndBizId".equals(methodName)) {
                return ErpFinanceDualWriteLogDO.builder()
                        .id(21L)
                        .sourceVoucherId(901L)
                        .targetVoucherId(902L)
                        .bizType(ErpBizTypeEnum.PRODUCTION_INBOUND.getType())
                        .bizId(901L)
                        .status(ErpFinanceDualWriteStatusEnum.SUCCESS.getStatus())
                        .build();
            }
            return null;
        }));
        setField(service, "voucherService", createProxy(ErpFinanceVoucherService.class, (methodName, args) -> {
            if ("getVoucher".equals(methodName)) {
                Long voucherId = (Long) args[0];
                if (voucherId == 901L) {
                    return ErpFinanceVoucherDO.builder().id(901L)
                            .bizType(ErpBizTypeEnum.PRODUCTION_INBOUND.getType())
                            .bizId(901L)
                            .build();
                }
                if (voucherId == 902L) {
                    return ErpFinanceVoucherDO.builder().id(902L)
                            .bizType(ErpBizTypeEnum.PRODUCTION_INBOUND.getType())
                            .bizId(901L)
                            .build();
                }
            }
            if ("getVoucherEntryListByVoucherId".equals(methodName)) {
                Long voucherId = (Long) args[0];
                if (voucherId == 901L || voucherId == 902L) {
                    return Arrays.asList(
                            ErpFinanceVoucherEntryDO.builder()
                                    .id(voucherId == 901L ? 911L : 921L)
                                    .voucherId(voucherId)
                                    .entryNo(1)
                                    .subjectCode("1405")
                                    .subjectName("库存商品")
                                    .summary("自制入库-制造费用")
                                    .debitAmount(new BigDecimal("50.00"))
                                    .creditAmount(BigDecimal.ZERO)
                                    .build(),
                            ErpFinanceVoucherEntryDO.builder()
                                    .id(voucherId == 901L ? 912L : 922L)
                                    .voucherId(voucherId)
                                    .entryNo(2)
                                    .subjectCode("5001")
                                    .subjectName("生产成本")
                                    .summary("自制入库-结转")
                                    .debitAmount(BigDecimal.ZERO)
                                    .creditAmount(new BigDecimal("50.00"))
                                    .build()
                    );
                }
            }
            return null;
        }));
        setField(service, "voucherEntryMapper", createProxy(ErpFinanceVoucherEntryMapper.class, (methodName, args) -> {
            if ("updateById".equals(methodName)) {
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
                return Arrays.asList(
                        ErpFinanceDualLedgerDiffConfigDO.builder()
                                .id(203L)
                                .bizType(ErpBizTypeEnum.PRODUCTION_INBOUND.getType())
                                .diffItemType(ErpFinanceDualLedgerDiffItemTypeEnum.POWER.getType())
                                .internalSourceType(ErpFinanceDualLedgerDiffSourceTypeEnum.COST_ITEM.getType())
                                .internalSourceValue(ErpFinanceDualLedgerDiffItemTypeEnum.POWER.getType())
                                .calculationType(ErpFinanceDiffCalculationTypeEnum.PRO_RATA.getType())
                                .ratio(new BigDecimal("1.1500"))
                                .build(),
                        ErpFinanceDualLedgerDiffConfigDO.builder()
                                .id(204L)
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
                return new ErpProductionInboundDO()
                        .setId(901L)
                        .setProductionOrderId(990902L)
                        .setTotalCost(new BigDecimal("50.00"));
            }
            return null;
        }));
        setField(service, "productionCostService", createProxy(ErpProductionCostService.class, (methodName, args) -> {
            if ("getCostDetail".equals(methodName)) {
                return buildProductionCostDetail(
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        new BigDecimal("20.00"),
                        new BigDecimal("30.00"),
                        new BigDecimal("50.00"));
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

        boolean result = service.recomputeByBizId(ErpBizTypeEnum.PRODUCTION_INBOUND.getType(), 901L);

        assertTrue(result);
        assertNotNull(updatedVoucherRef.get());
        assertEquals(new BigDecimal("57.50"), updatedVoucherRef.get().getTotalDebitAmount());
        assertEquals(new BigDecimal("57.50"), updatedVoucherRef.get().getTotalCreditAmount());
    }

    @Test
    void recomputeByBizId_shouldPreserveProductionInboundEntrySplitWhenRecalculatingTotal() throws Exception {
        ErpFinanceDualWriteServiceImpl service = new ErpFinanceDualWriteServiceImpl();
        java.util.Map<Long, ErpFinanceVoucherEntryDO> updatedEntryMap = new java.util.LinkedHashMap<>();
        AtomicReference<ErpFinanceVoucherDO> updatedVoucherRef = new AtomicReference<>();

        setField(service, "dualWriteLogMapper", createProxy(ErpFinanceDualWriteLogMapper.class, (methodName, args) -> {
            if ("selectLatestByBizTypeAndBizId".equals(methodName)) {
                return ErpFinanceDualWriteLogDO.builder()
                        .id(22L)
                        .sourceVoucherId(1001L)
                        .targetVoucherId(1002L)
                        .bizType(ErpBizTypeEnum.PRODUCTION_INBOUND.getType())
                        .bizId(1001L)
                        .status(ErpFinanceDualWriteStatusEnum.SUCCESS.getStatus())
                        .build();
            }
            return null;
        }));
        setField(service, "voucherService", createProxy(ErpFinanceVoucherService.class, (methodName, args) -> {
            if ("getVoucher".equals(methodName)) {
                Long voucherId = (Long) args[0];
                if (voucherId == 1001L) {
                    return ErpFinanceVoucherDO.builder().id(1001L)
                            .bizType(ErpBizTypeEnum.PRODUCTION_INBOUND.getType())
                            .bizId(1001L)
                            .build();
                }
                if (voucherId == 1002L) {
                    return ErpFinanceVoucherDO.builder().id(1002L)
                            .bizType(ErpBizTypeEnum.PRODUCTION_INBOUND.getType())
                            .bizId(1001L)
                            .build();
                }
            }
            if ("getVoucherEntryListByVoucherId".equals(methodName)) {
                Long voucherId = (Long) args[0];
                if (voucherId == 1001L || voucherId == 1002L) {
                    return Arrays.asList(
                            ErpFinanceVoucherEntryDO.builder()
                                    .id(voucherId == 1001L ? 1011L : 1021L)
                                    .voucherId(voucherId)
                                    .entryNo(1)
                                    .subjectCode("1405")
                                    .subjectName("库存商品")
                                    .summary("自制入库-材料")
                                    .debitAmount(new BigDecimal("60.00"))
                                    .creditAmount(BigDecimal.ZERO)
                                    .build(),
                            ErpFinanceVoucherEntryDO.builder()
                                    .id(voucherId == 1001L ? 1012L : 1022L)
                                    .voucherId(voucherId)
                                    .entryNo(2)
                                    .subjectCode("1406")
                                    .subjectName("库存商品")
                                    .summary("自制入库-辅料")
                                    .debitAmount(new BigDecimal("40.00"))
                                    .creditAmount(BigDecimal.ZERO)
                                    .build(),
                            ErpFinanceVoucherEntryDO.builder()
                                    .id(voucherId == 1001L ? 1013L : 1023L)
                                    .voucherId(voucherId)
                                    .entryNo(3)
                                    .subjectCode("5001")
                                    .subjectName("生产成本")
                                    .summary("自制入库-结转")
                                    .debitAmount(BigDecimal.ZERO)
                                    .creditAmount(new BigDecimal("100.00"))
                                    .build()
                    );
                }
            }
            return null;
        }));
        setField(service, "voucherEntryMapper", createProxy(ErpFinanceVoucherEntryMapper.class, (methodName, args) -> {
            if ("updateById".equals(methodName)) {
                ErpFinanceVoucherEntryDO entry = (ErpFinanceVoucherEntryDO) args[0];
                updatedEntryMap.put(entry.getId(), entry);
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
                return Arrays.asList(
                        ErpFinanceDualLedgerDiffConfigDO.builder()
                                .id(204L)
                                .bizType(ErpBizTypeEnum.PRODUCTION_INBOUND.getType())
                                .diffItemType(ErpFinanceDualLedgerDiffItemTypeEnum.POWER.getType())
                                .internalSourceType(ErpFinanceDualLedgerDiffSourceTypeEnum.COST_ITEM.getType())
                                .internalSourceValue(ErpFinanceDualLedgerDiffItemTypeEnum.POWER.getType())
                                .calculationType(ErpFinanceDiffCalculationTypeEnum.PRO_RATA.getType())
                                .ratio(new BigDecimal("1.2000"))
                                .build()
                );
            }
            return null;
        }));
        setField(service, "productionInboundService", createProxy(ErpProductionInboundService.class, (methodName, args) -> {
            if ("getProductionInbound".equals(methodName)) {
                return new ErpProductionInboundDO()
                        .setId(1001L)
                        .setProductionOrderId(990903L)
                        .setTotalCost(new BigDecimal("100.00"));
            }
            return null;
        }));
        setField(service, "productionCostService", createProxy(ErpProductionCostService.class, (methodName, args) -> {
            if ("getCostDetail".equals(methodName)) {
                return buildProductionCostDetail(
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        new BigDecimal("100.00"),
                        BigDecimal.ZERO,
                        new BigDecimal("100.00"));
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

        boolean result = service.recomputeByBizId(ErpBizTypeEnum.PRODUCTION_INBOUND.getType(), 1001L);

        assertTrue(result);
        assertNotNull(updatedVoucherRef.get());
        assertEquals(new BigDecimal("120.00"), updatedVoucherRef.get().getTotalDebitAmount());
        assertEquals(new BigDecimal("120.00"), updatedVoucherRef.get().getTotalCreditAmount());
        assertEquals(new BigDecimal("72.00"), updatedEntryMap.get(1021L).getDebitAmount());
        assertEquals(new BigDecimal("48.00"), updatedEntryMap.get(1022L).getDebitAmount());
        assertEquals(new BigDecimal("120.00"), updatedEntryMap.get(1023L).getCreditAmount());
    }

    @Test
    void recomputeByBizId_shouldUseLatestSuccessLogWhenMultipleLogsExist() throws Exception {
        ErpFinanceDualWriteServiceImpl service = new ErpFinanceDualWriteServiceImpl();
        AtomicReference<ErpFinanceVoucherEntryDO> updatedDebitEntryRef = new AtomicReference<>();

        setField(service, "dualWriteLogMapper", createProxy(ErpFinanceDualWriteLogMapper.class, (methodName, args) -> {
            if ("selectLatestByBizTypeAndBizId".equals(methodName)) {
                return ErpFinanceDualWriteLogDO.builder()
                        .id(99L)
                        .sourceVoucherId(901L)
                        .targetVoucherId(902L)
                        .bizType(ErpBizTypeEnum.PRODUCTION_INBOUND.getType())
                        .bizId(1L)
                        .status(ErpFinanceDualWriteStatusEnum.SUCCESS.getStatus())
                        .build();
            }
            return null;
        }));
        setField(service, "voucherService", createProxy(ErpFinanceVoucherService.class, (methodName, args) -> {
            if ("getVoucher".equals(methodName)) {
                Long voucherId = (Long) args[0];
                return ErpFinanceVoucherDO.builder().id(voucherId)
                        .bizType(ErpBizTypeEnum.PRODUCTION_INBOUND.getType())
                        .bizId(1L)
                        .build();
            }
            if ("getVoucherEntryListByVoucherId".equals(methodName)) {
                return Arrays.asList(
                        new ErpFinanceVoucherEntryDO().setId(31L).setVoucherId((Long) args[0]).setEntryNo(1)
                                .setSummary("自制入库").setSubjectCode("1405").setSubjectName("库存商品")
                                .setDebitAmount(new BigDecimal("900.00")).setCreditAmount(BigDecimal.ZERO),
                        new ErpFinanceVoucherEntryDO().setId(32L).setVoucherId((Long) args[0]).setEntryNo(2)
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
                return 1;
            }
            return null;
        }));
        setField(service, "voucherMapper", createProxy(ErpFinanceVoucherMapper.class, (methodName, args) -> 1));
        setField(service, "dualLedgerAmountDiffLogMapper", createProxy(ErpFinanceDualLedgerAmountDiffLogMapper.class, (methodName, args) -> null));
        setField(service, "dualLedgerDiffConfigService", createProxy(ErpFinanceDualLedgerDiffConfigService.class, (methodName, args) -> {
            if ("getDualLedgerDiffConfigList".equals(methodName)) {
                return List.of(
                        ErpFinanceDualLedgerDiffConfigDO.builder()
                                .bizType(ErpBizTypeEnum.PRODUCTION_INBOUND.getType())
                                .diffItemType(ErpFinanceDualLedgerDiffItemTypeEnum.LABOR.getType())
                                .internalSourceType(ErpFinanceDualLedgerDiffSourceTypeEnum.COST_ITEM.getType())
                                .internalSourceValue(ErpFinanceDualLedgerDiffItemTypeEnum.LABOR.getType())
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

        boolean result = service.recomputeByBizId(ErpBizTypeEnum.PRODUCTION_INBOUND.getType(), 1L);

        assertTrue(result);
        assertNotNull(updatedDebitEntryRef.get());
        assertEquals(new BigDecimal("930.00"), updatedDebitEntryRef.get().getDebitAmount());
    }

    @Test
    void recomputeByBizId_shouldRecalculateAmount() throws Exception {
        ErpFinanceDualWriteServiceImpl service = new ErpFinanceDualWriteServiceImpl();
        AtomicReference<ErpFinanceVoucherEntryDO> updatedEntryRef = new AtomicReference<>();
        AtomicReference<ErpFinanceVoucherDO> updatedVoucherRef = new AtomicReference<>();
        List<ErpFinanceDualLedgerAmountDiffLogDO> insertedLogs = new ArrayList<>();
        AtomicInteger updateCount = new AtomicInteger(0);

        // Mock dualWriteLogMapper
        setField(service, "dualWriteLogMapper", createProxy(ErpFinanceDualWriteLogMapper.class, (methodName, args) -> {
            if ("selectLatestByBizTypeAndBizId".equals(methodName)) {
                return ErpFinanceDualWriteLogDO.builder()
                        .id(1L)
                        .sourceVoucherId(100L)
                        .targetVoucherId(200L)
                        .bizType(11)
                        .bizId(50L)
                        .status(ErpFinanceDualWriteStatusEnum.SUCCESS.getStatus())
                        .build();
            }
            return null;
        }));

        // Mock voucherService
        setField(service, "voucherService", createProxy(ErpFinanceVoucherService.class, (methodName, args) -> {
            if ("getVoucher".equals(methodName)) {
                return ErpFinanceVoucherDO.builder()
                        .id(100L)
                        .bizType(11)
                        .bizId(50L)
                        .build();
            }
            if ("getVoucherEntryListByVoucherId".equals(methodName)) {
                Long voucherId = (Long) args[0];
                if (voucherId == 100L) {
                    // Source entries
                    return Arrays.asList(
                            ErpFinanceVoucherEntryDO.builder()
                                    .id(1L).voucherId(100L).entryNo(1)
                                    .subjectCode("5001").debitAmount(new BigDecimal("100.00")).build()
                    );
                }
                if (voucherId == 200L) {
                    // Target entries
                    return Arrays.asList(
                            ErpFinanceVoucherEntryDO.builder()
                                    .id(2L).voucherId(200L).entryNo(1)
                                    .subjectCode("5001").debitAmount(new BigDecimal("100.00")).build()
                    );
                }
            }
            return null;
        }));

        // Mock voucherEntryMapper
        setField(service, "voucherEntryMapper", createProxy(ErpFinanceVoucherEntryMapper.class, (methodName, args) -> {
            if ("updateById".equals(methodName)) {
                updatedEntryRef.set((ErpFinanceVoucherEntryDO) args[0]);
                updateCount.incrementAndGet();
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
                insertedLogs.clear();
                return null;
            }
            if ("insertBatch".equals(methodName)) {
                insertedLogs.addAll((List<ErpFinanceDualLedgerAmountDiffLogDO>) args[0]);
                return null;
            }
            return null;
        }));

        // Mock dualLedgerDiffConfigService
        setField(service, "dualLedgerDiffConfigService", createProxy(ErpFinanceDualLedgerDiffConfigService.class, (methodName, args) -> {
            if ("getDualLedgerDiffConfigList".equals(methodName)) {
                return Arrays.asList(
                        ErpFinanceDualLedgerDiffConfigDO.builder()
                                .id(1L)
                                .bizType(11)
                                .diffItemType(20)
                                .internalSourceValue(5001)
                                .calculationType(ErpFinanceDiffCalculationTypeEnum.PRO_RATA.getType())
                                .ratio(new BigDecimal("1.15"))
                                .build()
                );
            }
            return null;
        }));

        // Mock amountDiffCalculatorFactory
        AmountDiffCalculatorFactory factory = new AmountDiffCalculatorFactory();
        factory.init(Arrays.asList(
                new ProRataAmountDiffCalculator(),
                new FixedVarianceAmountDiffCalculator(),
                new SourceMappingAmountDiffCalculator()
        ));
        setField(service, "amountDiffCalculatorFactory", factory);

        // Execute recompute
        boolean result = service.recomputeByBizId(11, 50L);

        // Verify
        assertTrue(result);
        assertEquals(1, updateCount.get());
        assertNotNull(updatedEntryRef.get());
        // 100 * 1.15 = 115
        assertEquals(new BigDecimal("115.00"), updatedEntryRef.get().getDebitAmount());
        assertNotNull(updatedVoucherRef.get());
        assertEquals(new BigDecimal("115.00"), updatedVoucherRef.get().getTotalDebitAmount());
        assertEquals(BigDecimal.ZERO.setScale(2), updatedVoucherRef.get().getTotalCreditAmount());
        assertEquals(1, insertedLogs.size());
        assertEquals(new BigDecimal("-15.00"), insertedLogs.get(0).getDiffAmount());
    }

    @Test
    void recomputeByBizId_shouldMatchCostItemSourceSemantics() throws Exception {
        ErpFinanceDualWriteServiceImpl service = new ErpFinanceDualWriteServiceImpl();
        AtomicReference<ErpFinanceVoucherEntryDO> updatedEntryRef = new AtomicReference<>();

        setField(service, "dualWriteLogMapper", createProxy(ErpFinanceDualWriteLogMapper.class, (methodName, args) -> {
            if ("selectLatestByBizTypeAndBizId".equals(methodName)) {
                return ErpFinanceDualWriteLogDO.builder()
                        .id(2L)
                        .sourceVoucherId(300L)
                        .targetVoucherId(400L)
                        .bizType(11)
                        .bizId(60L)
                        .status(ErpFinanceDualWriteStatusEnum.SUCCESS.getStatus())
                        .build();
            }
            return null;
        }));

        setField(service, "voucherService", createProxy(ErpFinanceVoucherService.class, (methodName, args) -> {
            if ("getVoucher".equals(methodName)) {
                return ErpFinanceVoucherDO.builder()
                        .id(300L)
                        .bizType(11)
                        .bizId(60L)
                        .build();
            }
            if ("getVoucherEntryListByVoucherId".equals(methodName)) {
                Long voucherId = (Long) args[0];
                if (voucherId == 300L) {
                    return Arrays.asList(
                            ErpFinanceVoucherEntryDO.builder()
                                    .id(11L).voucherId(300L).entryNo(1)
                                    .subjectCode("660201").subjectName("制造费用-直接人工")
                                    .summary("生产成本直接人工归集")
                                    .debitAmount(new BigDecimal("200.00")).build()
                    );
                }
                if (voucherId == 400L) {
                    return Arrays.asList(
                            ErpFinanceVoucherEntryDO.builder()
                                    .id(12L).voucherId(400L).entryNo(1)
                                    .subjectCode("660201").subjectName("制造费用-直接人工")
                                    .summary("生产成本直接人工归集")
                                    .debitAmount(new BigDecimal("200.00")).build()
                    );
                }
            }
            return null;
        }));

        setField(service, "voucherEntryMapper", createProxy(ErpFinanceVoucherEntryMapper.class, (methodName, args) -> {
            if ("updateById".equals(methodName)) {
                updatedEntryRef.set((ErpFinanceVoucherEntryDO) args[0]);
                return 1;
            }
            return null;
        }));

        setField(service, "voucherMapper", createProxy(ErpFinanceVoucherMapper.class, (methodName, args) -> 1));
        setField(service, "dualLedgerAmountDiffLogMapper", createProxy(ErpFinanceDualLedgerAmountDiffLogMapper.class, (methodName, args) -> null));

        setField(service, "dualLedgerDiffConfigService", createProxy(ErpFinanceDualLedgerDiffConfigService.class, (methodName, args) -> {
            if ("getDualLedgerDiffConfigList".equals(methodName)) {
                return Arrays.asList(
                        ErpFinanceDualLedgerDiffConfigDO.builder()
                                .id(2L)
                                .bizType(11)
                                .diffItemType(ErpFinanceDualLedgerDiffItemTypeEnum.LABOR.getType())
                                .internalSourceType(ErpFinanceDualLedgerDiffSourceTypeEnum.COST_ITEM.getType())
                                .internalSourceValue(ErpFinanceDualLedgerDiffItemTypeEnum.LABOR.getType())
                                .calculationType(ErpFinanceDiffCalculationTypeEnum.PRO_RATA.getType())
                                .ratio(new BigDecimal("1.10"))
                                .build()
                );
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

        boolean result = service.recomputeByBizId(11, 60L);

        assertTrue(result);
        assertNotNull(updatedEntryRef.get());
        assertEquals(new BigDecimal("220.00"), updatedEntryRef.get().getDebitAmount());
    }

    @Test
    void recomputeByBizId_shouldRelinkLatestVoucherIdsWhenLogPointersAreStale() throws Exception {
        ErpFinanceDualWriteServiceImpl service = new ErpFinanceDualWriteServiceImpl();
        AtomicReference<ErpFinanceDualWriteLogDO> updatedLogRef = new AtomicReference<>();
        AtomicReference<ErpFinanceVoucherEntryDO> updatedEntryRef = new AtomicReference<>();

        setField(service, "dualWriteLogMapper", createProxy(ErpFinanceDualWriteLogMapper.class, (methodName, args) -> {
            if ("selectLatestByBizTypeAndBizId".equals(methodName)) {
                return ErpFinanceDualWriteLogDO.builder()
                        .id(3L)
                        .sourceVoucherId(700L)
                        .targetVoucherId(800L)
                        .sourceLedgerId(99603L)
                        .targetLedgerId(99604L)
                        .bizType(40)
                        .bizId(107006L)
                        .status(ErpFinanceDualWriteStatusEnum.SUCCESS.getStatus())
                        .build();
            }
            if ("updateById".equals(methodName)) {
                updatedLogRef.set((ErpFinanceDualWriteLogDO) args[0]);
                return 1;
            }
            return null;
        }));

        setField(service, "voucherService", createProxy(ErpFinanceVoucherService.class, (methodName, args) -> {
            if ("getVoucher".equals(methodName)) {
                return null;
            }
            if ("getVoucherByLedgerAndBiz".equals(methodName)) {
                Long ledgerId = (Long) args[0];
                if (ledgerId == 99603L) {
                    return ErpFinanceVoucherDO.builder().id(124003L).ledgerId(99603L).bizType(40).bizId(107006L).build();
                }
                if (ledgerId == 99604L) {
                    return ErpFinanceVoucherDO.builder().id(124004L).ledgerId(99604L).bizType(40).bizId(107006L).build();
                }
            }
            if ("getVoucherEntryListByVoucherId".equals(methodName)) {
                Long voucherId = (Long) args[0];
                if (voucherId == 124003L) {
                    return Arrays.asList(
                            ErpFinanceVoucherEntryDO.builder()
                                    .id(21L).voucherId(124003L).entryNo(1)
                                    .subjectCode("6601").subjectName("销售费用")
                                    .summary("费用报销")
                                    .debitAmount(new BigDecimal("760.00")).build()
                    );
                }
                if (voucherId == 124004L) {
                    return Arrays.asList(
                            ErpFinanceVoucherEntryDO.builder()
                                    .id(22L).voucherId(124004L).entryNo(1)
                                    .subjectCode("6601").subjectName("销售费用")
                                    .summary("费用报销")
                                    .debitAmount(new BigDecimal("760.00")).build()
                    );
                }
            }
            return null;
        }));

        setField(service, "voucherEntryMapper", createProxy(ErpFinanceVoucherEntryMapper.class, (methodName, args) -> {
            if ("updateById".equals(methodName)) {
                updatedEntryRef.set((ErpFinanceVoucherEntryDO) args[0]);
                return 1;
            }
            return null;
        }));
        setField(service, "voucherMapper", createProxy(ErpFinanceVoucherMapper.class, (methodName, args) -> 1));
        setField(service, "dualLedgerAmountDiffLogMapper", createProxy(ErpFinanceDualLedgerAmountDiffLogMapper.class, (methodName, args) -> null));
        setField(service, "dualLedgerDiffConfigService", createProxy(ErpFinanceDualLedgerDiffConfigService.class, (methodName, args) -> {
            if ("getDualLedgerDiffConfigList".equals(methodName)) {
                return Arrays.asList(
                        ErpFinanceDualLedgerDiffConfigDO.builder()
                                .id(3L)
                                .bizType(40)
                                .diffItemType(ErpFinanceDualLedgerDiffItemTypeEnum.OTHER.getType())
                                .internalSourceType(ErpFinanceDualLedgerDiffSourceTypeEnum.COST_ITEM.getType())
                                .internalSourceValue(6601)
                                .calculationType(ErpFinanceDiffCalculationTypeEnum.FIXED_VARIANCE.getType())
                                .fixedAmount(new BigDecimal("-40.00"))
                                .build()
                );
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

        boolean result = service.recomputeByBizId(40, 107006L);

        assertTrue(result);
        assertNotNull(updatedLogRef.get());
        assertEquals(124003L, updatedLogRef.get().getSourceVoucherId());
        assertEquals(124004L, updatedLogRef.get().getTargetVoucherId());
        assertNotNull(updatedEntryRef.get());
        assertEquals(new BigDecimal("800.00"), updatedEntryRef.get().getDebitAmount());
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static ErpProductionCostDetailRespVO buildProductionCostDetail(BigDecimal laborCost,
                                                                           BigDecimal depreciationCost,
                                                                           BigDecimal powerCost,
                                                                           BigDecimal otherCost,
                                                                           BigDecimal totalCost) {
        ErpProductionCostDetailRespVO detail = new ErpProductionCostDetailRespVO();
        detail.setLaborCost(laborCost);
        detail.setDepreciationCost(depreciationCost);
        detail.setPowerCost(powerCost);
        detail.setOtherCost(otherCost);
        detail.setTotalCost(totalCost);
        return detail;
    }

    @SuppressWarnings("unchecked")
    private static <T> T createProxy(Class<T> interfaceType, java.util.function.BiFunction<String, Object[], Object> handler) {
        return (T) Proxy.newProxyInstance(
                interfaceType.getClassLoader(),
                new Class<?>[]{interfaceType},
                (proxy, method, args) -> {
                    if (method.getDeclaringClass() == Object.class) {
                        if ("toString".equals(method.getName())) {
                            return interfaceType.getSimpleName() + "Proxy";
                        }
                        if ("hashCode".equals(method.getName())) {
                            return System.identityHashCode(proxy);
                        }
                        if ("equals".equals(method.getName())) {
                            return proxy == args[0];
                        }
                    }
                    return handler.apply(method.getName(), args);
                }
        );
    }
}
