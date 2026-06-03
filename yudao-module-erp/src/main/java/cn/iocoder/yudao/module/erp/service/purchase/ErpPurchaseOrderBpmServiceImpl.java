package cn.iocoder.yudao.module.erp.service.purchase;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.bpm.service.approval.BpmApprovalRuntimeService;
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceService;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderCancelApprovalReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderSubmitReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpPurchaseSuggestDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseOrderAuditLogDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseOrderDO;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpPurchaseSuggestMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseOrderAuditLogMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseOrderMapper;
import cn.iocoder.yudao.module.erp.enums.ErpAuditStatus;
import cn.iocoder.yudao.module.erp.enums.ErpPurchaseOrderAuditActionTypeConstants;
import cn.iocoder.yudao.module.erp.enums.ErpPurchaseOrderBpmConstants;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PURCHASE_ORDER_APPROVE_FAIL;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PURCHASE_ORDER_BPM_CANCEL_FAIL;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PURCHASE_ORDER_BPM_SUBMIT_FAIL;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PURCHASE_ORDER_NOT_EXISTS;

@Service
@Validated
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
        approvalRuntimeService.submit("erp.purchase.order.submit", purchaseOrder.getId(), userId);
        erpPurchaseOrderMapper.updateById(new ErpPurchaseOrderDO()
                .setId(purchaseOrder.getId())
                .setStatus(ErpAuditStatus.PROCESS.getStatus()));
        if (ObjectUtil.equal(purchaseOrder.getStatus(), ErpAuditStatus.REJECT.getStatus())) {
            erpPurchaseOrderAuditLogMapper.insert(new ErpPurchaseOrderAuditLogDO()
                    .setOrderId(purchaseOrder.getId())
                    .setActionType(ErpPurchaseOrderAuditActionTypeConstants.RESUBMIT)
                    .setBeforeStatus(ErpAuditStatus.REJECT.getStatus())
                    .setAfterStatus(ErpAuditStatus.PROCESS.getStatus()));
        }
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
        approvalRuntimeService.cancel("erp.purchase.order.submit", purchaseOrder.getId(), userId, reqVO.getReason());
        erpPurchaseOrderAuditLogMapper.insert(new ErpPurchaseOrderAuditLogDO()
                .setOrderId(purchaseOrder.getId())
                .setActionType(ErpPurchaseOrderAuditActionTypeConstants.CANCEL)
                .setBeforeStatus(ErpAuditStatus.PROCESS.getStatus())
                .setAfterStatus(ErpAuditStatus.PROCESS.getStatus())
                .setReason(reqVO.getReason()));
    }

    @Override
    public void handleProcessInstanceResult(Long orderId, String processInstanceId, Integer status, String reason) {
        // 旧监听器入口保留兼容，结果回写已收敛到 PurchaseOrderResultHandler
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
