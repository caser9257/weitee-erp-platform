package cn.iocoder.yudao.module.erp.service.finance;

import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceAssetCandidateDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceExpenseDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceExpenseItemDO;
import cn.iocoder.yudao.module.erp.enums.ErpAuditStatus;
import cn.iocoder.yudao.module.erp.enums.ErpFinanceExpenseAccountingTypeEnum;
import cn.iocoder.yudao.module.erp.enums.ErpFinanceExpenseTypeEnum;
import cn.iocoder.yudao.module.erp.enums.ErpFinanceAssetCandidateStatusEnum;
import cn.iocoder.yudao.module.erp.enums.ErpFinanceAssetSourceTypeEnum;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ErpFinanceAssetCandidateServiceImplTest {

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
        setField(service, "financeAssetCandidateMapper", createProxy(cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinanceAssetCandidateMapper.class, (methodName, args) -> {
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
        setField(service, "purchaseInService", createProxy(cn.iocoder.yudao.module.erp.service.purchase.ErpPurchaseInService.class, (methodName, args) -> null));
        setField(service, "productService", createProxy(cn.iocoder.yudao.module.erp.service.product.ErpProductService.class, (methodName, args) -> java.util.Collections.emptyMap()));

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

    @FunctionalInterface
    private interface MethodHandler {
        Object handle(String methodName, Object[] args) throws Exception;
    }
}
