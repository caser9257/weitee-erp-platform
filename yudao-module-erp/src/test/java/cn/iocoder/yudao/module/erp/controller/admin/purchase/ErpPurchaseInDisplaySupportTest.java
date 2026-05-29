package cn.iocoder.yudao.module.erp.controller.admin.purchase;

import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInAuditLogRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInDO;
import cn.iocoder.yudao.module.erp.enums.ErpPurchaseInStockInStatusEnum;
import cn.iocoder.yudao.module.erp.enums.ErpQaStatusEnum;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ErpPurchaseInDisplaySupportTest {

    @Test
    void mergeAuditLogs_shouldAppendBpmNodeHistoryAndSortByTimeDesc() {
        LocalDateTime baseTime = LocalDateTime.of(2026, 4, 9, 11, 0);
        List<ErpPurchaseInAuditLogRespVO> businessLogs = List.of(
                buildAuditLog("RESUBMIT", null, 920201L, baseTime.minusMinutes(10), "重新提交审批")
        );
        List<ErpPurchaseInAuditLogRespVO> bpmLogs = List.of(
                buildAuditLog("APPROVE", "采购审批组长", 920202L, baseTime.minusMinutes(5), "组长审批通过"),
                buildAuditLog("APPROVE", "仓库负责人", 920204L, baseTime.minusMinutes(1), "仓库确认收货")
        );

        List<ErpPurchaseInAuditLogRespVO> mergedLogs =
                ErpPurchaseInDisplaySupport.mergeAuditLogs(new ArrayList<>(businessLogs), bpmLogs);

        assertEquals(3, mergedLogs.size());
        assertEquals("仓库负责人", mergedLogs.get(0).getTaskName());
        assertEquals("采购审批组长", mergedLogs.get(1).getTaskName());
        assertEquals("RESUBMIT", mergedLogs.get(2).getActionType());
    }

    @Test
    void mergeAuditLogs_shouldPreferDetailedBpmNodeLogWhenTerminalActionDuplicated() {
        LocalDateTime actionTime = LocalDateTime.of(2026, 4, 9, 11, 30);
        List<ErpPurchaseInAuditLogRespVO> businessLogs = List.of(
                buildAuditLog("APPROVE", null, 920204L, actionTime.plusSeconds(1), "流程审批通过")
        );
        List<ErpPurchaseInAuditLogRespVO> bpmLogs = List.of(
                buildAuditLog("APPROVE", "仓库负责人", 920204L, actionTime, "仓库确认收货")
        );

        List<ErpPurchaseInAuditLogRespVO> mergedLogs =
                ErpPurchaseInDisplaySupport.mergeAuditLogs(new ArrayList<>(businessLogs), bpmLogs);

        assertEquals(1, mergedLogs.size());
        assertEquals("仓库负责人", mergedLogs.get(0).getTaskName());
        assertEquals("仓库确认收货", mergedLogs.get(0).getReason());
    }

    @Test
    void mergeAuditLogs_shouldSupportImmutableBusinessLogs() {
        LocalDateTime actionTime = LocalDateTime.of(2026, 4, 10, 13, 30);
        List<ErpPurchaseInAuditLogRespVO> businessLogs = List.of();
        List<ErpPurchaseInAuditLogRespVO> bpmLogs = List.of(
                buildAuditLog("APPROVE", "仓库负责人", 920204L, actionTime, "仓库审批通过")
        );

        List<ErpPurchaseInAuditLogRespVO> mergedLogs =
                ErpPurchaseInDisplaySupport.mergeAuditLogs(businessLogs, bpmLogs);

        assertEquals(1, mergedLogs.size());
        assertEquals("APPROVE", mergedLogs.get(0).getActionType());
        assertEquals("仓库负责人", mergedLogs.get(0).getTaskName());
    }

    @Test
    void resolveActionTypeByTaskStatus_shouldMapBpmTaskTerminalStatus() {
        assertEquals("APPROVE", ErpPurchaseInDisplaySupport.resolveActionTypeByTaskStatus(2));
        assertEquals("REJECT", ErpPurchaseInDisplaySupport.resolveActionTypeByTaskStatus(3));
        assertEquals("CANCEL", ErpPurchaseInDisplaySupport.resolveActionTypeByTaskStatus(4));
        assertNull(ErpPurchaseInDisplaySupport.resolveActionTypeByTaskStatus(1));
    }

    @Test
    void buildQualityCheckAuditLogRespVO_shouldMapQualityCheckResult() {
        LocalDateTime qaTime = LocalDateTime.of(2026, 4, 9, 15, 30);
        ErpPurchaseInDO purchaseIn = new ErpPurchaseInDO()
                .setQaStatus(ErpQaStatusEnum.PARTIAL.getStatus())
                .setQaTime(qaTime)
                .setQaUserId(920204L)
                .setQaRemark("部分物料存在瑕疵");

        ErpPurchaseInAuditLogRespVO auditLog = ErpPurchaseInDisplaySupport.buildQualityCheckAuditLogRespVO(purchaseIn);

        assertEquals("QUALITY_CHECK_PARTIAL", auditLog.getActionType());
        assertEquals(ErpQaStatusEnum.TO_INSPECT.getStatus(), auditLog.getBeforeStatus());
        assertEquals(ErpQaStatusEnum.PARTIAL.getStatus(), auditLog.getAfterStatus());
        assertEquals(920204L, auditLog.getOperatorId());
        assertEquals("入库质检", auditLog.getTaskName());
        assertEquals(qaTime, auditLog.getCreateTime());
        assertEquals("部分物料存在瑕疵", auditLog.getReason());
    }

    @Test
    void buildQualityCheckAuditLogRespVO_shouldReturnNullWhenStillWaitingForQualityCheck() {
        ErpPurchaseInDO purchaseIn = new ErpPurchaseInDO()
                .setQaStatus(ErpQaStatusEnum.TO_INSPECT.getStatus())
                .setQaTime(LocalDateTime.of(2026, 4, 9, 15, 30))
                .setQaUserId(920204L);

        assertNull(ErpPurchaseInDisplaySupport.buildQualityCheckAuditLogRespVO(purchaseIn));
    }

    @Test
    void buildStockInAuditLogRespVO_shouldMapWarehouseStockInAction() {
        LocalDateTime stockInTime = LocalDateTime.of(2026, 4, 10, 9, 15);
        ErpPurchaseInDO purchaseIn = new ErpPurchaseInDO()
                .setStockInStatus(ErpPurchaseInStockInStatusEnum.STOCKED_IN.getStatus())
                .setStockInTime(stockInTime)
                .setStockInUserId(920205L);

        ErpPurchaseInAuditLogRespVO auditLog = ErpPurchaseInDisplaySupport.buildStockInAuditLogRespVO(purchaseIn);

        assertEquals("STOCK_IN_CONFIRMED", auditLog.getActionType());
        assertEquals(ErpPurchaseInStockInStatusEnum.TO_STOCK_IN.getStatus(), auditLog.getBeforeStatus());
        assertEquals(ErpPurchaseInStockInStatusEnum.STOCKED_IN.getStatus(), auditLog.getAfterStatus());
        assertEquals(920205L, auditLog.getOperatorId());
        assertEquals("最终入库", auditLog.getTaskName());
        assertEquals(stockInTime, auditLog.getCreateTime());
    }

    @Test
    void buildBusinessAuditLogRespVOs_shouldIncludeQualityCheckAndStockInLogs() {
        ErpPurchaseInDO purchaseIn = new ErpPurchaseInDO()
                .setQaStatus(ErpQaStatusEnum.PASSED.getStatus())
                .setQaTime(LocalDateTime.of(2026, 4, 9, 15, 30))
                .setQaUserId(920204L)
                .setStockInStatus(ErpPurchaseInStockInStatusEnum.STOCKED_IN.getStatus())
                .setStockInTime(LocalDateTime.of(2026, 4, 10, 9, 0))
                .setStockInUserId(920205L);

        List<ErpPurchaseInAuditLogRespVO> auditLogs = ErpPurchaseInDisplaySupport.buildBusinessAuditLogRespVOs(purchaseIn);

        assertEquals(2, auditLogs.size());
        assertEquals("QUALITY_CHECK_PASSED", auditLogs.get(0).getActionType());
        assertEquals("STOCK_IN_CONFIRMED", auditLogs.get(1).getActionType());
    }

    private ErpPurchaseInAuditLogRespVO buildAuditLog(String actionType, String taskName,
                                                      Long operatorId, LocalDateTime createTime,
                                                      String reason) {
        ErpPurchaseInAuditLogRespVO log = new ErpPurchaseInAuditLogRespVO();
        log.setActionType(actionType);
        log.setTaskName(taskName);
        log.setOperatorId(operatorId);
        log.setCreateTime(createTime);
        log.setReason(reason);
        return log;
    }
}
