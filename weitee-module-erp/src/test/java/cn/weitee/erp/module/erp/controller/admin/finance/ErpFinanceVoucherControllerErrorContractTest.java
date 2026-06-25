package cn.weitee.erp.module.erp.controller.admin.finance;

import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.framework.web.core.handler.GlobalExceptionHandler;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.voucher.ErpFinanceVoucherGenerateReqVO;
import cn.weitee.erp.module.erp.enums.ErrorCodeConstantsFinanceLedger;
import cn.weitee.erp.module.erp.enums.ErrorCodeConstantsFinanceVoucher;
import cn.weitee.erp.module.erp.enums.common.ErpBizTypeEnum;
import cn.weitee.erp.module.erp.service.finance.ErpFinanceVoucherService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ErpFinanceVoucherControllerErrorContractTest {

    @Test
    void generateVoucher_shouldExposeSourceTimeRequiredError() throws Exception {
        ErpFinanceVoucherController controller = new ErpFinanceVoucherController();
        setField(controller, "financeVoucherService", createVoucherServiceProxy(
                new ServiceException(ErrorCodeConstantsFinanceVoucher.FINANCE_VOUCHER_SOURCE_TIME_REQUIRED.getCode(),
                        "业务单据(XSCK20260429000003)缺少可用的业务日期，请先补充或手工指定凭证日期")));

        ServiceException ex = assertThrows(ServiceException.class, () -> controller.generateVoucher(new ErpFinanceVoucherGenerateReqVO()
                .setLedgerId(1L)
                .setBizType(ErpBizTypeEnum.SALE_OUT.getType())
                .setBizId(502L)
                .setTemplateId(35L)));

        CommonResult<?> result = new GlobalExceptionHandler("test-app", null).serviceExceptionHandler(ex);
        assertEquals(ErrorCodeConstantsFinanceVoucher.FINANCE_VOUCHER_SOURCE_TIME_REQUIRED.getCode(), result.getCode());
        assertEquals("业务单据(XSCK20260429000003)缺少可用的业务日期，请先补充或手工指定凭证日期", result.getMsg());
    }

    @Test
    void generateVoucher_shouldExposeDuplicateDefaultLedgerError() throws Exception {
        ErpFinanceVoucherController controller = new ErpFinanceVoucherController();
        setField(controller, "financeVoucherService", createVoucherServiceProxy(
                new ServiceException(ErrorCodeConstantsFinanceLedger.FINANCE_LEDGER_DEFAULT_DUPLICATE.getCode(),
                        ErrorCodeConstantsFinanceLedger.FINANCE_LEDGER_DEFAULT_DUPLICATE.getMsg())));

        ServiceException ex = assertThrows(ServiceException.class, () -> controller.generateVoucher(new ErpFinanceVoucherGenerateReqVO()
                .setLedgerId(1L)
                .setBizType(ErpBizTypeEnum.SALE_RETURN.getType())
                .setBizId(602L)
                .setTemplateId(36L)));

        CommonResult<?> result = new GlobalExceptionHandler("test-app", null).serviceExceptionHandler(ex);
        assertEquals(ErrorCodeConstantsFinanceLedger.FINANCE_LEDGER_DEFAULT_DUPLICATE.getCode(), result.getCode());
        assertEquals(ErrorCodeConstantsFinanceLedger.FINANCE_LEDGER_DEFAULT_DUPLICATE.getMsg(), result.getMsg());
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

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
