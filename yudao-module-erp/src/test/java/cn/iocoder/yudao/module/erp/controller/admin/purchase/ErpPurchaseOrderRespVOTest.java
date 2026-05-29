package cn.iocoder.yudao.module.erp.controller.admin.purchase;

import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderExportItemRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderRespVO;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ErpPurchaseOrderRespVOTest {

    @Test
    void sourceOrderNos_shouldBeExportableToExcel() throws Exception {
        Field field = ErpPurchaseOrderRespVO.class.getDeclaredField("sourceOrderNos");
        assertNotNull(field.getAnnotation(cn.idev.excel.annotation.ExcelProperty.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void exportItems_shouldRepeatSourceForEveryOrderItemRow() throws Exception {
        ErpPurchaseOrderController controller = new ErpPurchaseOrderController();
        ErpPurchaseOrderRespVO order = new ErpPurchaseOrderRespVO()
                .setNo("PO20260515001")
                .setSupplierName("华东紧固件供应商")
                .setSourceOrderNos("SO20260501001")
                .setItems(List.of(
                        new ErpPurchaseOrderRespVO.Item().setProductName("六角螺丝 M6x20").setCount(new BigDecimal("1000")),
                        new ErpPurchaseOrderRespVO.Item().setProductName("平垫 6mm").setCount(new BigDecimal("1000"))));
        Method method = ErpPurchaseOrderController.class.getDeclaredMethod("buildPurchaseOrderExportItemList", List.class);
        method.setAccessible(true);

        List<ErpPurchaseOrderExportItemRespVO> result =
                (List<ErpPurchaseOrderExportItemRespVO>) method.invoke(controller, List.of(order));

        assertEquals(2, result.size());
        assertEquals("SO20260501001", result.get(0).getSourceOrderNos());
        assertEquals("SO20260501001", result.get(1).getSourceOrderNos());
        assertEquals("六角螺丝 M6x20", result.get(0).getProductName());
        assertEquals("平垫 6mm", result.get(1).getProductName());
    }
}
