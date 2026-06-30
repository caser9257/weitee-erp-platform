package cn.weitee.erp.module.erp.service.stock.approval;

import cn.hutool.core.util.StrUtil;
import cn.weitee.erp.module.bpm.service.approval.provider.ApprovalContext;
import cn.weitee.erp.module.bpm.service.approval.provider.ApprovalContextProvider;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockInDO;
import cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockInMapper;
import cn.weitee.erp.module.erp.enums.ErpStockInBpmConstants;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.STOCK_IN_NOT_EXISTS;

@Component
public class StockInContextProvider implements ApprovalContextProvider {

    @Resource
    private ErpStockInMapper stockInMapper;

    @Override
    public String getSceneCode() {
        return ErpStockInBpmConstants.SCENE_CODE;
    }

    @Override
    public ApprovalContext getContext(Long bizId) {
        ErpStockInDO stockIn = stockInMapper.selectById(bizId);
        if (stockIn == null) {
            throw exception(STOCK_IN_NOT_EXISTS);
        }
        Map<String, Object> variables = new HashMap<>();
        variables.put(ErpStockInBpmConstants.VARIABLE_STOCK_IN_ID, stockIn.getId());
        variables.put(ErpStockInBpmConstants.VARIABLE_STOCK_IN_NO, stockIn.getNo());
        variables.put(ErpStockInBpmConstants.VARIABLE_TOTAL_PRICE, stockIn.getTotalPrice());
        variables.put(ErpStockInBpmConstants.VARIABLE_SUPPLIER_ID, stockIn.getSupplierId());
        Map<String, Object> notifyParams = new HashMap<>();
        notifyParams.put("bizTitle", "其它入库单 " + stockIn.getNo());
        notifyParams.put("bizNo", stockIn.getNo());
        notifyParams.put("amount", stockIn.getTotalPrice());
        return ApprovalContext.builder()
                .bizId(stockIn.getId())
                .bizNo(stockIn.getNo())
                .bizTitle("其它入库单 " + stockIn.getNo())
                .amount(stockIn.getTotalPrice())
                .startUserId(parseCreatorId(stockIn.getCreator()))
                .variables(variables)
                .notifyParams(notifyParams)
                .build();
    }

    private Long parseCreatorId(String creator) {
        return creator != null && StrUtil.isNumeric(creator) ? Long.valueOf(creator) : null;
    }
}
