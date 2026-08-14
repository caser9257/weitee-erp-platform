package cn.weitee.erp.module.erp.service.stock.approval;

import cn.hutool.core.util.StrUtil;
import cn.weitee.erp.module.bpm.service.approval.provider.ApprovalContext;
import cn.weitee.erp.module.bpm.service.approval.provider.ApprovalContextProvider;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockMoveDO;
import cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockMoveMapper;
import cn.weitee.erp.module.erp.enums.ErpStockMoveBpmConstants;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.Map;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.STOCK_MOVE_NOT_EXISTS;

@Component
public class StockMoveContextProvider implements ApprovalContextProvider {

    @Resource
    private ErpStockMoveMapper stockMoveMapper;

    @Override
    public String getSceneCode() {
        return ErpStockMoveBpmConstants.SCENE_CODE;
    }

    @Override
    public ApprovalContext getContext(Long bizId) {
        ErpStockMoveDO stockMove = stockMoveMapper.selectById(bizId);
        if (stockMove == null) {
            throw exception(STOCK_MOVE_NOT_EXISTS);
        }
        return ApprovalContext.builder().bizId(stockMove.getId()).bizNo(stockMove.getNo())
                .bizTitle("库存调拨单" + stockMove.getNo()).amount(stockMove.getTotalPrice())
                .startUserId(StrUtil.isNumeric(stockMove.getCreator()) ? Long.valueOf(stockMove.getCreator()) : null)
                .variables(Map.of("stockMoveId", stockMove.getId(), "stockMoveNo", stockMove.getNo(),
                        "totalPrice", stockMove.getTotalPrice()))
                .notifyParams(Map.of("bizTitle", "库存调拨单" + stockMove.getNo(), "bizNo", stockMove.getNo(),
                        "amount", stockMove.getTotalPrice())).build();
    }
}
