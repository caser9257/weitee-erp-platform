package cn.weitee.erp.module.erp.service.finance;

import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.reportitem.ErpFinanceReportItemSaveReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.reportitem.ErpFinanceReportTemplateInitReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.reportitem.ErpFinanceReportTemplateInitRespVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceLedgerDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceReportItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceReportItemSubjectDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceSubjectDO;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceReportItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceReportItemSubjectMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceSubjectMapper;
import cn.weitee.erp.module.erp.enums.ErpFinanceReportAmountRuleEnum;
import cn.weitee.erp.module.erp.enums.ErpFinanceReportItemCategoryEnum;
import cn.weitee.erp.module.erp.enums.ErpFinanceReportTypeEnum;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsFinanceReport.FINANCE_REPORT_ITEM_SUBJECT_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ErpFinanceReportItemServiceImplTest {

    @Test
    void createFinanceReportItem_shouldRejectMissingMappedSubject() throws Exception {
        ErpFinanceReportItemServiceImpl service = new ErpFinanceReportItemServiceImpl();
        setField(service, "financeLedgerService", createProxy(ErpFinanceLedgerService.class, (methodName, args) ->
                "validateFinanceLedger".equals(methodName)
                        ? new ErpFinanceLedgerDO().setId(1L).setName("标准账簿").setStatus(CommonStatusEnum.ENABLE.getStatus())
                        : null));
        setField(service, "financeReportItemMapper", createProxy(ErpFinanceReportItemMapper.class, (methodName, args) ->
                "selectByLedgerIdAndReportTypeAndItemCode".equals(methodName) ? null : null));
        setField(service, "financeReportItemSubjectMapper", createProxy(ErpFinanceReportItemSubjectMapper.class, (methodName, args) -> null));
        setField(service, "financeSubjectMapper", createProxy(ErpFinanceSubjectMapper.class, (methodName, args) ->
                "selectListByLedgerIdAndSubjectCodes".equals(methodName) ? List.of() : null));

        ServiceException ex = assertThrows(ServiceException.class, () -> service.createFinanceReportItem(buildSaveReqVO(null)));

        assertEquals(FINANCE_REPORT_ITEM_SUBJECT_NOT_EXISTS.getCode(), ex.getCode());
    }

    @Test
    void createFinanceReportItem_shouldInsertHeaderAndSubjectMappings() throws Exception {
        ErpFinanceReportItemServiceImpl service = new ErpFinanceReportItemServiceImpl();
        AtomicReference<ErpFinanceReportItemDO> insertedItemRef = new AtomicReference<>();
        AtomicReference<List<ErpFinanceReportItemSubjectDO>> insertedSubjectsRef = new AtomicReference<>();
        setField(service, "financeLedgerService", createProxy(ErpFinanceLedgerService.class, (methodName, args) ->
                "validateFinanceLedger".equals(methodName)
                        ? new ErpFinanceLedgerDO().setId(1L).setName("标准账簿").setStatus(CommonStatusEnum.ENABLE.getStatus())
                        : null));
        setField(service, "financeReportItemMapper", createProxy(ErpFinanceReportItemMapper.class, (methodName, args) -> {
            if ("selectByLedgerIdAndReportTypeAndItemCode".equals(methodName)) {
                return null;
            }
            if ("insert".equals(methodName)) {
                ErpFinanceReportItemDO item = (ErpFinanceReportItemDO) args[0];
                item.setId(20L);
                insertedItemRef.set(item);
                return 1;
            }
            return null;
        }));
        setField(service, "financeReportItemSubjectMapper", createProxy(ErpFinanceReportItemSubjectMapper.class, (methodName, args) -> {
            if ("insertBatch".equals(methodName)) {
                insertedSubjectsRef.set((List<ErpFinanceReportItemSubjectDO>) args[0]);
                return true;
            }
            return null;
        }));
        setField(service, "financeSubjectMapper", createProxy(ErpFinanceSubjectMapper.class, (methodName, args) ->
                "selectListByLedgerIdAndSubjectCodes".equals(methodName)
                        ? List.of(new ErpFinanceSubjectDO().setId(1L).setLedgerId(1L).setSubjectCode("1002")
                        .setSubjectName("银行存款").setStatus(CommonStatusEnum.ENABLE.getStatus()))
                        : null));

        Long id = service.createFinanceReportItem(buildSaveReqVO(null));

        assertEquals(20L, id);
        assertEquals("BS-ASSET", insertedItemRef.get().getItemCode());
        assertEquals(1, insertedSubjectsRef.get().size());
        assertEquals(20L, insertedSubjectsRef.get().get(0).getItemId());
        assertEquals("1002", insertedSubjectsRef.get().get(0).getSubjectCode());
    }

    @Test
    void initStandardTemplate_shouldCreateMissingSubjectsAndReportItems() throws Exception {
        ErpFinanceReportItemServiceImpl service = new ErpFinanceReportItemServiceImpl();
        AtomicReference<List<ErpFinanceReportItemSubjectDO>> insertedSubjectsRef = new AtomicReference<>();
        java.util.concurrent.atomic.AtomicInteger insertedSubjectCount = new java.util.concurrent.atomic.AtomicInteger();
        java.util.concurrent.atomic.AtomicInteger insertedItemCount = new java.util.concurrent.atomic.AtomicInteger();
        setField(service, "financeLedgerService", createProxy(ErpFinanceLedgerService.class, (methodName, args) ->
                "validateFinanceLedger".equals(methodName)
                        ? new ErpFinanceLedgerDO().setId(1L).setName("标准账簿").setStatus(CommonStatusEnum.ENABLE.getStatus())
                        : null));
        setField(service, "financeSubjectMapper", createProxy(ErpFinanceSubjectMapper.class, (methodName, args) -> {
            if ("selectListByLedgerIdAndSubjectCodes".equals(methodName)) {
                return List.of();
            }
            if ("insert".equals(methodName)) {
                insertedSubjectCount.incrementAndGet();
                return 1;
            }
            return null;
        }));
        setField(service, "financeReportItemMapper", createProxy(ErpFinanceReportItemMapper.class, (methodName, args) -> {
            if ("selectByLedgerIdAndReportTypeAndItemCode".equals(methodName)) {
                return null;
            }
            if ("insert".equals(methodName)) {
                ErpFinanceReportItemDO item = (ErpFinanceReportItemDO) args[0];
                item.setId((long) insertedItemCount.incrementAndGet());
                return 1;
            }
            return null;
        }));
        setField(service, "financeReportItemSubjectMapper", createProxy(ErpFinanceReportItemSubjectMapper.class, (methodName, args) -> {
            if ("insertBatch".equals(methodName)) {
                insertedSubjectsRef.set((List<ErpFinanceReportItemSubjectDO>) args[0]);
                return true;
            }
            return null;
        }));

        ErpFinanceReportTemplateInitReqVO reqVO = new ErpFinanceReportTemplateInitReqVO();
        reqVO.setLedgerId(1L);
        ErpFinanceReportTemplateInitRespVO respVO = service.initStandardTemplate(reqVO);

        assertEquals(18, respVO.getCreatedSubjectCount());
        assertEquals(16, respVO.getCreatedItemCount());
        assertEquals(true, insertedSubjectCount.get() > 0);
        assertEquals(true, insertedSubjectsRef.get().size() > 0);
    }

    private ErpFinanceReportItemSaveReqVO buildSaveReqVO(Long id) {
        ErpFinanceReportItemSaveReqVO reqVO = new ErpFinanceReportItemSaveReqVO();
        reqVO.setId(id);
        reqVO.setLedgerId(1L);
        reqVO.setReportType(ErpFinanceReportTypeEnum.BALANCE_SHEET.getType());
        reqVO.setItemCategory(ErpFinanceReportItemCategoryEnum.ASSET.getType());
        reqVO.setItemCode("BS-ASSET");
        reqVO.setItemName("资产合计");
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        reqVO.setSort(10);
        ErpFinanceReportItemSaveReqVO.SubjectMapping mapping = new ErpFinanceReportItemSaveReqVO.SubjectMapping();
        mapping.setSubjectCode("1002");
        mapping.setAmountRule(ErpFinanceReportAmountRuleEnum.ENDING_DEBIT.getType());
        mapping.setAmountSign(1);
        reqVO.setSubjects(List.of(mapping));
        return reqVO;
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
