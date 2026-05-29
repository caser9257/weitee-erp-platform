package cn.iocoder.yudao.module.erp.service.purchase;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.bpm.api.task.BpmProcessInstanceApi;
import cn.iocoder.yudao.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmProcessInstanceCancelReqVO;
import cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceService;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInCancelApprovalReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInSubmitReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInDO;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseInMapper;
import cn.iocoder.yudao.module.erp.enums.ErpAuditStatus;
import cn.iocoder.yudao.module.erp.enums.ErpPurchaseInBpmConstants;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PURCHASE_IN_APPROVE_FAIL;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PURCHASE_IN_BPM_CANCEL_FAIL;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PURCHASE_IN_BPM_SUBMIT_FAIL;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PURCHASE_IN_NOT_EXISTS;

@Service
@Validated
public class ErpPurchaseInBpmServiceImpl implements ErpPurchaseInBpmService {

    @Resource
    private ErpPurchaseInMapper erpPurchaseInMapper;
    @Resource
    private ErpPurchaseInService purchaseInService;

    @Resource
    private BpmProcessInstanceApi processInstanceApi;
    @Resource
    private BpmProcessInstanceService processInstanceService;

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
        String processInstanceId = processInstanceApi.createProcessInstance(userId,
                new BpmProcessInstanceCreateReqDTO()
                        .setProcessDefinitionKey(ErpPurchaseInBpmConstants.PROCESS_DEFINITION_KEY)
                        .setBusinessKey(String.valueOf(purchaseIn.getId()))
                        .setVariables(buildVariables(purchaseIn))
                        .setStartUserSelectAssignees(reqVO.getStartUserSelectAssignees()));
        erpPurchaseInMapper.updateById(new ErpPurchaseInDO()
                .setId(purchaseIn.getId())
                .setStatus(ErpAuditStatus.PROCESS.getStatus())
                .setProcessInstanceId(processInstanceId));
        return processInstanceId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelPurchaseInApproval(Long userId, ErpPurchaseInCancelApprovalReqVO reqVO) {
        ErpPurchaseInDO purchaseIn = getRequiredPurchaseIn(reqVO.getId());
        if (!ObjectUtil.equal(purchaseIn.getStatus(), ErpAuditStatus.PROCESS.getStatus())
                || StrUtil.isBlank(purchaseIn.getProcessInstanceId())) {
            throw exception(PURCHASE_IN_BPM_CANCEL_FAIL);
        }
        processInstanceService.cancelProcessInstanceByStartUser(userId,
                new BpmProcessInstanceCancelReqVO()
                        .setId(purchaseIn.getProcessInstanceId())
                        .setReason(reqVO.getReason()));
        erpPurchaseInMapper.clearProcessInstanceId(purchaseIn.getId());
    }

    @Override
    public void handleProcessInstanceResult(Long purchaseInId, String processInstanceId, Integer status, String reason) {
        ErpPurchaseInDO purchaseIn = getRequiredPurchaseIn(purchaseInId);
        if (!StrUtil.equals(processInstanceId, purchaseIn.getProcessInstanceId())) {
            return;
        }
        if (ObjectUtil.equal(status, BpmProcessInstanceStatusEnum.APPROVE.getStatus())) {
            purchaseInService.updatePurchaseInStatusByBpm(purchaseInId, processInstanceId,
                    ErpAuditStatus.APPROVE.getStatus(), reason);
            return;
        }
        if (ObjectUtil.equal(status, BpmProcessInstanceStatusEnum.REJECT.getStatus())) {
            purchaseInService.updatePurchaseInStatusByBpm(purchaseInId, processInstanceId,
                    ErpAuditStatus.REJECT.getStatus(), reason);
            return;
        }
        if (ObjectUtil.equal(status, BpmProcessInstanceStatusEnum.CANCEL.getStatus())) {
            erpPurchaseInMapper.clearProcessInstanceId(purchaseInId);
        }
    }

    private Map<String, Object> buildVariables(ErpPurchaseInDO purchaseIn) {
        Map<String, Object> variables = new HashMap<>();
        variables.put(ErpPurchaseInBpmConstants.VARIABLE_IN_ID, purchaseIn.getId());
        variables.put(ErpPurchaseInBpmConstants.VARIABLE_IN_NO, purchaseIn.getNo());
        variables.put(ErpPurchaseInBpmConstants.VARIABLE_TOTAL_PRICE, purchaseIn.getTotalPrice());
        variables.put(ErpPurchaseInBpmConstants.VARIABLE_SUPPLIER_ID, purchaseIn.getSupplierId());
        variables.put(ErpPurchaseInBpmConstants.VARIABLE_ORDER_ID, purchaseIn.getOrderId());
        variables.put(ErpPurchaseInBpmConstants.VARIABLE_CREATOR_ID, parseCreatorId(purchaseIn.getCreator()));
        return variables;
    }

    private Long parseCreatorId(String creator) {
        return StrUtil.isNumeric(creator) ? Long.valueOf(creator) : null;
    }

    private ErpPurchaseInDO getRequiredPurchaseIn(Long purchaseInId) {
        ErpPurchaseInDO purchaseIn = erpPurchaseInMapper.selectById(purchaseInId);
        if (purchaseIn == null) {
            throw exception(PURCHASE_IN_NOT_EXISTS);
        }
        return purchaseIn;
    }

}
