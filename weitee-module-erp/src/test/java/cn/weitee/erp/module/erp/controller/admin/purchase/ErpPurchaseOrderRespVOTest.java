package cn.weitee.erp.module.erp.controller.admin.purchase;

import cn.weitee.erp.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderExportItemRespVO;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderAuditLogRespVO;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderRespVO;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
        ErpPurchaseOrderDisplayService displayService = new ErpPurchaseOrderDisplayService();
        ErpPurchaseOrderRespVO order = new ErpPurchaseOrderRespVO()
                .setNo("PO20260515001")
                .setSupplierName("华东紧固件供应商")
                .setSourceOrderNos("SO20260501001")
                .setItems(List.of(
                        new ErpPurchaseOrderRespVO.Item().setProductName("六角螺丝 M6x20").setCount(new BigDecimal("1000")),
                        new ErpPurchaseOrderRespVO.Item().setProductName("平垫 6mm").setCount(new BigDecimal("1000"))));
        Method method = ErpPurchaseOrderDisplayService.class.getDeclaredMethod("buildExportItemList", List.class);
        method.setAccessible(true);

        List<ErpPurchaseOrderExportItemRespVO> result =
                (List<ErpPurchaseOrderExportItemRespVO>) method.invoke(displayService, List.of(order));

        assertEquals(2, result.size());
        assertEquals("SO20260501001", result.get(0).getSourceOrderNos());
        assertEquals("SO20260501001", result.get(1).getSourceOrderNos());
        assertEquals("六角螺丝 M6x20", result.get(0).getProductName());
        assertEquals("平垫 6mm", result.get(1).getProductName());
    }

    @Test
    void detailRespVO_shouldExposeSeparatedOperationAndApprovalLogsWhileKeepingMergedAuditLogsForCompatibility() {
        ErpPurchaseOrderRespVO order = new ErpPurchaseOrderRespVO();
        ErpPurchaseOrderAuditLogRespVO operationLog = new ErpPurchaseOrderAuditLogRespVO();
        operationLog.setActionType("UPDATE");
        operationLog.setOperatorId(1L);
        operationLog.setCreateTime(LocalDateTime.of(2026, 7, 6, 10, 0));
        List<ErpPurchaseOrderAuditLogRespVO> operationLogs = List.of(operationLog);

        ErpPurchaseOrderAuditLogRespVO approvalLog = new ErpPurchaseOrderAuditLogRespVO();
        approvalLog.setActionType("APPROVE");
        approvalLog.setOperatorId(2L);
        approvalLog.setTaskName("采购审批组长");
        approvalLog.setCreateTime(LocalDateTime.of(2026, 7, 6, 11, 0));
        List<ErpPurchaseOrderAuditLogRespVO> approvalLogs = List.of(approvalLog);

        order.setOperationLogs(operationLogs);
        order.setApprovalLogs(approvalLogs);
        order.setAuditLogs(ErpPurchaseOrderDisplaySupport.mergeAuditLogs(new ArrayList<>(operationLogs), approvalLogs));

        assertEquals(List.of("UPDATE"), order.getOperationLogs().stream()
                .map(ErpPurchaseOrderAuditLogRespVO::getActionType).toList());
        assertEquals(List.of("APPROVE"), order.getApprovalLogs().stream()
                .map(ErpPurchaseOrderAuditLogRespVO::getActionType).toList());
        assertEquals(List.of("APPROVE", "UPDATE"), order.getAuditLogs().stream()
                .map(ErpPurchaseOrderAuditLogRespVO::getActionType).toList());
    }
}
