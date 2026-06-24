package cn.iocoder.yudao.module.erp.service.sale.approval;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.bpm.service.approval.provider.ApprovalContext;
import cn.iocoder.yudao.module.bpm.service.approval.provider.ApprovalContextProvider;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOrderDO;
import cn.iocoder.yudao.module.erp.dal.mysql.sale.ErpSaleOrderMapper;
import cn.iocoder.yudao.module.erp.enums.ErpSaleOrderBpmConstants;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.SALE_ORDER_NOT_EXISTS;

@Component
public class SaleOrderContextProvider implements ApprovalContextProvider {

    @Resource
    private ErpSaleOrderMapper saleOrderMapper;

    @Override
    public String getSceneCode() {
        return "erp.sale.order.submit";
    }

    @Override
    public ApprovalContext getContext(Long bizId) {
        ErpSaleOrderDO order = saleOrderMapper.selectById(bizId);
        if (order == null) {
            throw exception(SALE_ORDER_NOT_EXISTS);
        }
        Map<String, Object> variables = new HashMap<>();
        variables.put(ErpSaleOrderBpmConstants.VARIABLE_ORDER_ID, order.getId());
        variables.put(ErpSaleOrderBpmConstants.VARIABLE_ORDER_NO, order.getNo());
        variables.put(ErpSaleOrderBpmConstants.VARIABLE_TOTAL_PRICE, order.getTotalPrice());
        variables.put(ErpSaleOrderBpmConstants.VARIABLE_CUSTOMER_ID, order.getCustomerId());
        variables.put(ErpSaleOrderBpmConstants.VARIABLE_PROJECT_ID, order.getProjectId());
        variables.put(ErpSaleOrderBpmConstants.VARIABLE_BUSINESS_TYPE, order.getBusinessType());
        Map<String, Object> notifyParams = new HashMap<>();
        notifyParams.put("bizTitle", "销售订单 " + order.getNo());
        notifyParams.put("bizNo", order.getNo());
        notifyParams.put("amount", order.getTotalPrice());
        return ApprovalContext.builder()
                .bizId(order.getId())
                .bizNo(order.getNo())
                .bizTitle("销售订单 " + order.getNo())
                .amount(order.getTotalPrice())
                .startUserId(parseCreatorId(order.getCreator()))
                .variables(variables)
                .notifyParams(notifyParams)
                .build();
    }

    private Long parseCreatorId(String creator) {
        return creator != null && StrUtil.isNumeric(creator) ? Long.valueOf(creator) : null;
    }
}
