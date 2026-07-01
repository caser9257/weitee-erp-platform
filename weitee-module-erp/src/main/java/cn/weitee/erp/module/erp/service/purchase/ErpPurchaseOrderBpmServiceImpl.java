package cn.weitee.erp.module.erp.service.purchase;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.weitee.erp.module.bpm.service.approval.BpmApprovalRuntimeService;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderCancelApprovalReqVO;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderSubmitReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpPurchaseSuggestDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseOrderAuditLogDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseOrderDO;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpPurchaseSuggestMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseOrderAuditLogMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseOrderMapper;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.enums.ErpPurchaseOrderAuditActionTypeConstants;
import cn.weitee.erp.module.erp.enums.ErpPurchaseOrderBpmConstants;
import cn.weitee.erp.module.erp.util.ErpTransactionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PURCHASE_ORDER_APPROVE_FAIL;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PURCHASE_ORDER_BPM_CANCEL_FAIL;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PURCHASE_ORDER_BPM_SUBMIT_FAIL;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PURCHASE_ORDER_NOT_EXISTS;

@Service
@Validated
@Slf4j
public class ErpPurchaseOrderBpmServiceImpl implements ErpPurchaseOrderBpmService {

    @Resource
    private ErpPurchaseOrderMapper erpPurchaseOrderMapper;
    @Resource
    private ErpPurchaseOrderAuditLogMapper erpPurchaseOrderAuditLogMapper;
    @Resource
    private ErpPurchaseOrderService purchaseOrderService;
    @Resource
    private ErpPurchaseSuggestMapper erpPurchaseSuggestMapper;

    @Resource
    private BpmApprovalRuntimeService approvalRuntimeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String submitPurchaseOrder(Long userId, ErpPurchaseOrderSubmitReqVO reqVO) {
        ErpPurchaseOrderDO purchaseOrder = getRequiredPurchaseOrder(reqVO.getId());
        if (ObjectUtil.equal(purchaseOrder.getStatus(), ErpAuditStatus.APPROVE.getStatus())) {
            throw exception(PURCHASE_ORDER_APPROVE_FAIL);
        }
        if (ObjectUtil.equal(purchaseOrder.getStatus(), ErpAuditStatus.PROCESS.getStatus())
                && StrUtil.isNotBlank(purchaseOrder.getProcessInstanceId())) {
            throw exception(PURCHASE_ORDER_BPM_SUBMIT_FAIL);
        }
        // 事务内：只写本地状态
        Long orderId = purchaseOrder.getId();
        erpPurchaseOrderMapper.updateById(new ErpPurchaseOrderDO()
                .setId(orderId)
                .setStatus(ErpAuditStatus.PROCESS.getStatus())
                .setProcessInstanceId(null));
        if (ObjectUtil.equal(purchaseOrder.getStatus(), ErpAuditStatus.REJECT.getStatus())) {
            erpPurchaseOrderAuditLogMapper.insert(new ErpPurchaseOrderAuditLogDO()
                    .setOrderId(orderId)
                    .setActionType(ErpPurchaseOrderAuditActionTypeConstants.RESUBMIT)
                    .setBeforeStatus(ErpAuditStatus.REJECT.getStatus())
                    .setAfterStatus(ErpAuditStatus.PROCESS.getStatus()));
        }

        // 事务外：调 BPM 创建流程
        ErpTransactionUtils.afterCommit(() -> {
            try {
                String processInstanceId = approvalRuntimeService.submit(
                        "erp.purchase.order.submit", orderId, userId);
                erpPurchaseOrderMapper.updateById(new ErpPurchaseOrderDO()
                        .setId(orderId)
                        .setProcessInstanceId(processInstanceId));
            } catch (Exception e) {
                log.error("[submitPurchaseOrder] BPM 创建失败，orderId={}", orderId, e);
                erpPurchaseOrderMapper.updateById(new ErpPurchaseOrderDO()
                        .setId(orderId)
                        .setStatus(ErpAuditStatus.FAILED.getStatus())
                        .setProcessInstanceId(null));
            }
        });
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelPurchaseOrderApproval(Long userId, ErpPurchaseOrderCancelApprovalReqVO reqVO) {
        ErpPurchaseOrderDO purchaseOrder = getRequiredPurchaseOrder(reqVO.getId());
        if (!ObjectUtil.equal(purchaseOrder.getStatus(), ErpAuditStatus.PROCESS.getStatus())
                || StrUtil.isBlank(purchaseOrder.getProcessInstanceId())) {
            throw exception(PURCHASE_ORDER_BPM_CANCEL_FAIL);
        }
        // 事务内：只做本地校验
        Long orderId = purchaseOrder.getId();
        ErpTransactionUtils.afterCommit(() -> {
            try {
                approvalRuntimeService.cancel("erp.purchase.order.submit", orderId, userId, reqVO.getReason());
                // 注意：approvalRuntimeService.cancel() 内部会自行注册 afterCommit 调 BPM
                // 若 BPM 撤回失败，cancel() 内部会标记 snapshot 为 FAILED，但此处无法感知
                // 审计日志在此写入表示"已发起撤回请求"，实际撤回结果由 BPM 回调确认
                erpPurchaseOrderAuditLogMapper.insert(new ErpPurchaseOrderAuditLogDO()
                        .setOrderId(orderId)
                        .setActionType(ErpPurchaseOrderAuditActionTypeConstants.CANCEL)
                        .setBeforeStatus(ErpAuditStatus.PROCESS.getStatus())
                        .setAfterStatus(ErpAuditStatus.PROCESS.getStatus())
                        .setReason(reqVO.getReason()));
            } catch (Exception e) {
                log.warn("[cancelPurchaseOrderApproval] BPM 撤回失败，orderId={}", orderId, e);
            }
        });
    }

    private Map<String, Object> buildVariables(ErpPurchaseOrderDO purchaseOrder) {
        Map<String, Object> variables = new HashMap<>();
        variables.put(ErpPurchaseOrderBpmConstants.VARIABLE_ORDER_ID, purchaseOrder.getId());
        variables.put(ErpPurchaseOrderBpmConstants.VARIABLE_ORDER_NO, purchaseOrder.getNo());
        variables.put(ErpPurchaseOrderBpmConstants.VARIABLE_TOTAL_PRICE, purchaseOrder.getTotalPrice());
        variables.put(ErpPurchaseOrderBpmConstants.VARIABLE_SUPPLIER_ID, purchaseOrder.getSupplierId());
        variables.put(ErpPurchaseOrderBpmConstants.VARIABLE_SOURCE_TYPE, resolveSourceType(purchaseOrder.getId()));
        variables.put(ErpPurchaseOrderBpmConstants.VARIABLE_CREATOR_ID, parseCreatorId(purchaseOrder.getCreator()));
        return variables;
    }

    private String resolveSourceType(Long purchaseOrderId) {
        if (erpPurchaseSuggestMapper == null) {
            return ErpPurchaseOrderBpmConstants.SOURCE_TYPE_MANUAL;
        }
        List<ErpPurchaseSuggestDO> purchaseSuggests =
                erpPurchaseSuggestMapper.selectListByConvertPurchaseOrderIds(List.of(purchaseOrderId));
        return CollUtil.isEmpty(purchaseSuggests) ? ErpPurchaseOrderBpmConstants.SOURCE_TYPE_MANUAL
                : ErpPurchaseOrderBpmConstants.SOURCE_TYPE_MRP;
    }

    private Long parseCreatorId(String creator) {
        return StrUtil.isNumeric(creator) ? Long.valueOf(creator) : null;
    }

    private ErpPurchaseOrderDO getRequiredPurchaseOrder(Long orderId) {
        ErpPurchaseOrderDO purchaseOrder = erpPurchaseOrderMapper.selectById(orderId);
        if (purchaseOrder == null) {
            throw exception(PURCHASE_ORDER_NOT_EXISTS);
        }
        return purchaseOrder;
    }

}
