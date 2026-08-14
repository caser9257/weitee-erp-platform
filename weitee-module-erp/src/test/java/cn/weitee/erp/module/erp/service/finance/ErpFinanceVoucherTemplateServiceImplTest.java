package cn.weitee.erp.module.erp.service.finance;

import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.voucher.ErpFinanceVoucherTemplatePageReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.voucher.ErpFinanceVoucherTemplateSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceLedgerDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceSubjectDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceVoucherTemplateDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceVoucherTemplateItemDO;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceVoucherTemplateItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceVoucherTemplateMapper;
import cn.weitee.erp.module.erp.enums.ErpFinanceVoucherAmountSourceEnum;
import cn.weitee.erp.module.erp.enums.ErpFinanceVoucherEntryDirectionEnum;
import cn.weitee.erp.module.erp.enums.common.ErpBizTypeEnum;
import cn.weitee.erp.module.erp.service.finance.interceptor.FinancePermissionScope;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsFinanceReport.FINANCE_SUBJECT_NOT_ENABLE;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsFinanceVoucher.FINANCE_VOUCHER_TEMPLATE_AMOUNT_SOURCE_VALUE_REQUIRED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ErpFinanceVoucherTemplateServiceImplTest {

    @Test
    void getVoucherTemplatePage_shouldExcludeTemplateContainingUnauthorizedSubject() throws Exception {
        ErpFinanceVoucherTemplateServiceImpl service = new ErpFinanceVoucherTemplateServiceImpl();
        AtomicBoolean scopedQueryCalled = new AtomicBoolean(false);
        PageResult<ErpFinanceVoucherTemplateDO> expected = PageResult.empty(0L);
        setField(service, "financeDataPermissionService", createProxy(FinanceDataPermissionService.class, (methodName, args) -> {
            if ("getVisibleLedgerIds".equals(methodName)) {
                return List.of(1L);
            }
            if ("getPermissionScope".equals(methodName)) {
                return new FinancePermissionScope(FinancePermissionScope.Scope.limited(Set.of(1L)),
                        FinancePermissionScope.Scope.all(),
                        Map.of(1L, FinancePermissionScope.Scope.limited(Set.of("1001"))), false, false);
            }
            return null;
        }));
        setField(service, "voucherTemplateMapper", createProxy(ErpFinanceVoucherTemplateMapper.class, (methodName, args) -> {
            if ("selectPageByVisibleLedgerIdsAndSubjectCodes".equals(methodName)) {
                scopedQueryCalled.set(true);
                return expected;
            }
            return null;
        }));

        PageResult<ErpFinanceVoucherTemplateDO> result = service.getVoucherTemplatePage(
                new ErpFinanceVoucherTemplatePageReqVO());

        assertEquals(expected, result);
        assertEquals(true, scopedQueryCalled.get());
    }

    @Test
    void createVoucherTemplate_shouldInsertHeadAndItems() throws Exception {
        ErpFinanceVoucherTemplateServiceImpl service = new ErpFinanceVoucherTemplateServiceImpl();
        AtomicReference<ErpFinanceVoucherTemplateDO> insertedTemplateRef = new AtomicReference<>();
        AtomicReference<List<ErpFinanceVoucherTemplateItemDO>> insertedItemsRef = new AtomicReference<>();

        setField(service, "financeLedgerService", createProxy(ErpFinanceLedgerService.class, (methodName, args) -> {
            if ("validateFinanceLedger".equals(methodName)) {
                return new ErpFinanceLedgerDO().setId(1L).setName("标准账簿").setStatus(CommonStatusEnum.ENABLE.getStatus());
            }
            return null;
        }));
        setField(service, "financeSubjectService", createProxy(ErpFinanceSubjectService.class, (methodName, args) -> {
            if ("getFinanceSubjectListByLedgerIdAndSubjectCodes".equals(methodName)) {
                return List.of(
                        new ErpFinanceSubjectDO().setId(1L).setLedgerId(1L).setSubjectCode("660201")
                                .setSubjectName("管理费用-研发费").setLeaf(true).setStatus(CommonStatusEnum.ENABLE.getStatus()),
                        new ErpFinanceSubjectDO().setId(2L).setLedgerId(1L).setSubjectCode("220201")
                                .setSubjectName("其他应付款").setLeaf(true).setStatus(CommonStatusEnum.ENABLE.getStatus()));
            }
            return null;
        }));
        setField(service, "erpFinanceVoucherTemplateMapper", createProxy(ErpFinanceVoucherTemplateMapper.class, (methodName, args) -> {
            if ("insert".equals(methodName)) {
                ErpFinanceVoucherTemplateDO template = (ErpFinanceVoucherTemplateDO) args[0];
                template.setId(10L);
                insertedTemplateRef.set(template);
                return 1;
            }
            return null;
        }));
        setField(service, "erpFinanceVoucherTemplateItemMapper", createProxy(ErpFinanceVoucherTemplateItemMapper.class, (methodName, args) -> {
            if ("insertBatch".equals(methodName)) {
                insertedItemsRef.set((List<ErpFinanceVoucherTemplateItemDO>) args[0]);
                return true;
            }
            return null;
        }));

        ErpFinanceVoucherTemplateSaveReqVO reqVO = new ErpFinanceVoucherTemplateSaveReqVO();
        reqVO.setLedgerId(1L);
        reqVO.setBizType(ErpBizTypeEnum.FINANCE_EXPENSE.getType());
        reqVO.setName("费用报销模板");
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        reqVO.setAutoGenerate(false);
        reqVO.setItems(List.of(buildItem(ErpFinanceVoucherEntryDirectionEnum.DEBIT.getType(), "660201", "管理费用-研发费"),
                buildItem(ErpFinanceVoucherEntryDirectionEnum.CREDIT.getType(), "220201", "其他应付款")));

        Long id = service.createVoucherTemplate(reqVO);

        assertEquals(10L, id);
        assertEquals("费用报销模板", insertedTemplateRef.get().getName());
        assertEquals(2, insertedItemsRef.get().size());
        assertEquals(10L, insertedItemsRef.get().get(0).getTemplateId());
    }

    @Test
    void createVoucherTemplate_shouldSupportResearchAccountingBizTypes() throws Exception {
        ErpFinanceVoucherTemplateServiceImpl service = new ErpFinanceVoucherTemplateServiceImpl();
        AtomicReference<ErpFinanceVoucherTemplateDO> insertedTemplateRef = new AtomicReference<>();

        setField(service, "financeLedgerService", createProxy(ErpFinanceLedgerService.class, (methodName, args) -> {
            if ("validateFinanceLedger".equals(methodName)) {
                return new ErpFinanceLedgerDO().setId(1L).setName("标准账簿").setStatus(CommonStatusEnum.ENABLE.getStatus());
            }
            return null;
        }));
        setField(service, "financeSubjectService", createProxy(ErpFinanceSubjectService.class, (methodName, args) -> {
            if ("getFinanceSubjectListByLedgerIdAndSubjectCodes".equals(methodName)) {
                return List.of(
                        new ErpFinanceSubjectDO().setId(1L).setLedgerId(1L).setSubjectCode("660201")
                                .setSubjectName("管理费用-研发费").setLeaf(true).setStatus(CommonStatusEnum.ENABLE.getStatus()),
                        new ErpFinanceSubjectDO().setId(2L).setLedgerId(1L).setSubjectCode("220201")
                                .setSubjectName("其他应付款").setLeaf(true).setStatus(CommonStatusEnum.ENABLE.getStatus()));
            }
            return null;
        }));
        setField(service, "voucherTemplateMapper", createProxy(ErpFinanceVoucherTemplateMapper.class, (methodName, args) -> {
            if ("insert".equals(methodName)) {
                ErpFinanceVoucherTemplateDO template = (ErpFinanceVoucherTemplateDO) args[0];
                template.setId(11L);
                insertedTemplateRef.set(template);
                return 1;
            }
            return null;
        }));
        setField(service, "voucherTemplateItemMapper", createProxy(ErpFinanceVoucherTemplateItemMapper.class, (methodName, args) -> true));

        ErpFinanceVoucherTemplateSaveReqVO reqVO = buildTemplateSaveReqVO(List.of(
                buildItem(ErpFinanceVoucherEntryDirectionEnum.DEBIT.getType(), "660201", "管理费用-研发费"),
                buildItem(ErpFinanceVoucherEntryDirectionEnum.CREDIT.getType(), "220201", "其他应付款")));
        reqVO.setBizType(ErpBizTypeEnum.FINANCE_EXPENSE_CAPITALIZE.getType());
        reqVO.setName("研发资本化模板");

        Long id = service.createVoucherTemplate(reqVO);

        assertEquals(11L, id);
        assertEquals(ErpBizTypeEnum.FINANCE_EXPENSE_CAPITALIZE.getType(), insertedTemplateRef.get().getBizType());
    }

    @Test
    void createVoucherTemplate_shouldRequireAmountSourceValueWhenNeeded() throws Exception {
        ErpFinanceVoucherTemplateServiceImpl service = new ErpFinanceVoucherTemplateServiceImpl();
        setField(service, "financeLedgerService", createProxy(ErpFinanceLedgerService.class, (methodName, args) ->
                "validateFinanceLedger".equals(methodName)
                        ? new ErpFinanceLedgerDO().setId(1L).setName("标准账簿").setStatus(CommonStatusEnum.ENABLE.getStatus())
                        : null));
        setField(service, "financeSubjectService", createProxy(ErpFinanceSubjectService.class, (methodName, args) -> {
            if ("getFinanceSubjectListByLedgerIdAndSubjectCodes".equals(methodName)) {
                return List.of(new ErpFinanceSubjectDO().setId(1L).setLedgerId(1L).setSubjectCode("660201")
                        .setSubjectName("管理费用-研发费").setLeaf(true).setStatus(CommonStatusEnum.ENABLE.getStatus()));
            }
            return null;
        }));
        setField(service, "erpFinanceVoucherTemplateMapper", createProxy(ErpFinanceVoucherTemplateMapper.class, (methodName, args) -> null));
        setField(service, "erpFinanceVoucherTemplateItemMapper", createProxy(ErpFinanceVoucherTemplateItemMapper.class, (methodName, args) -> null));

        ErpFinanceVoucherTemplateSaveReqVO reqVO = buildTemplateSaveReqVO(List.of(
                buildItem(ErpFinanceVoucherEntryDirectionEnum.DEBIT.getType(), "660201", "管理费用-研发费")));
        reqVO.getItems().get(0).setAmountSource(ErpFinanceVoucherAmountSourceEnum.FIXED_AMOUNT.getType());

        ServiceException ex = assertThrows(ServiceException.class, () -> service.createVoucherTemplate(reqVO));
        assertEquals(FINANCE_VOUCHER_TEMPLATE_AMOUNT_SOURCE_VALUE_REQUIRED.getCode(), ex.getCode());
    }

    @Test
    void createVoucherTemplate_shouldKeepAmountSourceValue() throws Exception {
        ErpFinanceVoucherTemplateServiceImpl service = new ErpFinanceVoucherTemplateServiceImpl();
        AtomicReference<List<ErpFinanceVoucherTemplateItemDO>> insertedItemsRef = new AtomicReference<>();
        setField(service, "financeLedgerService", createProxy(ErpFinanceLedgerService.class, (methodName, args) -> {
            if ("validateFinanceLedger".equals(methodName)) {
                return new ErpFinanceLedgerDO().setId(1L).setName("标准账簿").setStatus(CommonStatusEnum.ENABLE.getStatus());
            }
            return null;
        }));
        setField(service, "financeSubjectService", createProxy(ErpFinanceSubjectService.class, (methodName, args) -> {
            if ("getFinanceSubjectListByLedgerIdAndSubjectCodes".equals(methodName)) {
                return List.of(new ErpFinanceSubjectDO().setId(1L).setLedgerId(1L).setSubjectCode("660201")
                        .setSubjectName("管理费用-研发费").setLeaf(true).setStatus(CommonStatusEnum.ENABLE.getStatus()));
            }
            return null;
        }));
        setField(service, "erpFinanceVoucherTemplateMapper", createProxy(ErpFinanceVoucherTemplateMapper.class, (methodName, args) -> {
            if ("insert".equals(methodName)) {
                ((ErpFinanceVoucherTemplateDO) args[0]).setId(10L);
                return 1;
            }
            return null;
        }));
        setField(service, "erpFinanceVoucherTemplateItemMapper", createProxy(ErpFinanceVoucherTemplateItemMapper.class, (methodName, args) -> {
            if ("insertBatch".equals(methodName)) {
                insertedItemsRef.set((List<ErpFinanceVoucherTemplateItemDO>) args[0]);
                return true;
            }
            return null;
        }));

        ErpFinanceVoucherTemplateSaveReqVO reqVO = buildTemplateSaveReqVO(List.of(
                buildItem(ErpFinanceVoucherEntryDirectionEnum.DEBIT.getType(), "660201", "管理费用-研发费")));
        reqVO.getItems().get(0).setAmountSource(ErpFinanceVoucherAmountSourceEnum.FIXED_AMOUNT.getType());
        reqVO.getItems().get(0).setAmountSourceValue(new BigDecimal("120.00"));

        service.createVoucherTemplate(reqVO);

        assertEquals(new BigDecimal("120.00"), insertedItemsRef.get().get(0).getAmountSourceValue());
    }

    @Test
    void createVoucherTemplate_shouldFillSubjectNameFromEnabledLeafSubject() throws Exception {
        ErpFinanceVoucherTemplateServiceImpl service = new ErpFinanceVoucherTemplateServiceImpl();
        AtomicReference<List<ErpFinanceVoucherTemplateItemDO>> insertedItemsRef = new AtomicReference<>();
        setField(service, "financeLedgerService", createProxy(ErpFinanceLedgerService.class, (methodName, args) -> {
            if ("validateFinanceLedger".equals(methodName)) {
                return new ErpFinanceLedgerDO().setId(1L).setName("标准账簿").setStatus(CommonStatusEnum.ENABLE.getStatus());
            }
            return null;
        }));
        setField(service, "financeSubjectService", createProxy(ErpFinanceSubjectService.class, (methodName, args) -> {
            if ("getFinanceSubjectListByLedgerIdAndSubjectCodes".equals(methodName)) {
                return List.of(new ErpFinanceSubjectDO().setId(1L).setLedgerId(1L).setSubjectCode("660201")
                        .setSubjectName("管理费用-研发费").setLeaf(true).setStatus(CommonStatusEnum.ENABLE.getStatus()));
            }
            return null;
        }));
        setField(service, "erpFinanceVoucherTemplateMapper", createProxy(ErpFinanceVoucherTemplateMapper.class, (methodName, args) -> {
            if ("insert".equals(methodName)) {
                ((ErpFinanceVoucherTemplateDO) args[0]).setId(10L);
                return 1;
            }
            return null;
        }));
        setField(service, "erpFinanceVoucherTemplateItemMapper", createProxy(ErpFinanceVoucherTemplateItemMapper.class, (methodName, args) -> {
            if ("insertBatch".equals(methodName)) {
                insertedItemsRef.set((List<ErpFinanceVoucherTemplateItemDO>) args[0]);
                return true;
            }
            return null;
        }));

        ErpFinanceVoucherTemplateSaveReqVO reqVO = buildTemplateSaveReqVO(List.of(
                buildItem(ErpFinanceVoucherEntryDirectionEnum.DEBIT.getType(), "660201", "手工错误名称")));

        service.createVoucherTemplate(reqVO);

        assertEquals("管理费用-研发费", reqVO.getItems().get(0).getSubjectName());
        assertEquals("管理费用-研发费", insertedItemsRef.get().get(0).getSubjectName());
    }

    @Test
    void createVoucherTemplate_shouldRejectDisabledSubject() throws Exception {
        ErpFinanceVoucherTemplateServiceImpl service = new ErpFinanceVoucherTemplateServiceImpl();
        setField(service, "financeLedgerService", createProxy(ErpFinanceLedgerService.class, (methodName, args) ->
                "validateFinanceLedger".equals(methodName)
                        ? new ErpFinanceLedgerDO().setId(1L).setName("标准账簿").setStatus(CommonStatusEnum.ENABLE.getStatus())
                        : null));
        setField(service, "financeSubjectService", createProxy(ErpFinanceSubjectService.class, (methodName, args) -> {
            if ("getFinanceSubjectListByLedgerIdAndSubjectCodes".equals(methodName)) {
                return List.of(new ErpFinanceSubjectDO().setId(1L).setLedgerId(1L).setSubjectCode("660201")
                        .setSubjectName("管理费用-研发费").setLeaf(true).setStatus(CommonStatusEnum.DISABLE.getStatus()));
            }
            return null;
        }));
        setField(service, "erpFinanceVoucherTemplateMapper", createProxy(ErpFinanceVoucherTemplateMapper.class, (methodName, args) -> null));
        setField(service, "erpFinanceVoucherTemplateItemMapper", createProxy(ErpFinanceVoucherTemplateItemMapper.class, (methodName, args) -> null));

        ServiceException ex = assertThrows(ServiceException.class, () -> service.createVoucherTemplate(
                buildTemplateSaveReqVO(List.of(buildItem(ErpFinanceVoucherEntryDirectionEnum.DEBIT.getType(), "660201", "管理费用-研发费")))));

        assertEquals(FINANCE_SUBJECT_NOT_ENABLE.getCode(), ex.getCode());
    }

    private ErpFinanceVoucherTemplateSaveReqVO buildTemplateSaveReqVO(List<ErpFinanceVoucherTemplateSaveReqVO.Item> items) {
        ErpFinanceVoucherTemplateSaveReqVO reqVO = new ErpFinanceVoucherTemplateSaveReqVO();
        reqVO.setLedgerId(1L);
        reqVO.setBizType(ErpBizTypeEnum.FINANCE_EXPENSE.getType());
        reqVO.setName("费用报销模板");
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        reqVO.setAutoGenerate(false);
        reqVO.setItems(items);
        return reqVO;
    }

    private ErpFinanceVoucherTemplateSaveReqVO.Item buildItem(Integer direction, String subjectCode, String subjectName) {
        ErpFinanceVoucherTemplateSaveReqVO.Item item = new ErpFinanceVoucherTemplateSaveReqVO.Item();
        item.setEntryDirection(direction);
        item.setSubjectCode(subjectCode);
        item.setSubjectName(subjectName);
        item.setAmountSource(ErpFinanceVoucherAmountSourceEnum.BIZ_AMOUNT.getType());
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

    private void setField(Object target, String fieldName, Object value) throws Exception {
        String actualFieldName = switch (fieldName) {
            case "voucherTemplateMapper" -> "erpFinanceVoucherTemplateMapper";
            case "voucherTemplateItemMapper" -> "erpFinanceVoucherTemplateItemMapper";
            default -> fieldName;
        };
        Field field = target.getClass().getDeclaredField(actualFieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @FunctionalInterface
    private interface MethodHandler {
        Object handle(String methodName, Object[] args);
    }
}
