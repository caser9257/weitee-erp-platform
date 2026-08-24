package cn.weitee.erp.module.erp.service.stock;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.weitee.erp.module.bpm.service.approval.BpmApprovalRuntimeService;
import cn.weitee.erp.module.erp.controller.admin.stock.vo.move.ErpStockMoveCancelApprovalReqVO;
import cn.weitee.erp.module.erp.controller.admin.stock.vo.move.ErpStockMoveSubmitReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockMoveDO;
import cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockMoveMapper;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.enums.ErpStockMoveBpmConstants;
import cn.weitee.erp.module.erp.util.ErpTransactionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.STOCK_MOVE_NOT_EXISTS;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.STOCK_MOVE_PROCESS_FAIL;

@Service
@Slf4j
public class ErpStockMoveBpmServiceImpl implements ErpStockMoveBpmService {

    @Resource
    private ErpStockMoveMapper stockMoveMapper;
    @Resource
    private BpmApprovalRuntimeService approvalRuntimeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitStockMove(Long userId, ErpStockMoveSubmitReqVO reqVO) {
        ErpStockMoveDO stockMove = getRequiredStockMove(reqVO.getId());
        if (ObjectUtil.equal(stockMove.getStatus(), ErpAuditStatus.APPROVE.getStatus())
                || (ObjectUtil.equal(stockMove.getStatus(), ErpAuditStatus.PROCESS.getStatus())
                && StrUtil.isNotBlank(stockMove.getProcessInstanceId()))) {
            throw exception(STOCK_MOVE_PROCESS_FAIL);
        }
        Long stockMoveId = stockMove.getId();
        stockMoveMapper.updateById(new ErpStockMoveDO().setId(stockMoveId)
                .setStatus(ErpAuditStatus.PROCESS.getStatus()).setProcessInstanceId(null));
        ErpTransactionUtils.afterCommit(() -> {
            try {
                String processInstanceId = approvalRuntimeService.submit(ErpStockMoveBpmConstants.SCENE_CODE, stockMoveId, userId);
                stockMoveMapper.updateById(new ErpStockMoveDO().setId(stockMoveId).setProcessInstanceId(processInstanceId));
            } catch (Exception e) {
                log.error("[submitStockMove] BPM 创建失败，stockMoveId={}", stockMoveId, e);
                stockMoveMapper.updateById(new ErpStockMoveDO().setId(stockMoveId)
                        .setStatus(ErpAuditStatus.FAILED.getStatus()).setProcessInstanceId(null));
            }
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelStockMoveApproval(Long userId, ErpStockMoveCancelApprovalReqVO reqVO) {
        ErpStockMoveDO stockMove = getRequiredStockMove(reqVO.getId());
        if (!ObjectUtil.equal(stockMove.getStatus(), ErpAuditStatus.PROCESS.getStatus())
                || StrUtil.isBlank(stockMove.getProcessInstanceId())) {
            throw exception(STOCK_MOVE_PROCESS_FAIL);
        }
        Long stockMoveId = stockMove.getId();
        ErpTransactionUtils.afterCommit(() -> approvalRuntimeService.cancel(
                ErpStockMoveBpmConstants.SCENE_CODE, stockMoveId, userId, reqVO.getReason()));
    }

    private ErpStockMoveDO getRequiredStockMove(Long id) {
        ErpStockMoveDO stockMove = stockMoveMapper.selectById(id);
        if (stockMove == null) {
            throw exception(STOCK_MOVE_NOT_EXISTS);
        }
        return stockMove;
    }
}
