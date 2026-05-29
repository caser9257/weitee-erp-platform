package cn.iocoder.yudao.module.erp.controller.admin.sale;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.order.ErpSaleOrderClosureSummaryRespVO;
import cn.iocoder.yudao.module.erp.service.sale.ErpSaleOrderClosureService;
import cn.iocoder.yudao.module.erp.service.sale.bo.ErpSaleOrderClosureSummaryBO;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ErpSaleOrderControllerClosureSummaryTest {

    @Test
    void getSaleOrderClosureSummary_shouldReturnClosureSummary() throws Exception {
        ErpSaleOrderController controller = new ErpSaleOrderController();
        setField(controller, "saleOrderClosureService", createClosureServiceProxy());

        CommonResult<ErpSaleOrderClosureSummaryRespVO> result = controller.getSaleOrderClosureSummary(1L);

        assertNotNull(result.getData());
        assertEquals(1L, result.getData().getSaleOrderId());
        assertEquals("WAIT_PURCHASE_IQC", result.getData().getClosureStage());
        assertEquals(new BigDecimal("6"), result.getData().getRemainingShipQty());
    }

    private ErpSaleOrderClosureService createClosureServiceProxy() {
        return (ErpSaleOrderClosureService) Proxy.newProxyInstance(
                ErpSaleOrderClosureService.class.getClassLoader(),
                new Class<?>[]{ErpSaleOrderClosureService.class},
                (proxy, method, args) -> {
                    if ("getClosureSummary".equals(method.getName())) {
                        ErpSaleOrderClosureSummaryBO summary = new ErpSaleOrderClosureSummaryBO();
                        summary.setSaleOrderId(1L);
                        summary.setSaleOrderNo("SO-001");
                        summary.setClosureStage("WAIT_PURCHASE_IQC");
                        summary.setRemainingShipQty(new BigDecimal("6"));
                        return summary;
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
