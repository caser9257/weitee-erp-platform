package cn.iocoder.yudao.module.erp.service.purchase;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.bpm.service.approval.BpmApprovalRuntimeService;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInCancelApprovalReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInSubmitReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInDO;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseInMapper;
import cn.iocoder.yudao.module.erp.enums.ErpAuditStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.APPROVAL_INSTANCE_NOT_PROCESSING;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PURCHASE_IN_APPROVE_FAIL;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PURCHASE_IN_BPM_CANCEL_FAIL;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PURCHASE_IN_BPM_SUBMIT_FAIL;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PURCHASE_IN_NOT_EXISTS;

@Service
@Validated
public class ErpPurchaseInBpmServiceImpl implements ErpPurchaseInBpmService {

    private static final String SCENE_CODE = "erp.purchase.in.submit";

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
        approvalRuntimeService.submit(SCENE_CODE, purchaseIn.getId(), userId);
        erpPurchaseInMapper.updateById(new ErpPurchaseInDO()
                .setId(purchaseIn.getId())
                .setStatus(ErpAuditStatus.PROCESS.getStatus()));
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
        try {
            approvalRuntimeService.cancel(SCENE_CODE, purchaseIn.getId(), userId, reqVO.getReason());
        } catch (ServiceException ex) {
            if (!ObjectUtil.equal(ex.getCode(), APPROVAL_INSTANCE_NOT_PROCESSING.getCode())) {
                throw ex;
            }
            // 流程已结束，清理绑定
            erpPurchaseInMapper.clearProcessInstanceId(purchaseIn.getId());
        }
    }

    @Override
    public void handleProcessInstanceResult(Long purchaseInId, String processInstanceId, Integer status, String reason) {
        // 旧监听器入口保留兼容，结果回写已收敛到 PurchaseInResultHandler
    }

    private ErpPurchaseInDO getRequiredPurchaseIn(Long purchaseInId) {
        ErpPurchaseInDO purchaseIn = erpPurchaseInMapper.selectById(purchaseInId);
        if (purchaseIn == null) {
            throw exception(PURCHASE_IN_NOT_EXISTS);
        }
        return purchaseIn;
    }

}
