package cn.weitee.erp.module.erp.service.stock;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.weitee.erp.module.bpm.service.approval.BpmApprovalRuntimeService;
import cn.weitee.erp.module.erp.controller.admin.stock.vo.out.ErpStockOutCancelApprovalReqVO;
import cn.weitee.erp.module.erp.controller.admin.stock.vo.out.ErpStockOutSubmitReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockOutDO;
import cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockOutMapper;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.enums.ErpStockOutBpmConstants;
import cn.weitee.erp.module.erp.util.ErpTransactionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.*;

@Service
@Validated
@Slf4j
public class ErpStockOutBpmServiceImpl implements ErpStockOutBpmService {

    @Resource
    private ErpStockOutMapper erpStockOutMapper;
    @Resource
    private ErpStockOutService stockOutService;

    @Resource
    private BpmApprovalRuntimeService approvalRuntimeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String submitStockOut(Long userId, ErpStockOutSubmitReqVO reqVO) {
        ErpStockOutDO stockOut = getRequiredStockOut(reqVO.getId());
        if (ObjectUtil.equal(stockOut.getStatus(), ErpAuditStatus.APPROVE.getStatus())) {
            throw exception(STOCK_OUT_APPROVE_FAIL);
        }
        if (ObjectUtil.equal(stockOut.getStatus(), ErpAuditStatus.PROCESS.getStatus())
                && StrUtil.isNotBlank(stockOut.getProcessInstanceId())) {
            throw exception(STOCK_OUT_BPM_SUBMIT_FAIL);
        }
        // 事务内：只写本地状态
        Long stockOutId = stockOut.getId();
        int updated = erpStockOutMapper.updateByIdAndStatus(stockOutId, ErpAuditStatus.DRAFT.getStatus(), new ErpStockOutDO()
                .setId(stockOutId)
                .setStatus(ErpAuditStatus.PROCESS.getStatus())
                .setProcessInstanceId(null));
        if (updated == 0) {
            throw exception(STOCK_OUT_BPM_SUBMIT_FAIL);
        }

        // 事务外：调 BPM 创建流程
        ErpTransactionUtils.afterCommit(() -> {
            try {
                String processInstanceId = approvalRuntimeService.submit(
                        ErpStockOutBpmConstants.SCENE_CODE, stockOutId, userId);
                erpStockOutMapper.updateById(new ErpStockOutDO()
                        .setId(stockOutId)
                        .setProcessInstanceId(processInstanceId));
            } catch (Exception e) {
                log.error("[submitStockOut] BPM 创建失败，stockOutId={}", stockOutId, e);
                erpStockOutMapper.updateById(new ErpStockOutDO()
                        .setId(stockOutId)
                        .setStatus(ErpAuditStatus.FAILED.getStatus())
                        .setProcessInstanceId(null));
            }
        });
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelStockOutApproval(Long userId, ErpStockOutCancelApprovalReqVO reqVO) {
        ErpStockOutDO stockOut = getRequiredStockOut(reqVO.getId());
        if (!ObjectUtil.equal(stockOut.getStatus(), ErpAuditStatus.PROCESS.getStatus())
                || StrUtil.isBlank(stockOut.getProcessInstanceId())) {
            throw exception(STOCK_OUT_BPM_CANCEL_FAIL);
        }
        // 事务内：只做本地校验
        Long stockOutId = stockOut.getId();
        ErpTransactionUtils.afterCommit(() -> {
            try {
                approvalRuntimeService.cancel(ErpStockOutBpmConstants.SCENE_CODE,
                        stockOutId, userId, reqVO.getReason());
                // 不在此处清理 processInstanceId，由 ResultHandler.onCancel() 统一处理状态回写
            } catch (Exception e) {
                log.warn("[cancelStockOutApproval] BPM 撤回失败，stockOutId={}", stockOutId, e);
                throw e;
            }
        });
    }

    @Override
    public void handleProcessInstanceResult(Long stockOutId, String processInstanceId, Integer status, String reason) {
        // 结果回写已收敛到 StockOutResultHandler
    }

    private ErpStockOutDO getRequiredStockOut(Long stockOutId) {
        ErpStockOutDO stockOut = erpStockOutMapper.selectById(stockOutId);
        if (stockOut == null) {
            throw exception(STOCK_OUT_NOT_EXISTS);
        }
        return stockOut;
    }

}
