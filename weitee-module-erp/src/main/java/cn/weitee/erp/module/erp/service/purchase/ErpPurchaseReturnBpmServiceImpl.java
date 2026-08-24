package cn.weitee.erp.module.erp.service.purchase;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.weitee.erp.module.bpm.service.approval.BpmApprovalRuntimeService;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.returns.ErpPurchaseReturnCancelApprovalReqVO;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.returns.ErpPurchaseReturnSubmitReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseReturnDO;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseReturnMapper;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.enums.ErpPurchaseReturnBpmConstants;
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
public class ErpPurchaseReturnBpmServiceImpl implements ErpPurchaseReturnBpmService {

    @Resource
    private ErpPurchaseReturnMapper erpPurchaseReturnMapper;
    @Resource
    private ErpPurchaseReturnService purchaseReturnService;

    @Resource
    private BpmApprovalRuntimeService approvalRuntimeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String submitPurchaseReturn(Long userId, ErpPurchaseReturnSubmitReqVO reqVO) {
        ErpPurchaseReturnDO purchaseReturn = getRequiredPurchaseReturn(reqVO.getId());
        if (ObjectUtil.equal(purchaseReturn.getStatus(), ErpAuditStatus.APPROVE.getStatus())) {
            throw exception(PURCHASE_RETURN_APPROVE_FAIL);
        }
        if (ObjectUtil.equal(purchaseReturn.getStatus(), ErpAuditStatus.PROCESS.getStatus())
                && StrUtil.isNotBlank(purchaseReturn.getProcessInstanceId())) {
            throw exception(PURCHASE_RETURN_BPM_SUBMIT_FAIL);
        }
        // 事务内：只写本地状态
        Long purchaseReturnId = purchaseReturn.getId();
        erpPurchaseReturnMapper.updateById(new ErpPurchaseReturnDO()
                .setId(purchaseReturnId)
                .setStatus(ErpAuditStatus.PROCESS.getStatus())
                .setProcessInstanceId(null));

        // 事务外：调 BPM 创建流程
        ErpTransactionUtils.afterCommit(() -> {
            try {
                String processInstanceId = approvalRuntimeService.submit(
                        ErpPurchaseReturnBpmConstants.SCENE_CODE, purchaseReturnId, userId);
                erpPurchaseReturnMapper.updateById(new ErpPurchaseReturnDO()
                        .setId(purchaseReturnId)
                        .setProcessInstanceId(processInstanceId));
            } catch (Exception e) {
                log.error("[submitPurchaseReturn] BPM 创建失败，purchaseReturnId={}", purchaseReturnId, e);
                erpPurchaseReturnMapper.updateById(new ErpPurchaseReturnDO()
                        .setId(purchaseReturnId)
                        .setStatus(ErpAuditStatus.FAILED.getStatus())
                        .setProcessInstanceId(null));
            }
        });
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelPurchaseReturnApproval(Long userId, ErpPurchaseReturnCancelApprovalReqVO reqVO) {
        ErpPurchaseReturnDO purchaseReturn = getRequiredPurchaseReturn(reqVO.getId());
        if (!ObjectUtil.equal(purchaseReturn.getStatus(), ErpAuditStatus.PROCESS.getStatus())
                || StrUtil.isBlank(purchaseReturn.getProcessInstanceId())) {
            throw exception(PURCHASE_RETURN_BPM_CANCEL_FAIL);
        }
        // 事务内：只做本地校验
        Long purchaseReturnId = purchaseReturn.getId();
        ErpTransactionUtils.afterCommit(() -> {
            try {
                approvalRuntimeService.cancel(ErpPurchaseReturnBpmConstants.SCENE_CODE,
                        purchaseReturnId, userId, reqVO.getReason());
                // 不在此处清理 processInstanceId，由 ResultHandler.onCancel() 统一处理状态回写
            } catch (Exception e) {
                log.warn("[cancelPurchaseReturnApproval] BPM 撤回失败，purchaseReturnId={}", purchaseReturnId, e);
                throw e;
            }
        });
    }

    @Override
    public void handleProcessInstanceResult(Long purchaseReturnId, String processInstanceId, Integer status, String reason) {
        // 结果回写已收敛到 PurchaseReturnResultHandler
    }

    private ErpPurchaseReturnDO getRequiredPurchaseReturn(Long purchaseReturnId) {
        ErpPurchaseReturnDO purchaseReturn = erpPurchaseReturnMapper.selectById(purchaseReturnId);
        if (purchaseReturn == null) {
            throw exception(PURCHASE_RETURN_NOT_EXISTS);
        }
        return purchaseReturn;
    }

}
