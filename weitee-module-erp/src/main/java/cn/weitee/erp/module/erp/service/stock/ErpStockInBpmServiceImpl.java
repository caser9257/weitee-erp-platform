package cn.weitee.erp.module.erp.service.stock;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.weitee.erp.module.bpm.service.approval.BpmApprovalRuntimeService;
import cn.weitee.erp.module.erp.controller.admin.stock.vo.in.ErpStockInCancelApprovalReqVO;
import cn.weitee.erp.module.erp.controller.admin.stock.vo.in.ErpStockInSubmitReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockInDO;
import cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockInMapper;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.enums.ErpStockInBpmConstants;
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
public class ErpStockInBpmServiceImpl implements ErpStockInBpmService {

    @Resource
    private ErpStockInMapper erpStockInMapper;
    @Resource
    private ErpStockInService stockInService;

    @Resource
    private BpmApprovalRuntimeService approvalRuntimeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String submitStockIn(Long userId, ErpStockInSubmitReqVO reqVO) {
        ErpStockInDO stockIn = getRequiredStockIn(reqVO.getId());
        if (ObjectUtil.equal(stockIn.getStatus(), ErpAuditStatus.APPROVE.getStatus())) {
            throw exception(STOCK_IN_APPROVE_FAIL);
        }
        if (ObjectUtil.equal(stockIn.getStatus(), ErpAuditStatus.PROCESS.getStatus())
                && StrUtil.isNotBlank(stockIn.getProcessInstanceId())) {
            throw exception(STOCK_IN_BPM_SUBMIT_FAIL);
        }
        // 事务内：只写本地状态
        Long stockInId = stockIn.getId();
        erpStockInMapper.updateById(new ErpStockInDO()
                .setId(stockInId)
                .setStatus(ErpAuditStatus.PROCESS.getStatus())
                .setProcessInstanceId(null));

        // 事务外：调 BPM 创建流程
        ErpTransactionUtils.afterCommit(() -> {
            try {
                String processInstanceId = approvalRuntimeService.submit(
                        ErpStockInBpmConstants.SCENE_CODE, stockInId, userId);
                erpStockInMapper.updateById(new ErpStockInDO()
                        .setId(stockInId)
                        .setProcessInstanceId(processInstanceId));
            } catch (Exception e) {
                log.error("[submitStockIn] BPM 创建失败，stockInId={}", stockInId, e);
                erpStockInMapper.updateById(new ErpStockInDO()
                        .setId(stockInId)
                        .setStatus(ErpAuditStatus.FAILED.getStatus())
                        .setProcessInstanceId(null));
            }
        });
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelStockInApproval(Long userId, ErpStockInCancelApprovalReqVO reqVO) {
        ErpStockInDO stockIn = getRequiredStockIn(reqVO.getId());
        if (!ObjectUtil.equal(stockIn.getStatus(), ErpAuditStatus.PROCESS.getStatus())
                || StrUtil.isBlank(stockIn.getProcessInstanceId())) {
            throw exception(STOCK_IN_BPM_CANCEL_FAIL);
        }
        // 事务内：只做本地校验
        Long stockInId = stockIn.getId();
        ErpTransactionUtils.afterCommit(() -> {
            try {
                approvalRuntimeService.cancel(ErpStockInBpmConstants.SCENE_CODE,
                        stockInId, userId, reqVO.getReason());
                // 不在此处清理 processInstanceId，由 ResultHandler.onCancel() 统一处理状态回写
            } catch (Exception e) {
                log.warn("[cancelStockInApproval] BPM 撤回失败，stockInId={}", stockInId, e);
            }
        });
    }

    @Override
    public void handleProcessInstanceResult(Long stockInId, String processInstanceId, Integer status, String reason) {
        // 结果回写已收敛到 StockInResultHandler
    }

    private ErpStockInDO getRequiredStockIn(Long stockInId) {
        ErpStockInDO stockIn = erpStockInMapper.selectById(stockInId);
        if (stockIn == null) {
            throw exception(STOCK_IN_NOT_EXISTS);
        }
        return stockIn;
    }

}
