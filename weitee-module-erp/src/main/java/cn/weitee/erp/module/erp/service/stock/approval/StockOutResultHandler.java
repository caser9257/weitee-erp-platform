package cn.weitee.erp.module.erp.service.stock.approval;

import cn.weitee.erp.module.bpm.service.approval.handler.ApprovalResultHandler;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockOutDO;
import cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockOutMapper;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.service.stock.ErpStockOutService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.STOCK_OUT_NOT_EXISTS;

@Component
public class StockOutResultHandler implements ApprovalResultHandler {

    @Resource
    private ErpStockOutMapper stockOutMapper;
    @Resource
    private ErpStockOutService stockOutService;

    @Override
    public String getSceneCode() {
        return "erp.stock.out.submit";
    }

    @Override
    public void onApprove(Long bizId, String processInstanceId, String reason) {
        validateExists(bizId);
        stockOutService.updateStockOutStatusByBpm(bizId, processInstanceId,
                ErpAuditStatus.APPROVE.getStatus(), reason);
    }

    @Override
    public void onReject(Long bizId, String processInstanceId, String reason) {
        validateExists(bizId);
        stockOutService.updateStockOutStatusByBpm(bizId, processInstanceId,
                ErpAuditStatus.REJECT.getStatus(), reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onCancel(Long bizId, String processInstanceId, String reason) {
        // 撤回/驳回：状态回 DRAFT，清理 processInstanceId，无库存副作用
        stockOutMapper.updateById(new ErpStockOutDO()
                .setId(bizId).setStatus(ErpAuditStatus.DRAFT.getStatus()));
        stockOutMapper.clearProcessInstanceId(bizId);
    }

    private void validateExists(Long stockOutId) {
        if (stockOutMapper.selectById(stockOutId) == null) {
            throw exception(STOCK_OUT_NOT_EXISTS);
        }
    }
}