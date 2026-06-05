package cn.iocoder.yudao.module.erp.service.purchase.approval;

import cn.iocoder.yudao.module.bpm.service.approval.provider.ApprovalContext;
import cn.iocoder.yudao.module.bpm.service.approval.provider.ApprovalContextProvider;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseOrderDO;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpPurchaseSuggestMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseOrderMapper;
import cn.iocoder.yudao.module.erp.enums.ErpPurchaseOrderBpmConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PURCHASE_ORDER_NOT_EXISTS;

@Component
public class PurchaseOrderContextProvider implements ApprovalContextProvider {

    @Resource
    private ErpPurchaseOrderMapper purchaseOrderMapper;
    @Autowired(required = false)
    private ErpPurchaseSuggestMapper purchaseSuggestMapper;

    @Override
    public String getSceneCode() {
        return "erp.purchase.order.submit";
    }

    @Override
    public ApprovalContext getContext(Long bizId) {
        ErpPurchaseOrderDO order = purchaseOrderMapper.selectById(bizId);
        if (order == null) {
            throw exception(PURCHASE_ORDER_NOT_EXISTS);
        }
        Map<String, Object> variables = new HashMap<>();
        variables.put(ErpPurchaseOrderBpmConstants.VARIABLE_ORDER_ID, order.getId());
        variables.put(ErpPurchaseOrderBpmConstants.VARIABLE_ORDER_NO, order.getNo());
        variables.put(ErpPurchaseOrderBpmConstants.VARIABLE_TOTAL_PRICE, order.getTotalPrice());
        variables.put(ErpPurchaseOrderBpmConstants.VARIABLE_SUPPLIER_ID, order.getSupplierId());
        variables.put(ErpPurchaseOrderBpmConstants.VARIABLE_SOURCE_TYPE, resolveSourceType(order.getId()));
        variables.put(ErpPurchaseOrderBpmConstants.VARIABLE_CREATOR_ID, parseCreatorId(order.getCreator()));
        Map<String, Object> notifyParams = new HashMap<>();
        notifyParams.put("bizTitle", "采购订单 " + order.getNo());
        notifyParams.put("bizNo", order.getNo());
        notifyParams.put("amount", order.getTotalPrice());
        return ApprovalContext.builder()
                .bizId(order.getId())
                .bizNo(order.getNo())
                .bizTitle("采购订单 " + order.getNo())
                .amount(order.getTotalPrice())
                .startUserId(parseCreatorId(order.getCreator()))
                .variables(variables)
                .notifyParams(notifyParams)
                .build();
    }

    private String resolveSourceType(Long purchaseOrderId) {
        if (purchaseSuggestMapper == null) {
            return ErpPurchaseOrderBpmConstants.SOURCE_TYPE_MANUAL;
        }
        List<?> purchaseSuggests = purchaseSuggestMapper.selectListByConvertPurchaseOrderIds(List.of(purchaseOrderId));
        return purchaseSuggests == null || purchaseSuggests.isEmpty()
                ? ErpPurchaseOrderBpmConstants.SOURCE_TYPE_MANUAL
                : ErpPurchaseOrderBpmConstants.SOURCE_TYPE_MRP;
    }

    private Long parseCreatorId(String creator) {
        return creator != null && creator.matches("\\d+") ? Long.valueOf(creator) : null;
    }
}
