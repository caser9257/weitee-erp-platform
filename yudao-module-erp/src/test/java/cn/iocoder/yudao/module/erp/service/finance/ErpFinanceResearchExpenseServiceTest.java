package cn.iocoder.yudao.module.erp.service.finance;

import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.expense.ErpFinanceResearchExpenseSummaryReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.expense.ErpFinanceResearchExpenseSummaryRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceExpenseDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.project.ErpProjectDO;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinanceExpenseMapper;
import cn.iocoder.yudao.module.erp.enums.ErpAuditStatus;
import cn.iocoder.yudao.module.erp.enums.ErpResearchExpenseCategoryEnum;
import cn.iocoder.yudao.module.erp.enums.common.ErpBizTypeEnum;
import cn.iocoder.yudao.module.erp.service.project.ErpProjectService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ErpFinanceResearchExpenseServiceTest {

    @Test
    void getResearchExpenseSummary_shouldReturnSummary() throws Exception {
        // 准备测试数据
        ErpFinanceResearchExpenseServiceImpl service = new ErpFinanceResearchExpenseServiceImpl();
        
        // 模拟Mapper返回数据
        ErpFinanceExpenseDO expense1 = new ErpFinanceExpenseDO();
        expense1.setId(1L);
        expense1.setProjectId(100L);
        expense1.setResearchCategory(ErpResearchExpenseCategoryEnum.EXPENSE.getType());
        expense1.setExpensePrice(new BigDecimal("1000.00"));
        
        ErpFinanceExpenseDO expense2 = new ErpFinanceExpenseDO();
        expense2.setId(2L);
        expense2.setProjectId(100L);
        expense2.setResearchCategory(ErpResearchExpenseCategoryEnum.EXPENSE.getType());
        expense2.setExpensePrice(new BigDecimal("2000.00"));
        
        ErpFinanceExpenseDO expense3 = new ErpFinanceExpenseDO();
        expense3.setId(3L);
        expense3.setProjectId(100L);
        expense3.setResearchCategory(ErpResearchExpenseCategoryEnum.CAPITALIZE.getType());
        expense3.setExpensePrice(new BigDecimal("5000.00"));
        
        List<ErpFinanceExpenseDO> mockExpenses = Arrays.asList(expense1, expense2, expense3);
        
        // 设置模拟对象
        ErpFinanceExpenseMapper mockMapper = createMockMapper(mockExpenses);
        setField(service, "financeExpenseMapper", mockMapper);
        
        ErpProjectService mockProjectService = createMockProjectService();
        setField(service, "projectService", mockProjectService);
        
        // 执行测试
        ErpFinanceResearchExpenseSummaryReqVO reqVO = new ErpFinanceResearchExpenseSummaryReqVO();
        reqVO.setProjectId(100L);
        
        List<ErpFinanceResearchExpenseSummaryRespVO> result = service.getResearchExpenseSummary(reqVO);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size()); // 两个分类：费用化和资本化
        
        // 验证费用化汇总
        ErpFinanceResearchExpenseSummaryRespVO expenseSummary = result.stream()
                .filter(r -> ErpResearchExpenseCategoryEnum.EXPENSE.getType().equals(r.getResearchCategory()))
                .findFirst()
                .orElse(null);
        assertNotNull(expenseSummary);
        assertEquals(new BigDecimal("3000.00"), expenseSummary.getTotalAmount());
        assertEquals(new BigDecimal("3000.00"), expenseSummary.getExpenseAmount());
        assertEquals(BigDecimal.ZERO, expenseSummary.getCapitalizeAmount());
        assertEquals(2, expenseSummary.getCount());
        
        // 验证资本化汇总
        ErpFinanceResearchExpenseSummaryRespVO capitalizeSummary = result.stream()
                .filter(r -> ErpResearchExpenseCategoryEnum.CAPITALIZE.getType().equals(r.getResearchCategory()))
                .findFirst()
                .orElse(null);
        assertNotNull(capitalizeSummary);
        assertEquals(new BigDecimal("5000.00"), capitalizeSummary.getTotalAmount());
        assertEquals(BigDecimal.ZERO, capitalizeSummary.getExpenseAmount());
        assertEquals(new BigDecimal("5000.00"), capitalizeSummary.getCapitalizeAmount());
        assertEquals(1, capitalizeSummary.getCount());
    }
    
    @Test
    void monthlyCarryForward_shouldProcessExpensesAndGenerateVouchers() throws Exception {
        // 准备测试数据
        ErpFinanceResearchExpenseServiceImpl service = new ErpFinanceResearchExpenseServiceImpl();
        
        ErpFinanceExpenseDO expense1 = new ErpFinanceExpenseDO();
        expense1.setId(1L);
        expense1.setProjectId(100L);
        expense1.setResearchCategory(ErpResearchExpenseCategoryEnum.EXPENSE.getType());
        expense1.setExpensePrice(new BigDecimal("1000.00"));
        expense1.setStatus(ErpAuditStatus.APPROVE.getStatus());
        expense1.setExpenseTime(LocalDateTime.of(2026, 3, 15, 10, 0, 0));
        
        ErpFinanceExpenseDO expense2 = new ErpFinanceExpenseDO();
        expense2.setId(2L);
        expense2.setProjectId(100L);
        expense2.setResearchCategory(ErpResearchExpenseCategoryEnum.CAPITALIZE.getType());
        expense2.setExpensePrice(new BigDecimal("5000.00"));
        expense2.setStatus(ErpAuditStatus.APPROVE.getStatus());
        expense2.setExpenseTime(LocalDateTime.of(2026, 3, 20, 10, 0, 0));
        
        List<ErpFinanceExpenseDO> mockExpenses = Arrays.asList(expense1, expense2);
        
        // 设置模拟对象
        ErpFinanceExpenseMapper mockMapper = createMockMapperWithUpdate(mockExpenses);
        setField(service, "financeExpenseMapper", mockMapper);
        
        ErpProjectService mockProjectService = createMockProjectService();
        setField(service, "projectService", mockProjectService);
        
        AtomicInteger voucherGenerateCount = new AtomicInteger(0);
        ErpFinanceVoucherService mockVoucherService = createMockVoucherService(voucherGenerateCount);
        setField(service, "voucherService", mockVoucherService);
        
        // 执行测试
        service.monthlyCarryForward(1L, 2026, 3);
        
        // 验证结果
        // 验证凭证生成了2次（每笔费用一次）
        assertEquals(2, voucherGenerateCount.get());
        
        // 验证费用状态更新为已结转
        assertEquals(ErpAuditStatus.CARRY_FORWARD.getStatus(), expense1.getStatus());
        assertEquals(ErpAuditStatus.CARRY_FORWARD.getStatus(), expense2.getStatus());
    }
    
    @Test
    void monthlyCarryForward_shouldDoNothingWhenNoExpenses() throws Exception {
        // 准备测试数据
        ErpFinanceResearchExpenseServiceImpl service = new ErpFinanceResearchExpenseServiceImpl();
        
        // 设置模拟对象 - 返回空列表
        ErpFinanceExpenseMapper mockMapper = createMockMapper(Collections.emptyList());
        setField(service, "financeExpenseMapper", mockMapper);
        
        ErpProjectService mockProjectService = createMockProjectService();
        setField(service, "projectService", mockProjectService);
        
        AtomicInteger voucherGenerateCount = new AtomicInteger(0);
        ErpFinanceVoucherService mockVoucherService = createMockVoucherService(voucherGenerateCount);
        setField(service, "voucherService", mockVoucherService);
        
        // 执行测试
        service.monthlyCarryForward(1L, 2026, 3);
        
        // 验证结果 - 没有费用时不应生成凭证
        assertEquals(0, voucherGenerateCount.get());
    }
    
    private ErpFinanceExpenseMapper createMockMapper(List<ErpFinanceExpenseDO> expenses) {
        return (ErpFinanceExpenseMapper) java.lang.reflect.Proxy.newProxyInstance(
                ErpFinanceExpenseMapper.class.getClassLoader(),
                new Class[]{ErpFinanceExpenseMapper.class},
                (proxy, method, args) -> {
                    if ("selectListByResearchExpense".equals(method.getName())) {
                        return expenses;
                    }
                    return null;
                }
        );
    }
    
    private ErpFinanceExpenseMapper createMockMapperWithUpdate(List<ErpFinanceExpenseDO> expenses) {
        return (ErpFinanceExpenseMapper) java.lang.reflect.Proxy.newProxyInstance(
                ErpFinanceExpenseMapper.class.getClassLoader(),
                new Class[]{ErpFinanceExpenseMapper.class},
                (proxy, method, args) -> {
                    if ("selectListByResearchExpense".equals(method.getName())) {
                        return expenses;
                    }
                    if ("updateById".equals(method.getName())) {
                        return 1; // 模拟更新成功
                    }
                    return null;
                }
        );
    }
    
    private ErpFinanceVoucherService createMockVoucherService(AtomicInteger generateCount) {
        return (ErpFinanceVoucherService) java.lang.reflect.Proxy.newProxyInstance(
                ErpFinanceVoucherService.class.getClassLoader(),
                new Class[]{ErpFinanceVoucherService.class},
                (proxy, method, args) -> {
                    if ("autoGenerateVoucher".equals(method.getName())) {
                        generateCount.incrementAndGet();
                        return 100L; // 返回模拟的凭证ID
                    }
                    return null;
                }
        );
    }
    
    private ErpProjectService createMockProjectService() {
        return (ErpProjectService) java.lang.reflect.Proxy.newProxyInstance(
                ErpProjectService.class.getClassLoader(),
                new Class[]{ErpProjectService.class},
                (proxy, method, args) -> {
                    if ("getProject".equals(method.getName())) {
                        ErpProjectDO project = new ErpProjectDO();
                        project.setId(100L);
                        project.setName("测试项目");
                        return project;
                    }
                    if ("getProjectList".equals(method.getName())) {
                        ErpProjectDO project = new ErpProjectDO();
                        project.setId(100L);
                        project.setName("测试项目");
                        return Arrays.asList(project);
                    }
                    return null;
                }
        );
    }
    
    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}