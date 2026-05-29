package cn.iocoder.yudao.module.erp.controller.admin.purchase;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.bpm.enums.task.BpmTaskStatusEnum;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderAuditLogRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInDO;
import cn.iocoder.yudao.module.erp.enums.ErpAuditStatus;
import cn.iocoder.yudao.module.erp.enums.ErpPurchaseInStockInStatusEnum;
import cn.iocoder.yudao.module.erp.enums.ErpQaStatusEnum;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

final class ErpPurchaseOrderDisplaySupport {

    private static final String DISPLAY_DELIMITER = "\u3001";

    private ErpPurchaseOrderDisplaySupport() {
    }

    static String resolveBusinessOwnerName(Collection<String> saleUserNames,
                                           Collection<String> sourceCreatorNames,
                                           String creatorName) {
        String saleUserDisplay = joinDistinctDisplay(saleUserNames);
        if (StrUtil.isNotBlank(saleUserDisplay)) {
            return saleUserDisplay;
        }
        String sourceCreatorDisplay = joinDistinctDisplay(sourceCreatorNames);
        if (StrUtil.isNotBlank(sourceCreatorDisplay)) {
            return sourceCreatorDisplay;
        }
        return StrUtil.blankToDefault(StrUtil.trim(creatorName), null);
    }

    static String joinDistinctDisplay(Collection<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        LinkedHashSet<String> result = new LinkedHashSet<>();
        for (String value : values) {
            String trimmed = StrUtil.trim(value);
            if (StrUtil.isNotBlank(trimmed)) {
                result.add(trimmed);
            }
        }
        return result.isEmpty() ? null : String.join(DISPLAY_DELIMITER, result);
    }

    static boolean hasPendingPurchaseIn(Collection<ErpPurchaseInDO> purchaseIns) {
        if (purchaseIns == null || purchaseIns.isEmpty()) {
            return false;
        }
        return purchaseIns.stream().anyMatch(ErpPurchaseOrderDisplaySupport::isPendingPurchaseIn);
    }

    static Long resolvePendingPurchaseInId(Collection<ErpPurchaseInDO> purchaseIns) {
        if (purchaseIns == null || purchaseIns.isEmpty()) {
            return null;
        }
        return purchaseIns.stream()
                .filter(ErpPurchaseOrderDisplaySupport::isPendingPurchaseIn)
                .map(ErpPurchaseInDO::getId)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    static boolean shouldDisplayPendingPurchaseIn(BigDecimal totalCount, BigDecimal inCount,
                                                  Collection<ErpPurchaseInDO> purchaseIns) {
        if (isInboundCompleted(totalCount, inCount)) {
            return false;
        }
        return hasPendingPurchaseIn(purchaseIns);
    }

    static Long resolveDisplayPendingPurchaseInId(BigDecimal totalCount, BigDecimal inCount,
                                                  Collection<ErpPurchaseInDO> purchaseIns) {
        if (!shouldDisplayPendingPurchaseIn(totalCount, inCount, purchaseIns)) {
            return null;
        }
        return resolvePendingPurchaseInId(purchaseIns);
    }

    static List<ErpPurchaseOrderAuditLogRespVO> mergeAuditLogs(List<ErpPurchaseOrderAuditLogRespVO> businessLogs,
                                                               List<ErpPurchaseOrderAuditLogRespVO> bpmLogs) {
        List<ErpPurchaseOrderAuditLogRespVO> mergedLogs = businessLogs;
        if (bpmLogs != null && !bpmLogs.isEmpty()) {
            for (ErpPurchaseOrderAuditLogRespVO bpmLog : bpmLogs) {
                removeDuplicateTerminalLog(mergedLogs, bpmLog);
                mergedLogs.add(bpmLog);
            }
        }
        mergedLogs.sort(Comparator.comparing(ErpPurchaseOrderAuditLogRespVO::getCreateTime,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return mergedLogs;
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

    private static boolean isPendingPurchaseIn(ErpPurchaseInDO purchaseIn) {
        if (purchaseIn == null) {
            return false;
        }
        if (!Objects.equals(purchaseIn.getStatus(), ErpAuditStatus.APPROVE.getStatus())) {
            return true;
        }
        if (purchaseIn.getQaStatus() == null
                || Objects.equals(purchaseIn.getQaStatus(), ErpQaStatusEnum.TO_INSPECT.getStatus())) {
            return true;
        }
        return Objects.equals(purchaseIn.getStockInStatus(), ErpPurchaseInStockInStatusEnum.TO_STOCK_IN.getStatus())
                || Objects.equals(purchaseIn.getStockInStatus(), ErpPurchaseInStockInStatusEnum.PARTIAL_STOCKED_IN.getStatus());
    }

    private static boolean isInboundCompleted(BigDecimal totalCount, BigDecimal inCount) {
        BigDecimal safeTotalCount = totalCount == null ? BigDecimal.ZERO : totalCount;
        BigDecimal safeInCount = inCount == null ? BigDecimal.ZERO : inCount;
        return safeTotalCount.compareTo(BigDecimal.ZERO) > 0 && safeInCount.compareTo(safeTotalCount) >= 0;
    }

    private static void removeDuplicateTerminalLog(List<ErpPurchaseOrderAuditLogRespVO> businessLogs,
                                                   ErpPurchaseOrderAuditLogRespVO bpmLog) {
        if (businessLogs == null || businessLogs.isEmpty()) {
            return;
        }
        if (StrUtil.isBlank(bpmLog.getTaskName())) {
            return;
        }
        businessLogs.removeIf(businessLog -> StrUtil.isBlank(businessLog.getTaskName())
                && Objects.equals(businessLog.getActionType(), bpmLog.getActionType())
                && Objects.equals(businessLog.getOperatorId(), bpmLog.getOperatorId()));
    }
}
