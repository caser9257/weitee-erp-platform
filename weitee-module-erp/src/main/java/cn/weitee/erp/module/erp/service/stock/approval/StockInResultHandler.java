package cn.weitee.erp.module.erp.service.stock.approval;

import cn.weitee.erp.module.bpm.service.approval.handler.ApprovalResultHandler;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockInDO;
import cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockInMapper;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.service.stock.ErpStockInService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.STOCK_IN_NOT_EXISTS;

@Component
public class StockInResultHandler implements ApprovalResultHandler {

    @Resource
    private ErpStockInMapper stockInMapper;
    @Resource
    private ErpStockInService stockInService;

    @Override
    public String getSceneCode() {
        return "erp.stock.in.submit";
    }

    @Override
    public void onApprove(Long bizId, String processInstanceId, String reason) {
        validateExists(bizId);
        stockInService.updateStockInStatusByBpm(bizId, processInstanceId,
                ErpAuditStatus.APPROVE.getStatus(), reason);
    }

    @Override
    public void onReject(Long bizId, String processInstanceId, String reason) {
        validateExists(bizId);
        stockInService.updateStockInStatusByBpm(bizId, processInstanceId,
                ErpAuditStatus.REJECT.getStatus(), reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onCancel(Long bizId, String processInstanceId, String reason) {
        // 撤回/驳回：状态回 DRAFT，清理 processInstanceId，无库存副作用
        stockInMapper.updateById(new ErpStockInDO()
                .setId(bizId).setStatus(ErpAuditStatus.DRAFT.getStatus()));
        stockInMapper.clearProcessInstanceId(bizId);
    }

    private void validateExists(Long stockInId) {
        if (stockInMapper.selectById(stockInId) == null) {
            throw exception(STOCK_IN_NOT_EXISTS);
        }
    }
}
