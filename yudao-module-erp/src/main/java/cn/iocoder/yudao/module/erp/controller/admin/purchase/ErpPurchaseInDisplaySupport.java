package cn.iocoder.yudao.module.erp.controller.admin.purchase;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.bpm.enums.task.BpmTaskStatusEnum;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInAuditLogRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInDO;
import cn.iocoder.yudao.module.erp.enums.ErpPurchaseInStockInStatusEnum;
import cn.iocoder.yudao.module.erp.enums.ErpQaStatusEnum;

import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

final class ErpPurchaseInDisplaySupport {

    private ErpPurchaseInDisplaySupport() {
    }

    static List<ErpPurchaseInAuditLogRespVO> mergeAuditLogs(List<ErpPurchaseInAuditLogRespVO> businessLogs,
                                                            List<ErpPurchaseInAuditLogRespVO> bpmLogs) {
        List<ErpPurchaseInAuditLogRespVO> mergedLogs = businessLogs == null
                ? new ArrayList<>()
                : new ArrayList<>(businessLogs);
        if (bpmLogs != null && !bpmLogs.isEmpty()) {
            for (ErpPurchaseInAuditLogRespVO bpmLog : bpmLogs) {
                removeDuplicateTerminalLog(mergedLogs, bpmLog);
                mergedLogs.add(bpmLog);
            }
        }
        mergedLogs.sort(Comparator.comparing(ErpPurchaseInAuditLogRespVO::getCreateTime,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return mergedLogs;
    }

    static List<ErpPurchaseInAuditLogRespVO> buildBusinessAuditLogRespVOs(ErpPurchaseInDO purchaseIn) {
        List<ErpPurchaseInAuditLogRespVO> auditLogs = new ArrayList<>(2);
        addAuditLog(auditLogs, buildQualityCheckAuditLogRespVO(purchaseIn));
        addAuditLog(auditLogs, buildStockInAuditLogRespVO(purchaseIn));
        return auditLogs;
    }

    static ErpPurchaseInAuditLogRespVO buildQualityCheckAuditLogRespVO(ErpPurchaseInDO purchaseIn) {
        if (purchaseIn == null || purchaseIn.getQaStatus() == null
                || Objects.equals(purchaseIn.getQaStatus(), ErpQaStatusEnum.TO_INSPECT.getStatus())
                || purchaseIn.getQaTime() == null || purchaseIn.getQaUserId() == null) {
            return null;
        }
        String actionType = resolveActionTypeByQaStatus(purchaseIn.getQaStatus());
        if (actionType == null) {
            return null;
        }
        ErpPurchaseInAuditLogRespVO respVO = new ErpPurchaseInAuditLogRespVO();
        respVO.setActionType(actionType);
        respVO.setBeforeStatus(ErpQaStatusEnum.TO_INSPECT.getStatus());
        respVO.setAfterStatus(purchaseIn.getQaStatus());
        respVO.setReason(purchaseIn.getQaRemark());
        respVO.setOperatorId(purchaseIn.getQaUserId());
        respVO.setTaskName("入库质检");
        respVO.setCreateTime(purchaseIn.getQaTime());
        return respVO;
    }

    static ErpPurchaseInAuditLogRespVO buildStockInAuditLogRespVO(ErpPurchaseInDO purchaseIn) {
        if (purchaseIn == null
                || (purchaseIn.getStockInStatus() == null
                || (!Objects.equals(purchaseIn.getStockInStatus(), ErpPurchaseInStockInStatusEnum.STOCKED_IN.getStatus())
                && !Objects.equals(purchaseIn.getStockInStatus(), ErpPurchaseInStockInStatusEnum.PARTIAL_STOCKED_IN.getStatus())))
                || purchaseIn.getStockInTime() == null || purchaseIn.getStockInUserId() == null) {
            return null;
        }
        ErpPurchaseInAuditLogRespVO respVO = new ErpPurchaseInAuditLogRespVO();
        respVO.setActionType(Objects.equals(purchaseIn.getStockInStatus(), ErpPurchaseInStockInStatusEnum.PARTIAL_STOCKED_IN.getStatus())
                ? "STOCK_IN_PARTIAL"
                : "STOCK_IN_CONFIRMED");
        respVO.setBeforeStatus(ErpPurchaseInStockInStatusEnum.TO_STOCK_IN.getStatus());
        respVO.setAfterStatus(purchaseIn.getStockInStatus());
        respVO.setOperatorId(purchaseIn.getStockInUserId());
        respVO.setTaskName("最终入库");
        respVO.setCreateTime(purchaseIn.getStockInTime());
        return respVO;
    }

    static String resolveActionTypeByTaskStatus(Integer taskStatus) {
        if (Objects.equals(taskStatus, BpmTaskStatusEnum.APPROVE.getStatus())) {
            return "APPROVE";
        }
        if (Objects.equals(taskStatus, BpmTaskStatusEnum.REJECT.getStatus())) {
            return "REJECT";
        }
        if (Objects.equals(taskStatus, BpmTaskStatusEnum.CANCEL.getStatus())) {
            return "CANCEL";
        }
        if (Objects.equals(taskStatus, BpmTaskStatusEnum.RETURN.getStatus())) {
            return "REJECT";
        }
        return null;
    }

    static String resolveActionTypeByQaStatus(Integer qaStatus) {
        if (Objects.equals(qaStatus, ErpQaStatusEnum.PARTIAL.getStatus())) {
            return "QUALITY_CHECK_PARTIAL";
        }
        if (Objects.equals(qaStatus, ErpQaStatusEnum.PASSED.getStatus())) {
            return "QUALITY_CHECK_PASSED";
        }
        if (Objects.equals(qaStatus, ErpQaStatusEnum.REJECTED.getStatus())) {
            return "QUALITY_CHECK_REJECTED";
        }
        return null;
    }

    private static void addAuditLog(List<ErpPurchaseInAuditLogRespVO> auditLogs,
                                    ErpPurchaseInAuditLogRespVO auditLog) {
        if (auditLog != null) {
            auditLogs.add(auditLog);
        }
    }

    private static void removeDuplicateTerminalLog(List<ErpPurchaseInAuditLogRespVO> businessLogs,
                                                   ErpPurchaseInAuditLogRespVO bpmLog) {
        if (businessLogs == null || businessLogs.isEmpty() || StrUtil.isBlank(bpmLog.getTaskName())) {
            return;
        }
        businessLogs.removeIf(businessLog -> StrUtil.isBlank(businessLog.getTaskName())
                && Objects.equals(businessLog.getActionType(), bpmLog.getActionType())
                && Objects.equals(businessLog.getOperatorId(), bpmLog.getOperatorId()));
    }

}
