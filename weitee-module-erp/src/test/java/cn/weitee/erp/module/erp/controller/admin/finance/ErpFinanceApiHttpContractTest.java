package cn.weitee.erp.module.erp.controller.admin.finance;

import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.framework.web.core.handler.GlobalExceptionHandler;
import cn.weitee.erp.module.erp.enums.ErrorCodeConstantsFinanceLedger;
import cn.weitee.erp.module.erp.enums.ErrorCodeConstantsFinanceVoucher;
import cn.weitee.erp.module.erp.enums.common.ErpBizTypeEnum;
import cn.weitee.erp.module.erp.service.finance.ErpFinanceLedgerService;
import cn.weitee.erp.module.erp.service.finance.ErpFinanceVoucherService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicReference;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ErpFinanceApiHttpContractTest {

    @Test
    void financeVoucherGenerate_shouldReturnBusinessErrorJsonWhenSourceTimeMissing() throws Exception {
        ErpFinanceVoucherController controller = new ErpFinanceVoucherController();
        setField(controller, "financeVoucherService", createVoucherServiceProxy(
                new ServiceException(ErrorCodeConstantsFinanceVoucher.FINANCE_VOUCHER_SOURCE_TIME_REQUIRED.getCode(),
                        "业务单据(XSCK20260429000003)缺少可用的业务日期，请先补充或手工指定凭证日期")));

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler("test-app", null))
                .build();

        mockMvc.perform(post("/erp/finance-voucher/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ledgerId": 1,
                                  "bizType": 21,
                                  "bizId": 502,
                                  "templateId": 35
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(
                        ErrorCodeConstantsFinanceVoucher.FINANCE_VOUCHER_SOURCE_TIME_REQUIRED.getCode()))
                .andExpect(jsonPath("$.msg").value("业务单据(XSCK20260429000003)缺少可用的业务日期，请先补充或手工指定凭证日期"));
    }

    @Test
    void financeVoucherGenerate_shouldReturnBadRequestJsonWhenBizTypeInvalid() throws Exception {
        ErpFinanceVoucherController controller = new ErpFinanceVoucherController();
        setField(controller, "financeVoucherService", createVoucherServiceProxy(
                new ServiceException(ErrorCodeConstantsFinanceVoucher.FINANCE_VOUCHER_SOURCE_TIME_REQUIRED.getCode(),
                        "unused")));

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler("test-app", null))
                .build();

        mockMvc.perform(post("/erp/finance-voucher/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ledgerId": 1,
                                  "bizType": %d,
                                  "bizId": 502,
                                  "templateId": 35
                                }
                                """.formatted(ErpBizTypeEnum.SALE_RETURN.getType() + 2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.msg", Matchers.containsString("必须在指定范围")));
    }

    @Test
    void financeLedgerUpdateDefaultStatus_shouldReturnSuccessJsonAndInvokeRepairPath() throws Exception {
        ErpFinanceLedgerController controller = new ErpFinanceLedgerController();
        AtomicReference<Long> ledgerIdRef = new AtomicReference<>();
        AtomicReference<Boolean> defaultStatusRef = new AtomicReference<>();
        setField(controller, "financeLedgerService", createLedgerServiceProxy(ledgerIdRef, defaultStatusRef, null));

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler("test-app", null))
                .build();

        mockMvc.perform(put("/erp/finance-ledger/update-default-status")
                        .param("id", "11")
                        .param("defaultStatus", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(true));

        Assertions.assertEquals(11L, ledgerIdRef.get());
        Assertions.assertEquals(Boolean.TRUE, defaultStatusRef.get());
    }

    @Test
    void financeLedgerUpdateDefaultStatus_shouldReturnBusinessErrorJsonWhenDuplicateDefaultExists() throws Exception {
        ErpFinanceLedgerController controller = new ErpFinanceLedgerController();
        setField(controller, "financeLedgerService", createLedgerServiceProxy(new AtomicReference<>(), new AtomicReference<>(),
                new ServiceException(ErrorCodeConstantsFinanceLedger.FINANCE_LEDGER_DEFAULT_DUPLICATE.getCode(),
                        ErrorCodeConstantsFinanceLedger.FINANCE_LEDGER_DEFAULT_DUPLICATE.getMsg())));

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler("test-app", null))
                .build();

        mockMvc.perform(put("/erp/finance-ledger/update-default-status")
                        .param("id", "11")
                        .param("defaultStatus", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(
                        ErrorCodeConstantsFinanceLedger.FINANCE_LEDGER_DEFAULT_DUPLICATE.getCode()))
                .andExpect(jsonPath("$.msg").value(
                        ErrorCodeConstantsFinanceLedger.FINANCE_LEDGER_DEFAULT_DUPLICATE.getMsg()));
    }

    private ErpFinanceVoucherService createVoucherServiceProxy(ServiceException exception) {
        return (ErpFinanceVoucherService) Proxy.newProxyInstance(
                ErpFinanceVoucherService.class.getClassLoader(),
                new Class<?>[]{ErpFinanceVoucherService.class},
                (proxy, method, args) -> {
                    if ("generateVoucher".equals(method.getName())) {
                        throw exception;
                    }
                    return null;
                });
    }

    private ErpFinanceLedgerService createLedgerServiceProxy(AtomicReference<Long> ledgerIdRef,
                                                             AtomicReference<Boolean> defaultStatusRef,
                                                             ServiceException exception) {
        return (ErpFinanceLedgerService) Proxy.newProxyInstance(
                ErpFinanceLedgerService.class.getClassLoader(),
                new Class<?>[]{ErpFinanceLedgerService.class},
                (proxy, method, args) -> {
                    if ("updateFinanceLedgerDefaultStatus".equals(method.getName())) {
                        if (exception != null) {
                            throw exception;
                        }
                        ledgerIdRef.set((Long) args[0]);
                        defaultStatusRef.set((Boolean) args[1]);
                    }
                    return null;
                });
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
