package cn.weitee.erp.module.erp.service.purchase.approval;

import cn.hutool.core.util.StrUtil;
import cn.weitee.erp.module.bpm.service.approval.provider.ApprovalContext;
import cn.weitee.erp.module.bpm.service.approval.provider.ApprovalContextProvider;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseReturnDO;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseReturnMapper;
import cn.weitee.erp.module.erp.enums.ErpPurchaseReturnBpmConstants;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PURCHASE_RETURN_NOT_EXISTS;

@Component
public class PurchaseReturnContextProvider implements ApprovalContextProvider {

    @Resource
    private ErpPurchaseReturnMapper purchaseReturnMapper;

    @Override
    public String getSceneCode() {
        return ErpPurchaseReturnBpmConstants.SCENE_CODE;
    }

    @Override
    public ApprovalContext getContext(Long bizId) {
        ErpPurchaseReturnDO purchaseReturn = purchaseReturnMapper.selectById(bizId);
        if (purchaseReturn == null) {
            throw exception(PURCHASE_RETURN_NOT_EXISTS);
        }
        Map<String, Object> variables = new HashMap<>();
        variables.put(ErpPurchaseReturnBpmConstants.VARIABLE_RETURN_ID, purchaseReturn.getId());
        variables.put(ErpPurchaseReturnBpmConstants.VARIABLE_RETURN_NO, purchaseReturn.getNo());
        variables.put(ErpPurchaseReturnBpmConstants.VARIABLE_TOTAL_PRICE, purchaseReturn.getTotalPrice());
        variables.put(ErpPurchaseReturnBpmConstants.VARIABLE_SUPPLIER_ID, purchaseReturn.getSupplierId());
        Map<String, Object> notifyParams = new HashMap<>();
        notifyParams.put("bizTitle", "采购退货单 " + purchaseReturn.getNo());
        notifyParams.put("bizNo", purchaseReturn.getNo());
        notifyParams.put("amount", purchaseReturn.getTotalPrice());
        return ApprovalContext.builder()
                .bizId(purchaseReturn.getId())
                .bizNo(purchaseReturn.getNo())
                .bizTitle("采购退货单 " + purchaseReturn.getNo())
                .amount(purchaseReturn.getTotalPrice())
                .startUserId(parseCreatorId(purchaseReturn.getCreator()))
                .variables(variables)
                .notifyParams(notifyParams)
                .build();
    }

    private Long parseCreatorId(String creator) {
        return creator != null && StrUtil.isNumeric(creator) ? Long.valueOf(creator) : null;
    }
}
