package cn.iocoder.yudao.module.erp.service.purchase;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.bpm.service.approval.BpmApprovalRuntimeService;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInCancelApprovalReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInSubmitReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInDO;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseInMapper;
import cn.iocoder.yudao.module.erp.enums.ErpAuditStatus;
import cn.iocoder.yudao.module.erp.util.ErpTransactionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PURCHASE_IN_APPROVE_FAIL;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PURCHASE_IN_BPM_CANCEL_FAIL;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PURCHASE_IN_BPM_SUBMIT_FAIL;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PURCHASE_IN_NOT_EXISTS;

@Service
@Validated
@Slf4j
public class ErpPurchaseInBpmServiceImpl implements ErpPurchaseInBpmService {

    @Resource
    private ErpPurchaseInMapper erpPurchaseInMapper;
    @Resource
    private ErpPurchaseInService purchaseInService;

    @Resource
    private BpmApprovalRuntimeService approvalRuntimeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String submitPurchaseIn(Long userId, ErpPurchaseInSubmitReqVO reqVO) {
        ErpPurchaseInDO purchaseIn = getRequiredPurchaseIn(reqVO.getId());
        if (ObjectUtil.equal(purchaseIn.getStatus(), ErpAuditStatus.APPROVE.getStatus())) {
            throw exception(PURCHASE_IN_APPROVE_FAIL);
        }
        if (ObjectUtil.equal(purchaseIn.getStatus(), ErpAuditStatus.PROCESS.getStatus())
                && StrUtil.isNotBlank(purchaseIn.getProcessInstanceId())) {
            throw exception(PURCHASE_IN_BPM_SUBMIT_FAIL);
        }
        // 事务内：只写本地状态
        Long purchaseInId = purchaseIn.getId();
        erpPurchaseInMapper.updateById(new ErpPurchaseInDO()
                .setId(purchaseInId)
                .setStatus(ErpAuditStatus.PROCESS.getStatus())
                .setProcessInstanceId(null));

        // 事务外：调 BPM 创建流程
        ErpTransactionUtils.afterCommit(() -> {
            try {
                String processInstanceId = approvalRuntimeService.submit(
                        "erp.purchase.in.submit", purchaseInId, userId);
                erpPurchaseInMapper.updateById(new ErpPurchaseInDO()
                        .setId(purchaseInId)
                        .setProcessInstanceId(processInstanceId));
            } catch (Exception e) {
                log.error("[submitPurchaseIn] BPM 创建失败，purchaseInId={}", purchaseInId, e);
                erpPurchaseInMapper.updateById(new ErpPurchaseInDO()
                        .setId(purchaseInId)
                        .setStatus(ErpAuditStatus.FAILED.getStatus())
                        .setProcessInstanceId(null));
            }
        });
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelPurchaseInApproval(Long userId, ErpPurchaseInCancelApprovalReqVO reqVO) {
        ErpPurchaseInDO purchaseIn = getRequiredPurchaseIn(reqVO.getId());
        if (!ObjectUtil.equal(purchaseIn.getStatus(), ErpAuditStatus.PROCESS.getStatus())
                || StrUtil.isBlank(purchaseIn.getProcessInstanceId())) {
            throw exception(PURCHASE_IN_BPM_CANCEL_FAIL);
        }
        // 事务内：只做本地校验
        Long purchaseInId = purchaseIn.getId();
        ErpTransactionUtils.afterCommit(() -> {
            try {
                approvalRuntimeService.cancel("erp.purchase.in.submit", purchaseInId, userId, reqVO.getReason());
                erpPurchaseInMapper.clearProcessInstanceId(purchaseInId);
            } catch (Exception e) {
                log.warn("[cancelPurchaseInApproval] BPM 撤回失败，purchaseInId={}", purchaseInId, e);
            }
        });
    }

    @Override
    public void handleProcessInstanceResult(Long purchaseInId, String processInstanceId, Integer status, String reason) {
        // 结果回写已收敛到 PurchaseInResultHandler
    }

    private ErpPurchaseInDO getRequiredPurchaseIn(Long purchaseInId) {
        ErpPurchaseInDO purchaseIn = erpPurchaseInMapper.selectById(purchaseInId);
        if (purchaseIn == null) {
            throw exception(PURCHASE_IN_NOT_EXISTS);
        }
        return purchaseIn;
    }

}
