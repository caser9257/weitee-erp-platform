package cn.iocoder.yudao.module.erp.service.sale;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.bpm.api.task.BpmProcessInstanceApi;
import cn.iocoder.yudao.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmProcessInstanceCancelReqVO;
import cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.FlowableUtils;
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceService;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.order.ErpSaleOrderCancelApprovalReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.order.ErpSaleOrderSubmitReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOrderAuditLogDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOrderDO;
import cn.iocoder.yudao.module.erp.dal.mysql.sale.ErpSaleOrderAuditLogMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.sale.ErpSaleOrderMapper;
import cn.iocoder.yudao.module.erp.enums.ErpAuditStatus;
import cn.iocoder.yudao.module.erp.enums.ErpSaleOrderAuditActionTypeConstants;
import cn.iocoder.yudao.module.erp.enums.ErpSaleOrderBpmConstants;
import org.flowable.engine.history.HistoricProcessInstance;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.PROCESS_INSTANCE_CANCEL_FAIL_NOT_EXISTS;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.SALE_ORDER_APPROVE_FAIL;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.SALE_ORDER_BPM_CANCEL_FAIL;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.SALE_ORDER_BPM_SUBMIT_FAIL;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.SALE_ORDER_NOT_EXISTS;

@Service
@Validated
public class ErpSaleOrderBpmServiceImpl implements ErpSaleOrderBpmService {

    @Resource
    private ErpSaleOrderMapper erpSaleOrderMapper;
    @Resource
    private ErpSaleOrderAuditLogMapper erpSaleOrderAuditLogMapper;
    @Resource
    private ErpSaleOrderService saleOrderService;

    @Resource
    private BpmProcessInstanceApi processInstanceApi;
    @Resource
    private BpmProcessInstanceService processInstanceService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String submitSaleOrder(Long userId, ErpSaleOrderSubmitReqVO reqVO) {
        ErpSaleOrderDO saleOrder = getRequiredSaleOrder(reqVO.getId());
        if (ObjectUtil.equal(saleOrder.getStatus(), ErpAuditStatus.APPROVE.getStatus())) {
            throw exception(SALE_ORDER_APPROVE_FAIL);
        }
        if (ObjectUtil.equal(saleOrder.getStatus(), ErpAuditStatus.PROCESS.getStatus())
                && StrUtil.isNotBlank(saleOrder.getProcessInstanceId())) {
            throw exception(SALE_ORDER_BPM_SUBMIT_FAIL);
        }
        String processInstanceId = processInstanceApi.createProcessInstance(userId,
                new BpmProcessInstanceCreateReqDTO()
                        .setProcessDefinitionKey(ErpSaleOrderBpmConstants.PROCESS_DEFINITION_KEY)
                        .setBusinessKey(String.valueOf(saleOrder.getId()))
                        .setVariables(buildVariables(saleOrder))
                        .setStartUserSelectAssignees(reqVO.getStartUserSelectAssignees()));
        erpSaleOrderMapper.updateById(new ErpSaleOrderDO()
                .setId(saleOrder.getId())
                .setStatus(ErpAuditStatus.PROCESS.getStatus())
                .setProcessInstanceId(processInstanceId));
        if (ObjectUtil.equal(saleOrder.getStatus(), ErpAuditStatus.REJECT.getStatus())) {
            erpSaleOrderAuditLogMapper.insert(new ErpSaleOrderAuditLogDO()
                    .setOrderId(saleOrder.getId())
                    .setActionType(ErpSaleOrderAuditActionTypeConstants.RESUBMIT)
                    .setBeforeStatus(ErpAuditStatus.REJECT.getStatus())
                    .setAfterStatus(ErpAuditStatus.PROCESS.getStatus()));
        }
        return processInstanceId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelSaleOrderApproval(Long userId, ErpSaleOrderCancelApprovalReqVO reqVO) {
        ErpSaleOrderDO saleOrder = getRequiredSaleOrder(reqVO.getId());
        if (!ObjectUtil.equal(saleOrder.getStatus(), ErpAuditStatus.PROCESS.getStatus())
                || StrUtil.isBlank(saleOrder.getProcessInstanceId())) {
            throw exception(SALE_ORDER_BPM_CANCEL_FAIL);
        }
        try {
            processInstanceService.cancelProcessInstanceByStartUser(userId,
                    new BpmProcessInstanceCancelReqVO()
                            .setId(saleOrder.getProcessInstanceId())
                            .setReason(reqVO.getReason()));
            clearProcessBinding(saleOrder.getId());
        } catch (ServiceException ex) {
            if (!ObjectUtil.equal(ex.getCode(), PROCESS_INSTANCE_CANCEL_FAIL_NOT_EXISTS.getCode())) {
                throw ex;
            }
            reconcileStoppedProcessInstance(saleOrder);
        }
    }

    @Override
    public void handleProcessInstanceResult(Long orderId, String processInstanceId, Integer status, String reason) {
        ErpSaleOrderDO saleOrder = getRequiredSaleOrder(orderId);
        if (!StrUtil.equals(processInstanceId, saleOrder.getProcessInstanceId())) {
            return;
        }
        if (ObjectUtil.equal(status, BpmProcessInstanceStatusEnum.APPROVE.getStatus())) {
            saleOrderService.updateSaleOrderStatusByBpm(orderId, processInstanceId,
                    ErpAuditStatus.APPROVE.getStatus(), reason);
            return;
        }
        if (ObjectUtil.equal(status, BpmProcessInstanceStatusEnum.REJECT.getStatus())) {
            saleOrderService.updateSaleOrderStatusByBpm(orderId, processInstanceId,
                    ErpAuditStatus.REJECT.getStatus(), reason);
            return;
        }
        if (ObjectUtil.equal(status, BpmProcessInstanceStatusEnum.CANCEL.getStatus())) {
            clearProcessBinding(orderId);
        }
    }

    private Map<String, Object> buildVariables(ErpSaleOrderDO saleOrder) {
        Map<String, Object> variables = new HashMap<>();
        variables.put(ErpSaleOrderBpmConstants.VARIABLE_ORDER_ID, saleOrder.getId());
        variables.put(ErpSaleOrderBpmConstants.VARIABLE_ORDER_NO, saleOrder.getNo());
        variables.put(ErpSaleOrderBpmConstants.VARIABLE_TOTAL_PRICE, saleOrder.getTotalPrice());
        variables.put(ErpSaleOrderBpmConstants.VARIABLE_CUSTOMER_ID, saleOrder.getCustomerId());
        variables.put(ErpSaleOrderBpmConstants.VARIABLE_PROJECT_ID, saleOrder.getProjectId());
        variables.put(ErpSaleOrderBpmConstants.VARIABLE_BUSINESS_TYPE, saleOrder.getBusinessType());
        return variables;
    }

    private void reconcileStoppedProcessInstance(ErpSaleOrderDO saleOrder) {
        HistoricProcessInstance historicProcessInstance = processInstanceService
                .getHistoricProcessInstance(saleOrder.getProcessInstanceId());
        if (historicProcessInstance == null) {
            clearProcessBinding(saleOrder.getId());
            return;
        }
        Integer processStatus = FlowableUtils.getProcessInstanceStatus(historicProcessInstance);
        String reason = FlowableUtils.getProcessInstanceReason(historicProcessInstance);
        if (ObjectUtil.equal(processStatus, BpmProcessInstanceStatusEnum.APPROVE.getStatus())) {
            saleOrderService.updateSaleOrderStatusByBpm(saleOrder.getId(), saleOrder.getProcessInstanceId(),
                    ErpAuditStatus.APPROVE.getStatus(), reason);
            return;
        }
        if (ObjectUtil.equal(processStatus, BpmProcessInstanceStatusEnum.REJECT.getStatus())) {
            saleOrderService.updateSaleOrderStatusByBpm(saleOrder.getId(), saleOrder.getProcessInstanceId(),
                    ErpAuditStatus.REJECT.getStatus(), reason);
            return;
        }
        clearProcessBinding(saleOrder.getId());
    }

    private void clearProcessBinding(Long orderId) {
        erpSaleOrderMapper.clearProcessInstanceId(orderId);
    }

    private ErpSaleOrderDO getRequiredSaleOrder(Long orderId) {
        ErpSaleOrderDO saleOrder = erpSaleOrderMapper.selectById(orderId);
        if (saleOrder == null) {
            throw exception(SALE_ORDER_NOT_EXISTS);
        }
        return saleOrder;
    }

}
