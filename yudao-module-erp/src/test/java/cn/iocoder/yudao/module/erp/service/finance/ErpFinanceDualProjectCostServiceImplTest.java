package cn.iocoder.yudao.module.erp.service.finance;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.projectdualcost.ErpFinanceDualProjectCostPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.projectdualcost.ErpFinanceDualProjectCostRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.projectdualcost.ErpFinanceDualProjectCostRebuildReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceDualProjectCostResultDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceDualProjectCostItemDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceDualProjectCostRebuildLogDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceDualLedgerDiffConfigDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceExpenseDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.project.ErpProjectDO;
import cn.iocoder.yudao.module.erp.service.finance.diffcalc.AmountDiffCalculatorFactory;
import cn.iocoder.yudao.module.erp.service.finance.diffcalc.ProRataAmountDiffCalculator;
import cn.iocoder.yudao.module.erp.service.finance.diffcalc.FixedVarianceAmountDiffCalculator;
import cn.iocoder.yudao.module.erp.service.finance.diffcalc.SourceMappingAmountDiffCalculator;
import cn.iocoder.yudao.module.erp.enums.ErpFinanceDiffCalculationTypeEnum;
import cn.iocoder.yudao.module.erp.enums.common.ErpBizTypeEnum;
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
 * 项目双账成本 Service 单元测试
 */
class ErpFinanceDualProjectCostServiceImplTest {

    // ========== getProjectDualCostPage 测试 ==========

    @Test
    void getProjectDualCostPage_shouldReturnResults() throws Exception {
        ErpFinanceDualProjectCostServiceImpl service = new ErpFinanceDualProjectCostServiceImpl();

        // Mock resultMapper 返回一条结果
        AtomicLong idGen = new AtomicLong(1);
        setField(service, "projectCostResultMapper", createProxy(
                cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinanceDualProjectCostResultMapper.class,
                (methodName, args) -> {
                    if ("selectPage".equals(methodName)) {
                        List<ErpFinanceDualProjectCostResultDO> list = new ArrayList<>();
                        list.add(new ErpFinanceDualProjectCostResultDO()
                                .setId(idGen.getAndIncrement())
                                .setProjectId(1L).setProjectNo("PRJ-001").setProjectName("研发项目A")
                                .setPeriod("2026-05").setCostType(10)
                                .setExternalAmount(new BigDecimal("10000.00"))
                                .setInternalAmount(new BigDecimal("12000.00"))
                                .setDiffAmount(new BigDecimal("2000.00"))
                                .setSourceCount(3).setStatus(0).setVersionNo(1));
                        return new PageResult<>(list, 1L);
                    }
                    return null;
                }));

        PageResult<ErpFinanceDualProjectCostRespVO> result =
                service.getProjectDualCostPage(new ErpFinanceDualProjectCostPageReqVO());

        assertEquals(1, result.getList().size());
        ErpFinanceDualProjectCostRespVO vo = result.getList().get(0);
        assertEquals("PRJ-001", vo.getProjectNo());
        assertEquals("研发项目A", vo.getProjectName());
        assertEquals("2026-05", vo.getPeriod());
        assertEquals(new BigDecimal("10000.00"), vo.getExternalAmount());
        assertEquals(new BigDecimal("12000.00"), vo.getInternalAmount());
        assertEquals(new BigDecimal("2000.00"), vo.getDiffAmount());
    }

    // ========== rebuildProjectDualCost 测试 ==========

    @Test
    void rebuildProjectDualCost_shouldAggregateByCostType() throws Exception {
        ErpFinanceDualProjectCostServiceImpl service = new ErpFinanceDualProjectCostServiceImpl();

        // 记录插入的结果
        List<ErpFinanceDualProjectCostResultDO> insertedResults = new ArrayList<>();
        List<ErpFinanceDualProjectCostItemDO> insertedItems = new ArrayList<>();
        AtomicLong idGen = new AtomicLong(1);

        // Mock resultMapper
        setField(service, "projectCostResultMapper", createProxy(
                cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinanceDualProjectCostResultMapper.class,
                (methodName, args) -> {
                    if ("delete".equals(methodName)) return 0;
                    if ("insert".equals(methodName)) {
                        ErpFinanceDualProjectCostResultDO r = (ErpFinanceDualProjectCostResultDO) args[0];
                        r.setId(idGen.getAndIncrement());
                        insertedResults.add(r);
                        return 1;
                    }
                    return null;
                }));

        // Mock itemMapper
        setField(service, "projectCostItemMapper", createProxy(
                cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinanceDualProjectCostItemMapper.class,
                (methodName, args) -> {
                    if ("delete".equals(methodName)) return 0;
                    if ("insert".equals(methodName)) {
                        ErpFinanceDualProjectCostItemDO item = (ErpFinanceDualProjectCostItemDO) args[0];
                        item.setId(idGen.getAndIncrement());
                        insertedItems.add(item);
                        return 1;
                    }
                    return null;
                }));

        // Mock rebuildLogMapper
        setField(service, "rebuildLogMapper", createProxy(
                cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinanceDualProjectCostRebuildLogMapper.class,
                (methodName, args) -> {
                    if ("insert".equals(methodName)) {
                        ErpFinanceDualProjectCostRebuildLogDO log = (ErpFinanceDualProjectCostRebuildLogDO) args[0];
                        log.setId(idGen.getAndIncrement());
                        return 1;
                    }
                    if ("updateById".equals(methodName)) return 1;
                    return null;
                }));

        // Mock expenseMapper：返回两条费用报销
        setField(service, "expenseMapper", createProxy(
                cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinanceExpenseMapper.class,
                (methodName, args) -> {
                    if ("selectList".equals(methodName)) {
                        return List.of(
                                new ErpFinanceExpenseDO().setId(101L).setNo("EXP-001")
                                        .setProjectId(1L).setStatus(10)
                                        .setExpenseTime(LocalDateTime.of(2026, 5, 15, 10, 0))
                                        .setExpensePrice(new BigDecimal("1000.00"))
                                        .setResearchCategory(1),
                                new ErpFinanceExpenseDO().setId(102L).setNo("EXP-002")
                                        .setProjectId(1L).setStatus(10)
                                        .setExpenseTime(LocalDateTime.of(2026, 5, 20, 14, 0))
                                        .setExpensePrice(new BigDecimal("2000.00"))
                                        .setResearchCategory(1));
                    }
                    return null;
                }));

        // Mock projectService
        setField(service, "projectService", createProxy(
                cn.iocoder.yudao.module.erp.service.project.ErpProjectService.class,
                (methodName, args) -> {
                    if ("getProject".equals(methodName)) {
                        return new ErpProjectDO().setId(1L).setNo("PRJ-001").setName("研发项目A");
                    }
                    return null;
                }));

        // Mock diffConfigService：返回空（无差异配置，external = internal）
        setField(service, "dualLedgerDiffConfigService", createProxy(
                ErpFinanceDualLedgerDiffConfigService.class,
                (methodName, args) -> List.of()));

        // Mock amountDiffCalculatorFactory
        AmountDiffCalculatorFactory factory = new AmountDiffCalculatorFactory();
        Field calcMapField = AmountDiffCalculatorFactory.class.getDeclaredField("calculatorMap");
        calcMapField.setAccessible(true);
        java.util.Map<Integer, ?> calcMap = (java.util.Map<Integer, ?>) calcMapField.get(factory);
        ProRataAmountDiffCalculator proRata = new ProRataAmountDiffCalculator();
        calcMap.put(proRata.getCalculationType(), proRata);
        setField(service, "amountDiffCalculatorFactory", factory);

        // 执行 rebuild
        ErpFinanceDualProjectCostRebuildReqVO reqVO = new ErpFinanceDualProjectCostRebuildReqVO();
        reqVO.setProjectId(1L);
        reqVO.setPeriod("2026-05");
        service.rebuildProjectDualCost(1L, reqVO);

        // 验证：两条费用按 researchCategory=1 归为同一 costType(20=人工)
        assertEquals(1, insertedResults.size(), "应生成 1 条结果（同一 costType 合并）");
        ErpFinanceDualProjectCostResultDO result = insertedResults.get(0);
        assertEquals(20, result.getCostType().intValue(), "researchCategory=1 应归为 costType=20(人工)");
        assertEquals(new BigDecimal("3000.00"), result.getInternalAmount(), "内部账 = 1000+2000");
        assertEquals(new BigDecimal("3000.00"), result.getExternalAmount(), "无差异配置时外部账=内部账");
        assertEquals(2, result.getSourceCount().intValue(), "来源单据数 = 2");
        assertEquals(2, insertedItems.size(), "应生成 2 条明细");
    }

    @Test
    void rebuildProjectDualCost_withDiffConfig_shouldApplyRatio() throws Exception {
        ErpFinanceDualProjectCostServiceImpl service = new ErpFinanceDualProjectCostServiceImpl();

        List<ErpFinanceDualProjectCostResultDO> insertedResults = new ArrayList<>();
        List<ErpFinanceDualProjectCostItemDO> insertedItems = new ArrayList<>();
        AtomicLong idGen = new AtomicLong(1);

        setField(service, "projectCostResultMapper", createProxy(
                cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinanceDualProjectCostResultMapper.class,
                (methodName, args) -> {
                    if ("delete".equals(methodName)) return 0;
                    if ("insert".equals(methodName)) {
                        ErpFinanceDualProjectCostResultDO r = (ErpFinanceDualProjectCostResultDO) args[0];
                        r.setId(idGen.getAndIncrement());
                        insertedResults.add(r);
                        return 1;
                    }
                    return null;
                }));
        setField(service, "projectCostItemMapper", createProxy(
                cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinanceDualProjectCostItemMapper.class,
                (methodName, args) -> {
                    if ("delete".equals(methodName)) return 0;
                    if ("insert".equals(methodName)) {
                        ErpFinanceDualProjectCostItemDO item = (ErpFinanceDualProjectCostItemDO) args[0];
                        item.setId(idGen.getAndIncrement());
                        insertedItems.add(item);
                        return 1;
                    }
                    return null;
                }));
        setField(service, "rebuildLogMapper", createProxy(
                cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinanceDualProjectCostRebuildLogMapper.class,
                (methodName, args) -> {
                    if ("insert".equals(methodName)) {
                        ErpFinanceDualProjectCostRebuildLogDO log = (ErpFinanceDualProjectCostRebuildLogDO) args[0];
                        log.setId(idGen.getAndIncrement());
                        return 1;
                    }
                    if ("updateById".equals(methodName)) return 1;
                    return null;
                }));
        setField(service, "expenseMapper", createProxy(
                cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinanceExpenseMapper.class,
                (methodName, args) -> {
                    if ("selectList".equals(methodName)) {
                        return List.of(new ErpFinanceExpenseDO().setId(101L).setNo("EXP-001")
                                .setProjectId(1L).setStatus(10)
                                .setExpenseTime(LocalDateTime.of(2026, 5, 15, 10, 0))
                                .setExpensePrice(new BigDecimal("10000.00"))
                                .setResearchCategory(1));
                    }
                    return null;
                }));
        setField(service, "projectService", createProxy(
                cn.iocoder.yudao.module.erp.service.project.ErpProjectService.class,
                (methodName, args) -> {
                    if ("getProject".equals(methodName)) {
                        return new ErpProjectDO().setId(1L).setNo("PRJ-001").setName("研发项目A");
                    }
                    return null;
                }));

        // Mock diffConfigService：costType=20(人工) 配置按比例 0.85
        setField(service, "dualLedgerDiffConfigService", createProxy(
                ErpFinanceDualLedgerDiffConfigService.class,
                (methodName, args) -> {
                    if ("getDualLedgerDiffConfigList".equals(methodName)) {
                        return List.of(new ErpFinanceDualLedgerDiffConfigDO()
                                .setBizType(60).setDiffItemType(20)
                                .setCalculationType(ErpFinanceDiffCalculationTypeEnum.PRO_RATA.getType())
                                .setRatio(new BigDecimal("0.85")));
                    }
                    return List.of();
                }));

        // Mock factory（用真实的 ProRata 计算器）
        AmountDiffCalculatorFactory factory = new AmountDiffCalculatorFactory();
        Field calcMapField = AmountDiffCalculatorFactory.class.getDeclaredField("calculatorMap");
        calcMapField.setAccessible(true);
        java.util.Map<Integer, ?> calcMap = (java.util.Map<Integer, ?>) calcMapField.get(factory);
        ProRataAmountDiffCalculator proRata = new ProRataAmountDiffCalculator();
        calcMap.put(proRata.getCalculationType(), proRata);
        setField(service, "amountDiffCalculatorFactory", factory);

        // 执行
        ErpFinanceDualProjectCostRebuildReqVO reqVO = new ErpFinanceDualProjectCostRebuildReqVO();
        reqVO.setProjectId(1L);
        reqVO.setPeriod("2026-05");
        service.rebuildProjectDualCost(1L, reqVO);

        // 验证：内部账 10000，外部账 = 10000 × 0.85 = 8500
        assertEquals(1, insertedResults.size());
        ErpFinanceDualProjectCostResultDO result = insertedResults.get(0);
        assertEquals(new BigDecimal("10000.00"), result.getInternalAmount());
        assertEquals(new BigDecimal("8500.00"), result.getExternalAmount(), "外部账 = 内部账 × 0.85");
        assertEquals(new BigDecimal("1500.00"), result.getDiffAmount(), "差异 = 10000 - 8500");

        // 验证明细
        assertEquals(1, insertedItems.size());
        ErpFinanceDualProjectCostItemDO item = insertedItems.get(0);
        assertEquals(new BigDecimal("10000.00"), item.getInternalAmount());
        assertEquals(new BigDecimal("8500.00"), item.getExternalAmount());
        assertEquals(new BigDecimal("1500.00"), item.getDiffAmount());
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

    @FunctionalInterface
    private interface MethodHandler {
        Object handle(String methodName, Object[] args);
    }
}
