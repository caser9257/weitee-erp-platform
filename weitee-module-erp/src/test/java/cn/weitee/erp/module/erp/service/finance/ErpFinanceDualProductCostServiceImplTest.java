package cn.weitee.erp.module.erp.service.finance;

import cn.weitee.erp.module.erp.controller.admin.finance.vo.productdualcost.ErpFinanceDualProductCostRebuildReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceDualProductCostResultDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceDualProductCostItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceDualProductCostRebuildLogDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceDualLedgerDiffConfigDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceVoucherDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceVoucherEntryDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionInboundDO;
import cn.weitee.erp.module.erp.enums.ErpFinanceDiffCalculationTypeEnum;
import cn.weitee.erp.module.erp.enums.ErpFinanceVoucherStatusEnum;
import cn.weitee.erp.module.erp.enums.common.ErpBizTypeEnum;
import cn.weitee.erp.module.erp.service.finance.diffcalc.AmountDiffCalculator;
import cn.weitee.erp.module.erp.service.finance.diffcalc.AmountDiffCalculatorFactory;
import cn.weitee.erp.module.erp.service.finance.diffcalc.ProRataAmountDiffCalculator;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 产品双账成本 Service 单元测试
 */
class ErpFinanceDualProductCostServiceImplTest {

    // ========== rebuildProductDualCost 测试 ==========

    @Test
    void rebuildProductDualCost_shouldClassifyBySubjectCode() throws Exception {
        ErpFinanceDualProductCostServiceImpl service = new ErpFinanceDualProductCostServiceImpl();

        List<ErpFinanceDualProductCostResultDO> insertedResults = new ArrayList<>();
        List<ErpFinanceDualProductCostItemDO> insertedItems = new ArrayList<>();
        AtomicLong idGen = new AtomicLong(1);

        // Mock resultMapper
        setField(service, "productCostResultMapper", createProxy(
                cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceDualProductCostResultMapper.class,
                (methodName, args) -> {
                    if ("delete".equals(methodName)) return 0;
                    if ("insert".equals(methodName)) {
                        ErpFinanceDualProductCostResultDO r = (ErpFinanceDualProductCostResultDO) args[0];
                        r.setId(idGen.getAndIncrement());
                        insertedResults.add(r);
                        return 1;
                    }
                    return null;
                }));

        // Mock itemMapper
        setField(service, "productCostItemMapper", createProxy(
                cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceDualProductCostItemMapper.class,
                (methodName, args) -> {
                    if ("delete".equals(methodName)) return 0;
                    if ("insert".equals(methodName)) {
                        ErpFinanceDualProductCostItemDO item = (ErpFinanceDualProductCostItemDO) args[0];
                        item.setId(idGen.getAndIncrement());
                        insertedItems.add(item);
                        return 1;
                    }
                    return null;
                }));

        // Mock rebuildLogMapper
        setField(service, "rebuildLogMapper", createProxy(
                cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceDualProductCostRebuildLogMapper.class,
                (methodName, args) -> {
                    if ("insert".equals(methodName)) {
                        ErpFinanceDualProductCostRebuildLogDO log = (ErpFinanceDualProductCostRebuildLogDO) args[0];
                        log.setId(idGen.getAndIncrement());
                        return 1;
                    }
                    if ("updateById".equals(methodName)) return 1;
                    return null;
                }));

        // Mock voucherMapper：返回一条自制入库凭证
        setField(service, "voucherMapper", createProxy(
                cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceVoucherMapper.class,
                (methodName, args) -> {
                    if ("selectList".equals(methodName)) {
                        return List.of(new ErpFinanceVoucherDO()
                                .setId(201L).setLedgerId(1L)
                                .setBizType(ErpBizTypeEnum.PRODUCTION_INBOUND.getType())
                                .setBizId(88L).setBizNo("ZZRK-001")
                                .setVoucherTime(LocalDateTime.of(2026, 5, 20, 10, 0))
                                .setStatus(ErpFinanceVoucherStatusEnum.GENERATED.getStatus())
                                .setTotalDebitAmount(new BigDecimal("10000.00"))
                                .setTotalCreditAmount(new BigDecimal("10000.00")));
                    }
                    return null;
                }));

        // Mock voucherEntryMapper：返回三条分录（材料/人工/制造费用）
        setField(service, "voucherEntryMapper", createProxy(
                cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceVoucherEntryMapper.class,
                (methodName, args) -> {
                    if ("selectList".equals(methodName)) {
                        return List.of(
                                // 材料：科目 1403
                                new ErpFinanceVoucherEntryDO().setId(301L).setVoucherId(201L)
                                        .setEntryNo(1).setSubjectCode("1403").setSubjectName("原材料")
                                        .setDebitAmount(new BigDecimal("5000.00")).setCreditAmount(BigDecimal.ZERO),
                                // 人工：科目 2211
                                new ErpFinanceVoucherEntryDO().setId(302L).setVoucherId(201L)
                                        .setEntryNo(2).setSubjectCode("2211").setSubjectName("应付职工薪酬")
                                        .setDebitAmount(new BigDecimal("3000.00")).setCreditAmount(BigDecimal.ZERO),
                                // 制造费用：科目 5101
                                new ErpFinanceVoucherEntryDO().setId(303L).setVoucherId(201L)
                                        .setEntryNo(3).setSubjectCode("5101").setSubjectName("制造费用")
                                        .setDebitAmount(new BigDecimal("2000.00")).setCreditAmount(BigDecimal.ZERO));
                    }
                    return null;
                }));
        mockProductionInboundSource(service, 88L, 1L, null);

        // Mock diffConfigService：无差异配置
        setField(service, "dualLedgerDiffConfigService", createProxy(
                ErpFinanceDualLedgerDiffConfigService.class,
                (methodName, args) -> List.of()));

        // Mock factory
        AmountDiffCalculatorFactory factory = new AmountDiffCalculatorFactory();
        Field calcMapField = AmountDiffCalculatorFactory.class.getDeclaredField("calculatorMap");
        calcMapField.setAccessible(true);
        java.util.Map<Integer, AmountDiffCalculator> calcMap = (java.util.Map<Integer, AmountDiffCalculator>) calcMapField.get(factory);
        ProRataAmountDiffCalculator proRata = new ProRataAmountDiffCalculator();
        calcMap.put(proRata.getCalculationType(), proRata);
        setField(service, "amountDiffCalculatorFactory", factory);

        // 执行 rebuild
        ErpFinanceDualProductCostRebuildReqVO reqVO = new ErpFinanceDualProductCostRebuildReqVO();
        reqVO.setProductId(1L);
        reqVO.setPeriod("2026-05");
        service.rebuildProductDualCost(1L, reqVO);

        // 验证结果
        assertEquals(1, insertedResults.size());
        ErpFinanceDualProductCostResultDO result = insertedResults.get(0);

        // 材料 5000
        assertEquals(new BigDecimal("5000.00"), result.getExternalMaterialAmount());
        assertEquals(new BigDecimal("5000.00"), result.getInternalMaterialAmount());
        // 人工 3000
        assertEquals(new BigDecimal("3000.00"), result.getExternalLaborAmount());
        assertEquals(new BigDecimal("3000.00"), result.getInternalLaborAmount());
        // 制造费用 2000
        assertEquals(new BigDecimal("2000.00"), result.getExternalOverheadAmount());
        assertEquals(new BigDecimal("2000.00"), result.getInternalOverheadAmount());
        // 总计 10000
        assertEquals(new BigDecimal("10000.00"), result.getExternalTotalAmount());
        assertEquals(new BigDecimal("10000.00"), result.getInternalTotalAmount());
        // 差异为 0
        assertEquals(0, result.getDiffAmount().compareTo(BigDecimal.ZERO));

        // 验证明细：3 条分录 → 3 条明细
        assertEquals(3, insertedItems.size());
        // 验证明细的 costComponentType 分类
        assertTrue(insertedItems.stream().anyMatch(i -> i.getCostComponentType() == 10), "应有材料类明细");
        assertTrue(insertedItems.stream().anyMatch(i -> i.getCostComponentType() == 20), "应有人工类明细");
        assertTrue(insertedItems.stream().anyMatch(i -> i.getCostComponentType() == 30), "应有制造费用类明细");
    }

    @Test
    void rebuildProductDualCost_withDiffConfig_shouldApplyRatio() throws Exception {
        ErpFinanceDualProductCostServiceImpl service = new ErpFinanceDualProductCostServiceImpl();

        List<ErpFinanceDualProductCostResultDO> insertedResults = new ArrayList<>();
        List<ErpFinanceDualProductCostItemDO> insertedItems = new ArrayList<>();
        AtomicLong idGen = new AtomicLong(1);

        setField(service, "productCostResultMapper", createProxy(
                cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceDualProductCostResultMapper.class,
                (methodName, args) -> {
                    if ("delete".equals(methodName)) return 0;
                    if ("insert".equals(methodName)) {
                        ErpFinanceDualProductCostResultDO r = (ErpFinanceDualProductCostResultDO) args[0];
                        r.setId(idGen.getAndIncrement());
                        insertedResults.add(r);
                        return 1;
                    }
                    return null;
                }));
        setField(service, "productCostItemMapper", createProxy(
                cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceDualProductCostItemMapper.class,
                (methodName, args) -> {
                    if ("delete".equals(methodName)) return 0;
                    if ("insert".equals(methodName)) {
                        ErpFinanceDualProductCostItemDO item = (ErpFinanceDualProductCostItemDO) args[0];
                        item.setId(idGen.getAndIncrement());
                        insertedItems.add(item);
                        return 1;
                    }
                    return null;
                }));
        setField(service, "rebuildLogMapper", createProxy(
                cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceDualProductCostRebuildLogMapper.class,
                (methodName, args) -> {
                    if ("insert".equals(methodName)) {
                        ErpFinanceDualProductCostRebuildLogDO log = (ErpFinanceDualProductCostRebuildLogDO) args[0];
                        log.setId(idGen.getAndIncrement());
                        return 1;
                    }
                    if ("updateById".equals(methodName)) return 1;
                    return null;
                }));
        // 只有一条材料分录
        setField(service, "voucherMapper", createProxy(
                cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceVoucherMapper.class,
                (methodName, args) -> {
                    if ("selectList".equals(methodName)) {
                        return List.of(new ErpFinanceVoucherDO()
                                .setId(201L).setLedgerId(1L)
                                .setBizType(ErpBizTypeEnum.PRODUCTION_INBOUND.getType())
                                .setBizId(88L).setBizNo("ZZRK-001")
                                .setVoucherTime(LocalDateTime.of(2026, 5, 20, 10, 0))
                                .setStatus(ErpFinanceVoucherStatusEnum.GENERATED.getStatus())
                                .setTotalDebitAmount(new BigDecimal("10000.00"))
                                .setTotalCreditAmount(new BigDecimal("10000.00")));
                    }
                    return null;
                }));
        setField(service, "voucherEntryMapper", createProxy(
                cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceVoucherEntryMapper.class,
                (methodName, args) -> {
                    if ("selectList".equals(methodName)) {
                        return List.of(new ErpFinanceVoucherEntryDO().setId(301L).setVoucherId(201L)
                                .setEntryNo(1).setSubjectCode("1403").setSubjectName("原材料")
                                .setDebitAmount(new BigDecimal("10000.00")).setCreditAmount(BigDecimal.ZERO));
                    }
                    return null;
                }));
        mockProductionInboundSource(service, 88L, 1L, null);

        // Mock diffConfigService：材料 costComponentType=10 配置 ratio=1.20
        setField(service, "dualLedgerDiffConfigService", createProxy(
                ErpFinanceDualLedgerDiffConfigService.class,
                (methodName, args) -> {
                    if ("getDualLedgerDiffConfigList".equals(methodName)) {
                        return List.of(new ErpFinanceDualLedgerDiffConfigDO()
                                .setBizType(70).setDiffItemType(10)
                                .setCalculationType(ErpFinanceDiffCalculationTypeEnum.PRO_RATA.getType())
                                .setRatio(new BigDecimal("1.20")));
                    }
                    return List.of();
                }));

        AmountDiffCalculatorFactory factory = new AmountDiffCalculatorFactory();
        Field calcMapField = AmountDiffCalculatorFactory.class.getDeclaredField("calculatorMap");
        calcMapField.setAccessible(true);
        java.util.Map<Integer, AmountDiffCalculator> calcMap = (java.util.Map<Integer, AmountDiffCalculator>) calcMapField.get(factory);
        ProRataAmountDiffCalculator proRata = new ProRataAmountDiffCalculator();
        calcMap.put(proRata.getCalculationType(), proRata);
        setField(service, "amountDiffCalculatorFactory", factory);

        // 执行
        ErpFinanceDualProductCostRebuildReqVO reqVO = new ErpFinanceDualProductCostRebuildReqVO();
        reqVO.setProductId(1L);
        reqVO.setPeriod("2026-05");
        service.rebuildProductDualCost(1L, reqVO);

        // 验证：内部材料 10000，外部材料 = 10000 × 1.20 = 12000
        assertEquals(1, insertedResults.size());
        ErpFinanceDualProductCostResultDO result = insertedResults.get(0);
        assertEquals(new BigDecimal("10000.00"), result.getInternalMaterialAmount());
        assertEquals(new BigDecimal("12000.00"), result.getExternalMaterialAmount(), "外部材料 = 内部材料 × 1.20");
        assertEquals(new BigDecimal("10000.00"), result.getInternalTotalAmount());
        assertEquals(new BigDecimal("12000.00"), result.getExternalTotalAmount());
        assertEquals(new BigDecimal("-2000.00"), result.getDiffAmount(), "差异 = 10000 - 12000");

        // 验证明细
        assertEquals(1, insertedItems.size());
        assertEquals(new BigDecimal("-2000.00"), insertedItems.get(0).getDiffAmount());
    }

    // ========== resolveCostComponentType 测试 ==========

    @Test
    void resolveCostComponentType_shouldClassifyCorrectly() throws Exception {
        ErpFinanceDualProductCostServiceImpl service = new ErpFinanceDualProductCostServiceImpl();

        java.lang.reflect.Method method = ErpFinanceDualProductCostServiceImpl.class
                .getDeclaredMethod("resolveCostComponentType", String.class);
        method.setAccessible(true);

        // 材料类
        assertEquals(10, method.invoke(service, "1403"), "1403原材料 → 材料");
        assertEquals(10, method.invoke(service, "140301"), "140301 → 材料");
        assertEquals(10, method.invoke(service, "1405"), "1405库存商品 → 材料");

        // 人工类
        assertEquals(20, method.invoke(service, "2211"), "2211薪酬 → 人工");
        assertEquals(20, method.invoke(service, "221101"), "221101 → 人工");

        // 制造费用类
        assertEquals(30, method.invoke(service, "5101"), "5101制造费用 → 制造费用");
        assertEquals(30, method.invoke(service, "510101"), "510101 → 制造费用");

        // 未知科目默认归入制造费用
        assertEquals(30, method.invoke(service, "6601"), "未知科目 → 制造费用");
        assertEquals(30, method.invoke(service, (String) null), "null → 制造费用");
        assertEquals(30, method.invoke(service, ""), "空字符串 → 制造费用");
    }

    @Test
    void rebuildProductDualCost_shouldExcludeOtherProductInboundVouchers() throws Exception {
        ErpFinanceDualProductCostServiceImpl service = new ErpFinanceDualProductCostServiceImpl();
        java.lang.reflect.Method method = ErpFinanceDualProductCostServiceImpl.class.getDeclaredMethod(
                "filterProductSourceVouchers", List.class, ErpFinanceDualProductCostRebuildReqVO.class);
        method.setAccessible(true);
        mockProductionInboundSource(service, 88L, 1L, 900L, 89L, 2L, 901L);

        ErpFinanceDualProductCostRebuildReqVO reqVO = new ErpFinanceDualProductCostRebuildReqVO();
        reqVO.setProductId(1L);
        reqVO.setProductionOrderId(900L);
        List<ErpFinanceVoucherDO> result = (List<ErpFinanceVoucherDO>) method.invoke(service, List.of(
                new ErpFinanceVoucherDO().setId(201L).setBizType(ErpBizTypeEnum.PRODUCTION_INBOUND.getType()).setBizId(88L),
                new ErpFinanceVoucherDO().setId(202L).setBizType(ErpBizTypeEnum.PRODUCTION_INBOUND.getType()).setBizId(89L)), reqVO);

        assertEquals(List.of(201L), result.stream().map(ErpFinanceVoucherDO::getId).toList());
    }

    // ========== 辅助方法 ==========

    @SuppressWarnings("unchecked")
    private <T> T createProxy(Class<T> type, MethodHandler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type},
                (proxy, method, args) -> {
                    if (method.getDeclaringClass() == Object.class) {
                        if ("toString".equals(method.getName())) return type.getSimpleName() + "Proxy";
                        if ("hashCode".equals(method.getName())) return System.identityHashCode(proxy);
                        if ("equals".equals(method.getName())) return proxy == args[0];
                    }
                    return handler.handle(method.getName(), args);
                });
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private void mockProductionInboundSource(ErpFinanceDualProductCostServiceImpl service, Long inboundId,
                                             Long productId, Long productionOrderId) throws Exception {
        mockProductionInboundSource(service, inboundId, productId, productionOrderId, null, null, null, null);
    }

    private void mockProductionInboundSource(ErpFinanceDualProductCostServiceImpl service, Long firstInboundId,
                                             Long firstProductId, Long firstOrderId, Long secondInboundId,
                                             Long secondProductId, Long secondOrderId) throws Exception {
        mockProductionInboundSource(service, firstInboundId, firstProductId, firstOrderId, secondInboundId,
                secondProductId, secondOrderId, null);
    }

    private void mockProductionInboundSource(ErpFinanceDualProductCostServiceImpl service, Long firstInboundId,
                                             Long firstProductId, Long firstOrderId, Long secondInboundId,
                                             Long secondProductId, Long secondOrderId, Object ignored) throws Exception {
        setField(service, "productionInboundMapper", createProxy(
                cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionInboundMapper.class, (methodName, args) -> {
                    if ("selectBatchIds".equals(methodName)) {
                        List<ErpProductionInboundDO> list = new ArrayList<>();
                        list.add(new ErpProductionInboundDO().setId(firstInboundId).setProductId(firstProductId)
                                .setProductionOrderId(firstOrderId));
                        if (secondInboundId != null) {
                            list.add(new ErpProductionInboundDO().setId(secondInboundId).setProductId(secondProductId)
                                    .setProductionOrderId(secondOrderId));
                        }
                        return list;
                    }
                    return null;
                }));
        setField(service, "outsourceInboundMapper", createProxy(
                cn.weitee.erp.module.erp.dal.mysql.mrp.ErpOutsourceInboundMapper.class, (methodName, args) -> List.of()));
        setField(service, "outsourceOrderMapper", createProxy(
                cn.weitee.erp.module.erp.dal.mysql.mrp.ErpOutsourceOrderMapper.class, (methodName, args) -> List.of()));
    }

    @FunctionalInterface
    private interface MethodHandler {
        Object handle(String methodName, Object[] args);
    }
}
