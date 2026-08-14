package cn.weitee.erp.module.erp.service.finance;

import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.asset.ErpFinanceAssetCandidatePageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceAssetCandidateDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceAssetDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceExpenseDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceExpenseItemDO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.asset.ErpFinanceAssetCandidateConfirmReqVO;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.enums.ErpFinanceExpenseAccountingTypeEnum;
import cn.weitee.erp.module.erp.enums.ErpFinanceExpenseTypeEnum;
import cn.weitee.erp.module.erp.enums.ErpFinanceAssetCandidateStatusEnum;
import cn.weitee.erp.module.erp.enums.ErpFinanceAssetSourceTypeEnum;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsFinanceAsset.ASSET_CANDIDATE_CONFIRM_FAIL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ErpFinanceAssetCandidateServiceImplTest {

    @Test
    void getFinanceAssetCandidatePage_whenDeptScopeLimited_shouldUseDeptScopedMapper() throws Exception {
        ErpFinanceAssetCandidateServiceImpl service = new ErpFinanceAssetCandidateServiceImpl();
        AtomicReference<Collection<Long>> actualDeptIds = new AtomicReference<>();
        setField(service, "financeDataPermissionService", createProxy(FinanceDataPermissionService.class, (methodName, args) -> {
            if ("getPermissionScope".equals(methodName)) {
                return new cn.weitee.erp.module.erp.service.finance.interceptor.FinancePermissionScope(
                        cn.weitee.erp.module.erp.service.finance.interceptor.FinancePermissionScope.Scope.all(),
                        cn.weitee.erp.module.erp.service.finance.interceptor.FinancePermissionScope.Scope.limited(Set.of(9L)),
                        java.util.Collections.emptyMap(), false, false);
            }
            return null;
        }));
        setField(service, "financeAssetCandidateMapper", createProxy(cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceAssetCandidateMapper.class, (methodName, args) -> {
            if ("selectPageByDeptIds".equals(methodName)) {
                actualDeptIds.set((Collection<Long>) args[1]);
                return new PageResult<>(List.of(new ErpFinanceAssetCandidateDO().setId(109L).setDeptId(9L)), 1L);
            }
            if ("selectPage".equals(methodName)) {
                throw new AssertionError("受限部门不得调用全量候选资产分页查询");
            }
            return null;
        }));

        PageResult<ErpFinanceAssetCandidateDO> page = service.getFinanceAssetCandidatePage(new ErpFinanceAssetCandidatePageReqVO());

        assertEquals(Set.of(9L), Set.copyOf(actualDeptIds.get()));
        assertEquals(List.of(109L), page.getList().stream().map(ErpFinanceAssetCandidateDO::getId).toList());
    }

    @Test
    void confirmFinanceAssetCandidate_whenCandidateAlreadyLinkedAsset_shouldFailWithoutCreatingAsset() throws Exception {
        ErpFinanceAssetCandidateServiceImpl service = new ErpFinanceAssetCandidateServiceImpl();
        AtomicInteger createAssetCount = new AtomicInteger();
        AtomicInteger updateCandidateCount = new AtomicInteger();

        setField(service, "financeAssetCandidateMapper", createProxy(cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceAssetCandidateMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return new ErpFinanceAssetCandidateDO()
                        .setId(108901L)
                        .setSourceType(ErpFinanceAssetSourceTypeEnum.FINANCE_EXPENSE.getType())
                        .setSourceBizId(981704L)
                        .setSourceBizNo("FYBX-981704")
                        .setSourceItemId(0L)
                        .setStatus(ErpFinanceAssetCandidateStatusEnum.PENDING_CONFIRM.getStatus());
            }
            if ("updateById".equals(methodName)) {
                updateCandidateCount.incrementAndGet();
                return 1;
            }
            return null;
        }));
        setField(service, "financeAssetMapper", createProxy(cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceAssetMapper.class, (methodName, args) -> {
            if ("selectByCandidateId".equals(methodName)) {
                return new ErpFinanceAssetDO()
                        .setId(108911L)
                        .setCandidateId(108901L);
            }
            return null;
        }));
        setField(service, "financeAssetService", createProxy(ErpFinanceAssetService.class, (methodName, args) -> {
            if ("createFinanceAsset".equals(methodName)) {
                createAssetCount.incrementAndGet();
                return 108913L;
            }
            return null;
        }));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.confirmFinanceAssetCandidate(buildConfirmReqVO(108901L)));

        assertEquals(ASSET_CANDIDATE_CONFIRM_FAIL.getCode(), ex.getCode());
        assertEquals(0, createAssetCount.get());
        assertEquals(0, updateCandidateCount.get());
    }

    @Test
    void confirmFinanceAssetCandidate_whenCandidateStatusChangedConcurrently_shouldFailWithoutCreatingAsset() throws Exception {
        ErpFinanceAssetCandidateServiceImpl service = new ErpFinanceAssetCandidateServiceImpl();
        AtomicInteger createAssetCount = new AtomicInteger();

        setField(service, "financeAssetCandidateMapper", createProxy(cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceAssetCandidateMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return new ErpFinanceAssetCandidateDO()
                        .setId(108902L)
                        .setSourceType(ErpFinanceAssetSourceTypeEnum.FINANCE_EXPENSE.getType())
                        .setSourceBizId(981704L)
                        .setSourceBizNo("FYBX-981704")
                        .setSourceItemId(0L)
                        .setStatus(ErpFinanceAssetCandidateStatusEnum.PENDING_CONFIRM.getStatus());
            }
            if ("updateByIdAndStatus".equals(methodName)) {
                return 0;
            }
            return null;
        }));
        setField(service, "financeAssetMapper", createProxy(cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceAssetMapper.class, (methodName, args) -> null));
        setField(service, "financeAssetService", createProxy(ErpFinanceAssetService.class, (methodName, args) -> {
            if ("createFinanceAsset".equals(methodName)) {
                createAssetCount.incrementAndGet();
                return 108914L;
            }
            return null;
        }));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.confirmFinanceAssetCandidate(buildConfirmReqVO(108902L)));

        assertEquals(ASSET_CANDIDATE_CONFIRM_FAIL.getCode(), ex.getCode());
        assertEquals(0, createAssetCount.get());
    }

    @Test
    void createCandidateFromExpense_shouldSupportResearchCapitalizeExpense() throws Exception {
        ErpFinanceAssetCandidateServiceImpl service = new ErpFinanceAssetCandidateServiceImpl();
        AtomicReference<ErpFinanceAssetCandidateDO> insertedCandidateRef = new AtomicReference<>();

        setField(service, "financeExpenseService", createProxy(ErpFinanceExpenseService.class, (methodName, args) -> {
            if ("getFinanceExpense".equals(methodName)) {
                return new ErpFinanceExpenseDO()
                        .setId(100L)
                        .setNo("FYBX-100")
                        .setStatus(ErpAuditStatus.APPROVE.getStatus())
                        .setExpenseType(ErpFinanceExpenseTypeEnum.RESEARCH.getType())
                        .setRdAccountingType(ErpFinanceExpenseAccountingTypeEnum.CAPITALIZE.getType())
                        .setDeptId(9L)
                        .setFinanceUserId(7L)
                        .setExpenseTime(LocalDateTime.of(2026, 5, 20, 10, 0))
                        .setExpensePrice(new BigDecimal("300.00"))
                        .setRemark("研发资本化");
            }
            if ("getFinanceExpenseItemListByExpenseId".equals(methodName)) {
                return List.of(new ErpFinanceExpenseItemDO()
                        .setId(1001L)
                        .setItemName("研发测试设备")
                        .setAmount(new BigDecimal("300.00"))
                        .setRemark("资产候选")
                        .setAssetCandidateFlag(Boolean.TRUE));
            }
            return null;
        }));
        setField(service, "financeAssetCandidateMapper", createProxy(cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceAssetCandidateMapper.class, (methodName, args) -> {
            if ("selectBySource".equals(methodName)) {
                return null;
            }
            if ("insert".equals(methodName)) {
                ErpFinanceAssetCandidateDO candidate = (ErpFinanceAssetCandidateDO) args[0];
                candidate.setId(501L);
                insertedCandidateRef.set(candidate);
                return 1;
            }
            return null;
        }));
        setField(service, "financeAssetService", createProxy(ErpFinanceAssetService.class, (methodName, args) -> null));
        setField(service, "purchaseInService", createProxy(cn.weitee.erp.module.erp.service.purchase.ErpPurchaseInService.class, (methodName, args) -> null));
        setField(service, "productService", createProxy(cn.weitee.erp.module.erp.service.product.ErpProductService.class, (methodName, args) -> java.util.Collections.emptyMap()));

        Long id = service.createCandidateFromExpense(100L);

        assertEquals(501L, id);
        assertEquals(ErpFinanceAssetSourceTypeEnum.FINANCE_EXPENSE.getType(), insertedCandidateRef.get().getSourceType());
        assertEquals("研发测试设备", insertedCandidateRef.get().getAssetName());
        assertEquals("研发资本化", insertedCandidateRef.get().getCategoryName());
        assertEquals(ErpFinanceAssetCandidateStatusEnum.PENDING_CONFIRM.getStatus(), insertedCandidateRef.get().getStatus());
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

    private ErpFinanceAssetCandidateConfirmReqVO buildConfirmReqVO(Long candidateId) {
        ErpFinanceAssetCandidateConfirmReqVO reqVO = new ErpFinanceAssetCandidateConfirmReqVO();
        reqVO.setCandidateId(candidateId);
        reqVO.setName("研发测试设备");
        reqVO.setCategoryName("研发设备");
        reqVO.setPurchaseDate(LocalDate.of(2026, 5, 1));
        reqVO.setStartUseDate(LocalDate.of(2026, 5, 1));
        reqVO.setOriginalAmount(new BigDecimal("600.00"));
        reqVO.setSalvageRate(BigDecimal.ZERO);
        reqVO.setDepreciationMethod("STRAIGHT_LINE");
        reqVO.setDepreciationPeriodMonths(36);
        reqVO.setDepreciationStartPeriod("2026-05");
        reqVO.setRemark("重复确认拦截");
        return reqVO;
    }

    @FunctionalInterface
    private interface MethodHandler {
        Object handle(String methodName, Object[] args) throws Exception;
    }
}
