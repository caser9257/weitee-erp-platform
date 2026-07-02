package cn.weitee.erp.module.erp.service.purchase;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInQualityDO;
import cn.weitee.erp.module.erp.enums.ErpPurchaseInQualityResultEnum;
import cn.weitee.erp.module.system.api.notify.NotifyMessageSendApi;
import cn.weitee.erp.module.system.api.notify.dto.NotifySendSingleToUserReqDTO;
import cn.weitee.erp.module.system.service.permission.MenuService;
import cn.weitee.erp.module.system.service.permission.PermissionService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 质检单通知辅助类。
 * 提取自 ErpPurchaseInQualityServiceImpl，供首检、复检、指派质检人等流程共用。
 */
@Slf4j
@Component
class ErpPurchaseInQualityNotificationHelper {

    private static final String IQC_ASSIGN_NOTIFY_TEMPLATE_CODE = "erp_iqc_checker_assigned";
    private static final String IQC_STOCK_IN_READY_NOTIFY_TEMPLATE_CODE = "erp_iqc_stock_in_ready";
    private static final String IQC_RECHECK_REJECTED_NOTIFY_TEMPLATE_CODE = "erp_iqc_recheck_rejected";
    private static final String PURCHASE_IN_CONFIRM_STOCK_PERMISSION = "erp:purchase-in:update-status";

    @Resource
    private NotifyMessageSendApi notifyMessageSendApi;
    @Resource
    private MenuService menuService;
    @Resource
    private PermissionService permissionService;

    /**
     * 发送指派质检人通知。
     */
    void sendAssignCheckerNotify(ErpPurchaseInQualityDO quality, ErpPurchaseInDO purchaseIn,
                                 Long assignedCheckerUserId) {
        String purchaseInNo = quality.getPurchaseInNo();
        if (purchaseInNo == null && purchaseIn != null) {
            purchaseInNo = purchaseIn.getNo();
        }
        Map<String, Object> templateParams = new HashMap<>();
        templateParams.put("qualityNo", quality.getNo());
        templateParams.put("purchaseInNo", purchaseInNo);

        NotifySendSingleToUserReqDTO notifyReqDTO = new NotifySendSingleToUserReqDTO();
        notifyReqDTO.setUserId(assignedCheckerUserId);
        notifyReqDTO.setTemplateCode(IQC_ASSIGN_NOTIFY_TEMPLATE_CODE);
        notifyReqDTO.setTemplateParams(templateParams);
        try {
            notifyMessageSendApi.sendSingleMessageToAdmin(notifyReqDTO);
        } catch (Exception ex) {
            log.error("[sendAssignCheckerNotify][qualityId({}) assignedCheckerUserId({}) notify failed]",
                    quality.getId(), assignedCheckerUserId, ex);
        }
    }

    /**
     * 发送质检完成通知。
     */
    void sendQualityFinishedNotify(ErpPurchaseInQualityDO quality, ErpPurchaseInDO purchaseIn,
                                   ErpPurchaseInQualityResultEnum result,
                                   BigDecimal totalPassCount, BigDecimal totalRejectCount,
                                   Integer stockInStatus) {
        Set<Long> receiveUserIds = new LinkedHashSet<>();
        addUserId(receiveUserIds, parseUserId(purchaseIn.getCreator()));
        if (result != ErpPurchaseInQualityResultEnum.REJECTED) {
            receiveUserIds.addAll(getUserIdsByPermission(PURCHASE_IN_CONFIRM_STOCK_PERMISSION));
        }
        if (CollUtil.isEmpty(receiveUserIds)) {
            return;
        }

        Map<String, Object> templateParams = new HashMap<>();
        templateParams.put("qualityNo", quality.getNo());
        templateParams.put("purchaseInNo", purchaseIn.getNo());
        templateParams.put("qaResult", result.getName());
        templateParams.put("passCount", totalPassCount);
        templateParams.put("rejectCount", totalRejectCount);
        templateParams.put("stockInStatus", stockInStatus);

        String templateCode = result == ErpPurchaseInQualityResultEnum.REJECTED
                ? IQC_RECHECK_REJECTED_NOTIFY_TEMPLATE_CODE
                : IQC_STOCK_IN_READY_NOTIFY_TEMPLATE_CODE;
        for (Long receiveUserId : receiveUserIds) {
            NotifySendSingleToUserReqDTO notifyReqDTO = new NotifySendSingleToUserReqDTO();
            notifyReqDTO.setUserId(receiveUserId);
            notifyReqDTO.setTemplateCode(templateCode);
            notifyReqDTO.setTemplateParams(templateParams);
            try {
                notifyMessageSendApi.sendSingleMessageToAdmin(notifyReqDTO);
            } catch (Exception ex) {
                log.error("[sendQualityFinishedNotify][qualityId({}) purchaseInId({}) receiveUserId({}) notify failed]",
                        quality.getId(), purchaseIn.getId(), receiveUserId, ex);
            }
        }
    }

    private Set<Long> getUserIdsByPermission(String permission) {
        List<Long> menuIds = menuService.getMenuIdListByPermissionFromCache(permission);
        if (CollUtil.isEmpty(menuIds)) {
            return Set.of();
        }
        Set<Long> roleIds = new LinkedHashSet<>();
        menuIds.forEach(menuId -> roleIds.addAll(permissionService.getMenuRoleIdListByMenuIdFromCache(menuId)));
        if (CollUtil.isEmpty(roleIds)) {
            return Set.of();
        }
        return new LinkedHashSet<>(permissionService.getUserRoleIdListByRoleId(roleIds));
    }

    private void addUserId(Set<Long> userIds, Long userId) {
        if (userId != null) {
            userIds.add(userId);
        }
    }

    private Long parseUserId(String value) {
        return StrUtil.isNumeric(value) ? Long.valueOf(value) : null;
    }
}
