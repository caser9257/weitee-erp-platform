package cn.weitee.erp.module.erp.service.finance;

import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.subject.ErpFinanceSubjectSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceLedgerDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceSubjectDO;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceReportItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceReportItemSubjectMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceSubjectMapper;
import cn.weitee.erp.module.erp.enums.ErpFinanceSubjectTypeEnum;
import cn.weitee.erp.module.erp.enums.ErpFinanceVoucherEntryDirectionEnum;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsFinanceReport.FINANCE_SUBJECT_CODE_DUPLICATE;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsFinanceReport.FINANCE_SUBJECT_DELETE_FAIL_USED_BY_REPORT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ErpFinanceSubjectServiceImplTest {

    @Test
    void createFinanceSubject_shouldRejectDuplicateSubjectCodeInSameLedger() throws Exception {
        ErpFinanceSubjectServiceImpl service = new ErpFinanceSubjectServiceImpl();
        setField(service, "financeLedgerService", createProxy(ErpFinanceLedgerService.class, (methodName, args) ->
                "validateFinanceLedger".equals(methodName)
                        ? new ErpFinanceLedgerDO().setId(1L).setName("标准账簿").setStatus(CommonStatusEnum.ENABLE.getStatus())
                        : null));
        setField(service, "financeSubjectMapper", createProxy(ErpFinanceSubjectMapper.class, (methodName, args) -> {
            if ("selectByLedgerIdAndSubjectCode".equals(methodName)) {
                return new ErpFinanceSubjectDO().setId(9L).setLedgerId(1L).setSubjectCode("1002");
            }
            return null;
        }));
        setField(service, "financeReportItemMapper", createProxy(ErpFinanceReportItemMapper.class, (methodName, args) -> null));
        setField(service, "financeReportItemSubjectMapper", createProxy(ErpFinanceReportItemSubjectMapper.class, (methodName, args) -> 0L));

        ServiceException ex = assertThrows(ServiceException.class, () -> service.createFinanceSubject(buildSubjectSaveReqVO(null)));

        assertEquals(FINANCE_SUBJECT_CODE_DUPLICATE.getCode(), ex.getCode());
    }

    @Test
    void createFinanceSubject_shouldInsertSubjectWhenValid() throws Exception {
        ErpFinanceSubjectServiceImpl service = new ErpFinanceSubjectServiceImpl();
        AtomicReference<ErpFinanceSubjectDO> insertedRef = new AtomicReference<>();
        setField(service, "financeLedgerService", createProxy(ErpFinanceLedgerService.class, (methodName, args) ->
                "validateFinanceLedger".equals(methodName)
                        ? new ErpFinanceLedgerDO().setId(1L).setName("标准账簿").setStatus(CommonStatusEnum.ENABLE.getStatus())
                        : null));
        setField(service, "financeSubjectMapper", createProxy(ErpFinanceSubjectMapper.class, (methodName, args) -> {
            if ("selectByLedgerIdAndSubjectCode".equals(methodName) || "selectById".equals(methodName)) {
                return null;
            }
            if ("insert".equals(methodName)) {
                ErpFinanceSubjectDO subject = (ErpFinanceSubjectDO) args[0];
                subject.setId(11L);
                insertedRef.set(subject);
                return 1;
            }
            return null;
        }));
        setField(service, "financeReportItemMapper", createProxy(ErpFinanceReportItemMapper.class, (methodName, args) -> null));
        setField(service, "financeReportItemSubjectMapper", createProxy(ErpFinanceReportItemSubjectMapper.class, (methodName, args) -> 0L));

        Long id = service.createFinanceSubject(buildSubjectSaveReqVO(null));

        assertEquals(11L, id);
        assertEquals("1002", insertedRef.get().getSubjectCode());
        assertEquals(Boolean.TRUE, insertedRef.get().getLeaf());
    }

    @Test
    void deleteFinanceSubject_shouldRejectWhenUsedByReportItemInSameLedger() throws Exception {
        ErpFinanceSubjectServiceImpl service = new ErpFinanceSubjectServiceImpl();
        setField(service, "financeLedgerService", createProxy(ErpFinanceLedgerService.class, (methodName, args) -> null));
        setField(service, "financeSubjectMapper", createProxy(ErpFinanceSubjectMapper.class, (methodName, args) ->
                "selectById".equals(methodName)
                        ? new ErpFinanceSubjectDO().setId(11L).setLedgerId(1L).setSubjectCode("1002").setSubjectName("银行存款")
                        : null));
        setField(service, "financeReportItemSubjectMapper", createProxy(ErpFinanceReportItemSubjectMapper.class, (methodName, args) ->
                "selectListBySubjectCode".equals(methodName)
                        ? List.of(new cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceReportItemSubjectDO()
                        .setItemId(20L).setSubjectCode("1002"))
                        : 0L));
        setField(service, "financeReportItemMapper", createProxy(ErpFinanceReportItemMapper.class, (methodName, args) ->
                "selectByIds".equals(methodName)
                        ? List.of(new cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceReportItemDO().setId(20L).setLedgerId(1L))
                        : null));

        ServiceException ex = assertThrows(ServiceException.class, () -> service.deleteFinanceSubject(11L));

        assertEquals(FINANCE_SUBJECT_DELETE_FAIL_USED_BY_REPORT.getCode(), ex.getCode());
    }

    private ErpFinanceSubjectSaveReqVO buildSubjectSaveReqVO(Long id) {
        ErpFinanceSubjectSaveReqVO reqVO = new ErpFinanceSubjectSaveReqVO();
        reqVO.setId(id);
        reqVO.setLedgerId(1L);
        reqVO.setSubjectCode("1002");
        reqVO.setSubjectName("银行存款");
        reqVO.setSubjectType(ErpFinanceSubjectTypeEnum.ASSET.getType());
        reqVO.setBalanceDirection(ErpFinanceVoucherEntryDirectionEnum.DEBIT.getType());
        reqVO.setLeaf(true);
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        reqVO.setSort(10);
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
        Field field = target.getClass().getDeclaredField(mapFieldName(fieldName));
        field.setAccessible(true);
        field.set(target, value);
    }

    private String mapFieldName(String fieldName) {
        return switch (fieldName) {
            case "financeSubjectMapper" -> "erpFinanceSubjectMapper";
            case "financeReportItemMapper" -> "erpFinanceReportItemMapper";
            case "financeReportItemSubjectMapper" -> "erpFinanceReportItemSubjectMapper";
            default -> fieldName;
        };
    }

    @FunctionalInterface
    private interface MethodHandler {
        Object handle(String methodName, Object[] args);
    }
}
