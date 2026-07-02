package cn.weitee.erp.module.erp.service.finance;

import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.expense.ErpFinanceExpenseProjectSummaryReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.expense.ErpFinanceExpenseProjectSummaryRespVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.expense.ErpFinanceExpenseSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpApStatementDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceExpenseDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceExpenseItemDO;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceExpenseItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceExpenseMapper;
import cn.weitee.erp.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.enums.ErpFinanceExpenseAccountingTypeEnum;
import cn.weitee.erp.module.erp.enums.common.ErpBizTypeEnum;
import cn.weitee.erp.module.erp.service.finance.ErpFinanceBizHookService;
import cn.weitee.erp.module.erp.service.project.ErpProjectService;
import cn.weitee.erp.module.erp.service.purchase.ErpSupplierService;
import cn.weitee.erp.module.system.api.dept.DeptApi;
import cn.weitee.erp.module.system.api.user.AdminUserApi;
import cn.weitee.erp.module.system.dal.dataobject.dict.DictDataDO;
import cn.weitee.erp.module.system.service.dict.DictDataService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsExpense.EXPENSE_RD_ACCOUNTING_TYPE_INVALID;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsExpense.EXPENSE_RD_ACCOUNTING_TYPE_REQUIRED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ErpFinanceExpenseServiceImplTest {

    @Test
    void createFinanceExpense_shouldInsertHeadAndItems() throws Exception {
        ErpFinanceExpenseServiceImpl service = new ErpFinanceExpenseServiceImpl();
        AtomicReference<ErpFinanceExpenseDO> insertedExpenseRef = new AtomicReference<>();
        AtomicReference<List<ErpFinanceExpenseItemDO>> insertedItemsRef = new AtomicReference<>();

        setField(service, "erpFinanceExpenseMapper", createProxy(ErpFinanceExpenseMapper.class, (methodName, args) -> {
            if ("selectByNo".equals(methodName)) {
                return null;
            }
            if ("insert".equals(methodName)) {
                ErpFinanceExpenseDO expense = (ErpFinanceExpenseDO) args[0];
                expense.setId(101L);
                insertedExpenseRef.set(expense);
                return 1;
            }
            return null;
        }));
        setField(service, "erpFinanceExpenseItemMapper", createProxy(ErpFinanceExpenseItemMapper.class, (methodName, args) -> {
            if ("insertBatch".equals(methodName)) {
                insertedItemsRef.set((List<ErpFinanceExpenseItemDO>) args[0]);
                return true;
            }
            return null;
        }));
        setField(service, "noRedisDAO", new ErpNoRedisDAO() {
            @Override
            public String generate(String prefix) {
                return "LSBX20260429000001";
            }
        });
        setField(service, "deptApi", createProxy(DeptApi.class, (methodName, args) -> null));
        setField(service, "projectService", createProxy(ErpProjectService.class, (methodName, args) -> null));
        setField(service, "supplierService", createProxy(ErpSupplierService.class, (methodName, args) -> null));
        setField(service, "accountService", createProxy(ErpAccountService.class, (methodName, args) -> null));
        setField(service, "adminUserApi", createProxy(AdminUserApi.class, (methodName, args) -> null));
        setField(service, "apStatementService", createProxy(ErpApStatementService.class, (methodName, args) -> null));
        setField(service, "financeExpenseTypeService", createExpenseTypeService());

        ErpFinanceExpenseSaveReqVO reqVO = new ErpFinanceExpenseSaveReqVO();
        reqVO.setExpenseTime(LocalDateTime.of(2026, 4, 29, 9, 0));
        reqVO.setExpenseType(80);
        reqVO.setDeptId(1L);
        reqVO.setSupplierId(2L);
        reqVO.setAccountId(3L);
        reqVO.setExpensePrice(new BigDecimal("300.00"));
        reqVO.setRemark("零星采购");
        reqVO.setItems(List.of(
                buildItem(null, "螺丝刀", "100.00", "工具"),
                buildItem(null, "测试材料", "200.00", "样机")));

        Long id = service.createFinanceExpense(reqVO);

        assertEquals(101L, id);
        assertEquals("LSBX20260429000001", insertedExpenseRef.get().getNo());
        assertEquals(ErpAuditStatus.PROCESS.getStatus(), insertedExpenseRef.get().getStatus());
        assertEquals(new BigDecimal("300.00"), insertedExpenseRef.get().getRemainPrice());
        assertEquals(2, insertedItemsRef.get().size());
        assertEquals(101L, insertedItemsRef.get().get(0).getExpenseId());
    }

    @Test
    void createFinanceExpense_shouldRequireRdAccountingTypeForResearchExpense() throws Exception {
        ErpFinanceExpenseServiceImpl service = new ErpFinanceExpenseServiceImpl();
        setField(service, "deptApi", createProxy(DeptApi.class, (methodName, args) -> null));
        setField(service, "projectService", createProxy(ErpProjectService.class, (methodName, args) -> null));
        setField(service, "supplierService", createProxy(ErpSupplierService.class, (methodName, args) -> null));
        setField(service, "accountService", createProxy(ErpAccountService.class, (methodName, args) -> null));
        setField(service, "adminUserApi", createProxy(AdminUserApi.class, (methodName, args) -> null));
        setField(service, "financeExpenseTypeService", createExpenseTypeService());

        ErpFinanceExpenseSaveReqVO reqVO = new ErpFinanceExpenseSaveReqVO();
        reqVO.setExpenseTime(LocalDateTime.of(2026, 5, 24, 9, 0));
        reqVO.setExpenseType(10);
        reqVO.setDeptId(1L);
        reqVO.setProjectId(100L);
        reqVO.setSupplierId(2L);
        reqVO.setAccountId(3L);
        reqVO.setExpensePrice(new BigDecimal("100.00"));
        reqVO.setItems(List.of(buildItem(null, "研发测试", "100.00", "样机")));

        ServiceException ex = assertThrows(ServiceException.class, () -> service.createFinanceExpense(reqVO));

        assertEquals(EXPENSE_RD_ACCOUNTING_TYPE_REQUIRED.getCode(), ex.getCode());
    }

    @Test
    void createFinanceExpense_shouldRejectInvalidRdAccountingTypeForResearchExpense() throws Exception {
        ErpFinanceExpenseServiceImpl service = new ErpFinanceExpenseServiceImpl();
        setField(service, "deptApi", createProxy(DeptApi.class, (methodName, args) -> null));
        setField(service, "projectService", createProxy(ErpProjectService.class, (methodName, args) -> null));
        setField(service, "supplierService", createProxy(ErpSupplierService.class, (methodName, args) -> null));
        setField(service, "accountService", createProxy(ErpAccountService.class, (methodName, args) -> null));
        setField(service, "adminUserApi", createProxy(AdminUserApi.class, (methodName, args) -> null));
        setField(service, "financeExpenseTypeService", createExpenseTypeService());

        ErpFinanceExpenseSaveReqVO reqVO = new ErpFinanceExpenseSaveReqVO();
        reqVO.setExpenseTime(LocalDateTime.of(2026, 5, 24, 9, 0));
        reqVO.setExpenseType(10);
        reqVO.setDeptId(1L);
        reqVO.setProjectId(100L);
        reqVO.setSupplierId(2L);
        reqVO.setAccountId(3L);
        reqVO.setExpensePrice(new BigDecimal("100.00"));
        reqVO.setRdAccountingType(99);
        reqVO.setItems(List.of(buildItem(null, "研发测试", "100.00", "样机")));

        ServiceException ex = assertThrows(ServiceException.class, () -> service.createFinanceExpense(reqVO));

        assertEquals(EXPENSE_RD_ACCOUNTING_TYPE_INVALID.getCode(), ex.getCode());
    }

    @Test
    void createFinanceExpense_shouldRejectRdAccountingTypeForNonResearchExpense() throws Exception {
        ErpFinanceExpenseServiceImpl service = new ErpFinanceExpenseServiceImpl();
        setField(service, "deptApi", createProxy(DeptApi.class, (methodName, args) -> null));
        setField(service, "projectService", createProxy(ErpProjectService.class, (methodName, args) -> null));
        setField(service, "supplierService", createProxy(ErpSupplierService.class, (methodName, args) -> null));
        setField(service, "accountService", createProxy(ErpAccountService.class, (methodName, args) -> null));
        setField(service, "adminUserApi", createProxy(AdminUserApi.class, (methodName, args) -> null));
        setField(service, "financeExpenseTypeService", createExpenseTypeService());

        ErpFinanceExpenseSaveReqVO reqVO = new ErpFinanceExpenseSaveReqVO();
        reqVO.setExpenseTime(LocalDateTime.of(2026, 5, 24, 9, 0));
        reqVO.setExpenseType(80);
        reqVO.setDeptId(1L);
        reqVO.setSupplierId(2L);
        reqVO.setAccountId(3L);
        reqVO.setExpensePrice(new BigDecimal("100.00"));
        reqVO.setRdAccountingType(ErpFinanceExpenseAccountingTypeEnum.CAPITALIZE.getType());
        reqVO.setItems(List.of(buildItem(null, "零星采购", "100.00", "工具")));

        ServiceException ex = assertThrows(ServiceException.class, () -> service.createFinanceExpense(reqVO));

        assertEquals(EXPENSE_RD_ACCOUNTING_TYPE_INVALID.getCode(), ex.getCode());
    }

    @Test
    void updateFinanceExpenseStatus_shouldCreateStatementAndDefaultItemOnApprove() throws Exception {
        ErpFinanceExpenseServiceImpl service = new ErpFinanceExpenseServiceImpl();
        ErpFinanceExpenseDO expense = new ErpFinanceExpenseDO()
                .setId(11L)
                .setNo("LSBX20260429000001")
                .setStatus(ErpAuditStatus.PROCESS.getStatus())
                .setExpenseTime(LocalDateTime.of(2026, 4, 29, 9, 0))
                .setExpenseType(80)
                .setDeptId(1L)
                .setSupplierId(2L)
                .setAccountId(3L)
                .setExpensePrice(new BigDecimal("300.00"))
                .setRemark("零星采购");
        AtomicReference<ErpFinanceExpenseDO> updatedExpenseRef = new AtomicReference<>();
        AtomicReference<ErpFinanceExpenseItemDO> insertedItemRef = new AtomicReference<>();
        AtomicReference<ErpFinanceExpenseDO> approvedExpenseRef = new AtomicReference<>();
        AtomicReference<Integer> autoGenerateBizTypeRef = new AtomicReference<>();
        AtomicReference<Long> autoGenerateBizIdRef = new AtomicReference<>();
        AtomicReference<LocalDate> financeHookBizDateRef = new AtomicReference<>();

        setField(service, "erpFinanceExpenseMapper", createProxy(ErpFinanceExpenseMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return expense;
            }
            if ("updateByIdAndStatus".equals(methodName)) {
                updatedExpenseRef.set((ErpFinanceExpenseDO) args[2]);
                expense.setStatus(updatedExpenseRef.get().getStatus());
                return 1;
            }
            return null;
        }));
        setField(service, "erpFinanceExpenseItemMapper", createProxy(ErpFinanceExpenseItemMapper.class, (methodName, args) -> {
            if ("selectListByExpenseId".equals(methodName)) {
                return List.of();
            }
            if ("insert".equals(methodName)) {
                insertedItemRef.set((ErpFinanceExpenseItemDO) args[0]);
                return 1;
            }
            return null;
        }));
        setField(service, "deptApi", createProxy(DeptApi.class, (methodName, args) -> null));
        setField(service, "projectService", createProxy(ErpProjectService.class, (methodName, args) -> null));
        setField(service, "supplierService", createProxy(ErpSupplierService.class, (methodName, args) -> null));
        setField(service, "accountService", createProxy(ErpAccountService.class, (methodName, args) -> null));
        setField(service, "adminUserApi", createProxy(AdminUserApi.class, (methodName, args) -> null));
        setField(service, "financeExpenseTypeService", createExpenseTypeService());
        setField(service, "apStatementService", createProxy(ErpApStatementService.class, (methodName, args) -> {
            if ("createStatementForFinanceExpense".equals(methodName)) {
                approvedExpenseRef.set((ErpFinanceExpenseDO) args[0]);
            }
            return null;
        }));
        setField(service, "financeBizHookService", createProxy(ErpFinanceBizHookService.class, (methodName, args) -> {
            if ("handleApprovedBiz".equals(methodName)) {
                autoGenerateBizTypeRef.set((Integer) args[0]);
                autoGenerateBizIdRef.set((Long) args[1]);
                financeHookBizDateRef.set((LocalDate) args[2]);
                return 99L;
            }
            return null;
        }));
        setField(service, "financeAssetCandidateService", createProxy(ErpFinanceAssetCandidateService.class, (methodName, args) -> null));

        service.updateFinanceExpenseStatus(11L, ErpAuditStatus.APPROVE.getStatus());

        assertEquals(ErpAuditStatus.APPROVE.getStatus(), updatedExpenseRef.get().getStatus());
        assertEquals("零星采购汇总", insertedItemRef.get().getItemName());
        assertEquals(new BigDecimal("300.00"), insertedItemRef.get().getAmount());
        assertEquals(11L, approvedExpenseRef.get().getId());
        assertEquals(ErpBizTypeEnum.FINANCE_EXPENSE.getType(), autoGenerateBizTypeRef.get());
        assertEquals(11L, autoGenerateBizIdRef.get());
        assertEquals(LocalDate.of(2026, 4, 29), financeHookBizDateRef.get());
    }

    @Test
    void updateFinanceExpenseStatus_shouldRejectRollbackWhenPaid() throws Exception {
        ErpFinanceExpenseServiceImpl service = new ErpFinanceExpenseServiceImpl();
        ErpFinanceExpenseDO expense = new ErpFinanceExpenseDO()
                .setId(11L)
                .setNo("LSBX20260429000001")
                .setStatus(ErpAuditStatus.APPROVE.getStatus());
        ErpApStatementDO statement = new ErpApStatementDO()
                .setId(21L)
                .setBizType(ErpBizTypeEnum.FINANCE_EXPENSE.getType())
                .setBizId(11L)
                .setPaidAmount(new BigDecimal("1.00"));

        setField(service, "erpFinanceExpenseMapper", createProxy(ErpFinanceExpenseMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return expense;
            }
            return null;
        }));
        setField(service, "erpFinanceExpenseItemMapper", createProxy(ErpFinanceExpenseItemMapper.class, (methodName, args) -> List.of()));
        setField(service, "apStatementService", createProxy(ErpApStatementService.class, (methodName, args) -> {
            if ("getApStatementByBizTypeAndBizId".equals(methodName)) {
                return statement;
            }
            return null;
        }));

        assertThrows(RuntimeException.class,
                () -> service.updateFinanceExpenseStatus(11L, ErpAuditStatus.PROCESS.getStatus()));
    }

    @Test
    void updateFinanceExpenseStatus_shouldRollbackDualLedgerVoucherWhenProcess() throws Exception {
        ErpFinanceExpenseServiceImpl service = new ErpFinanceExpenseServiceImpl();
        ErpFinanceExpenseDO expense = new ErpFinanceExpenseDO()
                .setId(11L)
                .setNo("LSBX20260429000001")
                .setStatus(ErpAuditStatus.APPROVE.getStatus());
        AtomicReference<ErpFinanceExpenseDO> updatedExpenseRef = new AtomicReference<>();
        AtomicReference<Integer> rollbackBizTypeRef = new AtomicReference<>();
        AtomicReference<Long> rollbackBizIdRef = new AtomicReference<>();
        AtomicReference<String> rollbackRemarkRef = new AtomicReference<>();

        setField(service, "erpFinanceExpenseMapper", createProxy(ErpFinanceExpenseMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return expense;
            }
            if ("updateByIdAndStatus".equals(methodName)) {
                updatedExpenseRef.set((ErpFinanceExpenseDO) args[2]);
                return 1;
            }
            return null;
        }));
        setField(service, "erpFinanceExpenseItemMapper", createProxy(ErpFinanceExpenseItemMapper.class,
                (methodName, args) -> List.of()));
        setField(service, "apStatementService", createProxy(ErpApStatementService.class, (methodName, args) -> {
            if ("getApStatementByBizTypeAndBizId".equals(methodName)) {
                return new ErpApStatementDO().setId(21L).setBizType(ErpBizTypeEnum.FINANCE_EXPENSE.getType())
                        .setBizId(11L).setPaidAmount(BigDecimal.ZERO);
            }
            return null;
        }));
        setField(service, "financeBizHookService", createProxy(ErpFinanceBizHookService.class, (methodName, args) -> {
            if ("handleRollbackBiz".equals(methodName)) {
                rollbackBizTypeRef.set((Integer) args[0]);
                rollbackBizIdRef.set((Long) args[1]);
                rollbackRemarkRef.set((String) args[3]);
            }
            return null;
        }));

        service.updateFinanceExpenseStatus(11L, ErpAuditStatus.PROCESS.getStatus());

        assertEquals(ErpAuditStatus.PROCESS.getStatus(), updatedExpenseRef.get().getStatus());
        assertEquals(ErpBizTypeEnum.FINANCE_EXPENSE.getType(), rollbackBizTypeRef.get());
        assertEquals(11L, rollbackBizIdRef.get());
        assertEquals("费用单反审核关闭台账", rollbackRemarkRef.get());
    }

    @Test
    void getFinanceExpenseProjectSummary_shouldSplitResearchByRdAccountingType() throws Exception {
        ErpFinanceExpenseServiceImpl service = new ErpFinanceExpenseServiceImpl();
        setField(service, "erpFinanceExpenseMapper", createProxy(ErpFinanceExpenseMapper.class, (methodName, args) -> {
            if ("selectList".equals(methodName)) {
                return List.of(
                        new ErpFinanceExpenseDO().setId(1L).setProjectId(100L).setExpenseType(10)
                                .setRdAccountingType(10)
                                .setExpensePrice(new BigDecimal("100.00"))
                                .setPaidPrice(new BigDecimal("20.00"))
                                .setRemainPrice(new BigDecimal("80.00")),
                        new ErpFinanceExpenseDO().setId(2L).setProjectId(100L).setExpenseType(10)
                                .setRdAccountingType(20)
                                .setExpensePrice(new BigDecimal("300.00"))
                                .setPaidPrice(new BigDecimal("50.00"))
                                .setRemainPrice(new BigDecimal("250.00")),
                        new ErpFinanceExpenseDO().setId(3L).setProjectId(100L).setExpenseType(10)
                                .setRdAccountingType(10)
                                .setExpensePrice(new BigDecimal("40.00"))
                                .setPaidPrice(BigDecimal.ZERO)
                                .setRemainPrice(new BigDecimal("40.00")));
            }
            return Collections.emptyList();
        }));

        List<ErpFinanceExpenseProjectSummaryRespVO> list =
                service.getFinanceExpenseProjectSummary(new ErpFinanceExpenseProjectSummaryReqVO());

        assertEquals(2, list.size());
        ErpFinanceExpenseProjectSummaryRespVO expenseSummary = list.stream()
                .filter(item -> ErpFinanceExpenseAccountingTypeEnum.EXPENSE.getType().equals(item.getRdAccountingType()))
                .findFirst()
                .orElseThrow();
        assertEquals(2L, expenseSummary.getExpenseCount());
        assertEquals(new BigDecimal("140.00"), expenseSummary.getTotalExpensePrice());
        assertEquals(new BigDecimal("20.00"), expenseSummary.getTotalPaidPrice());
        assertEquals(new BigDecimal("120.00"), expenseSummary.getTotalRemainPrice());
        assertEquals("费用化", expenseSummary.getRdAccountingTypeName());

        ErpFinanceExpenseProjectSummaryRespVO capitalizeSummary = list.stream()
                .filter(item -> ErpFinanceExpenseAccountingTypeEnum.CAPITALIZE.getType().equals(item.getRdAccountingType()))
                .findFirst()
                .orElseThrow();
        assertEquals(1L, capitalizeSummary.getExpenseCount());
        assertEquals(new BigDecimal("300.00"), capitalizeSummary.getTotalExpensePrice());
        assertEquals(new BigDecimal("50.00"), capitalizeSummary.getTotalPaidPrice());
        assertEquals(new BigDecimal("250.00"), capitalizeSummary.getTotalRemainPrice());
        assertEquals("资本化", capitalizeSummary.getRdAccountingTypeName());
    }

    private ErpFinanceExpenseSaveReqVO.Item buildItem(Long id, String itemName, String amount, String remark) {
        ErpFinanceExpenseSaveReqVO.Item item = new ErpFinanceExpenseSaveReqVO.Item();
        item.setId(id);
        item.setItemName(itemName);
        item.setAmount(new BigDecimal(amount));
        item.setRemark(remark);
        return item;
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

    private ErpFinanceExpenseTypeService createExpenseTypeService() throws Exception {
        ErpFinanceExpenseTypeService service = new ErpFinanceExpenseTypeService();
        setField(service, "dictDataService", createProxy(DictDataService.class, (methodName, args) -> {
            if ("getDictData".equals(methodName)) {
                return buildExpenseTypeDict((String) args[1]);
            }
            if ("getDictDataList".equals(methodName) || "getDictDataListByDictType".equals(methodName)) {
                return List.of(
                        buildExpenseTypeDict("10"),
                        buildExpenseTypeDict("80"));
            }
            return null;
        }));
        setField(service, "deptApi", createProxy(DeptApi.class, (methodName, args) -> null));
        return service;
    }

    private DictDataDO buildExpenseTypeDict(String value) {
        if ("10".equals(value)) {
            return new DictDataDO()
                    .setDictType(ErpFinanceExpenseTypeService.DICT_TYPE)
                    .setValue("10")
                    .setLabel("研发费用")
                    .setBizAttributes("{\"core\":true,\"projectRequired\":false,\"costCenterRequired\":false,\"leaseContractRequired\":false,\"assetCandidateFlag\":false,\"autoGenerateVoucher\":false}");
        }
        if ("80".equals(value)) {
            return new DictDataDO()
                    .setDictType(ErpFinanceExpenseTypeService.DICT_TYPE)
                    .setValue("80")
                    .setLabel("零星采购")
                    .setBizAttributes("{\"core\":true,\"projectRequired\":false,\"costCenterRequired\":false,\"leaseContractRequired\":false,\"assetCandidateFlag\":false,\"autoGenerateVoucher\":false}");
        }
        return null;
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
