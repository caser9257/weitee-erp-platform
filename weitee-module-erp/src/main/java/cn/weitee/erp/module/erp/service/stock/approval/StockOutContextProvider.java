package cn.weitee.erp.module.erp.service.stock.approval;

import cn.hutool.core.util.StrUtil;
import cn.weitee.erp.module.bpm.service.approval.provider.ApprovalContext;
import cn.weitee.erp.module.bpm.service.approval.provider.ApprovalContextProvider;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockOutDO;
import cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockOutMapper;
import cn.weitee.erp.module.erp.enums.ErpStockOutBpmConstants;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.STOCK_OUT_NOT_EXISTS;

@Component
public class StockOutContextProvider implements ApprovalContextProvider {

    @Resource
    private ErpStockOutMapper stockOutMapper;

    @Override
    public String getSceneCode() {
        return ErpStockOutBpmConstants.SCENE_CODE;
    }

    @Override
    public ApprovalContext getContext(Long bizId) {
        ErpStockOutDO stockOut = stockOutMapper.selectById(bizId);
        if (stockOut == null) {
            throw exception(STOCK_OUT_NOT_EXISTS);
        }
        Map<String, Object> variables = new HashMap<>();
        variables.put(ErpStockOutBpmConstants.VARIABLE_STOCK_OUT_ID, stockOut.getId());
        variables.put(ErpStockOutBpmConstants.VARIABLE_STOCK_OUT_NO, stockOut.getNo());
        variables.put(ErpStockOutBpmConstants.VARIABLE_TOTAL_PRICE, stockOut.getTotalPrice());
        variables.put(ErpStockOutBpmConstants.VARIABLE_CUSTOMER_ID, stockOut.getCustomerId());
        Map<String, Object> notifyParams = new HashMap<>();
        notifyParams.put("bizTitle", "其它出库单 " + stockOut.getNo());
        notifyParams.put("bizNo", stockOut.getNo());
        notifyParams.put("amount", stockOut.getTotalPrice());
        return ApprovalContext.builder()
                .bizId(stockOut.getId())
                .bizNo(stockOut.getNo())
                .bizTitle("其它出库单 " + stockOut.getNo())
                .amount(stockOut.getTotalPrice())
                .startUserId(parseCreatorId(stockOut.getCreator()))
                .variables(variables)
                .notifyParams(notifyParams)
                .build();
    }

    private Long parseCreatorId(String creator) {
        return creator != null && StrUtil.isNumeric(creator) ? Long.valueOf(creator) : null;
    }
}