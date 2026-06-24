package cn.iocoder.yudao.module.erp.service.purchase.approval;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.bpm.service.approval.provider.ApprovalContext;
import cn.iocoder.yudao.module.bpm.service.approval.provider.ApprovalContextProvider;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInDO;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseInMapper;
import cn.iocoder.yudao.module.erp.enums.ErpPurchaseInBpmConstants;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PURCHASE_IN_NOT_EXISTS;

@Component
public class PurchaseInContextProvider implements ApprovalContextProvider {

    @Resource
    private ErpPurchaseInMapper purchaseInMapper;

    @Override
    public String getSceneCode() {
        return "erp.purchase.in.submit";
    }

    @Override
    public ApprovalContext getContext(Long bizId) {
        ErpPurchaseInDO purchaseIn = purchaseInMapper.selectById(bizId);
        if (purchaseIn == null) {
            throw exception(PURCHASE_IN_NOT_EXISTS);
        }
        Map<String, Object> variables = new HashMap<>();
        variables.put(ErpPurchaseInBpmConstants.VARIABLE_IN_ID, purchaseIn.getId());
        variables.put(ErpPurchaseInBpmConstants.VARIABLE_IN_NO, purchaseIn.getNo());
        variables.put(ErpPurchaseInBpmConstants.VARIABLE_TOTAL_PRICE, purchaseIn.getTotalPrice());
        variables.put(ErpPurchaseInBpmConstants.VARIABLE_SUPPLIER_ID, purchaseIn.getSupplierId());
        variables.put(ErpPurchaseInBpmConstants.VARIABLE_ORDER_ID, purchaseIn.getOrderId());
        variables.put(ErpPurchaseInBpmConstants.VARIABLE_CREATOR_ID, parseCreatorId(purchaseIn.getCreator()));
        Map<String, Object> notifyParams = new HashMap<>();
        notifyParams.put("bizTitle", "采购入库单 " + purchaseIn.getNo());
        notifyParams.put("bizNo", purchaseIn.getNo());
        notifyParams.put("amount", purchaseIn.getTotalPrice());
        return ApprovalContext.builder()
                .bizId(purchaseIn.getId())
                .bizNo(purchaseIn.getNo())
                .bizTitle("采购入库单 " + purchaseIn.getNo())
                .amount(purchaseIn.getTotalPrice())
                .startUserId(parseCreatorId(purchaseIn.getCreator()))
                .variables(variables)
                .notifyParams(notifyParams)
                .build();
    }

    private Long parseCreatorId(String creator) {
        return creator != null && StrUtil.isNumeric(creator) ? Long.valueOf(creator) : null;
    }
}
