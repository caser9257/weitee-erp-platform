package cn.iocoder.yudao.module.erp.service.sale;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.FlowableUtils;
import cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import cn.iocoder.yudao.module.bpm.service.approval.BpmApprovalRuntimeService;
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceService;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.order.ErpSaleOrderCancelApprovalReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.order.ErpSaleOrderSubmitReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOrderAuditLogDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOrderDO;
import cn.iocoder.yudao.module.erp.dal.mysql.sale.ErpSaleOrderAuditLogMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.sale.ErpSaleOrderMapper;
import cn.iocoder.yudao.module.erp.enums.ErpAuditStatus;
import cn.iocoder.yudao.module.erp.enums.ErpSaleOrderAuditActionTypeConstants;
import org.flowable.engine.history.HistoricProcessInstance;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.APPROVAL_INSTANCE_NOT_PROCESSING;
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
    private BpmApprovalRuntimeService approvalRuntimeService;
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
        approvalRuntimeService.submit("erp.sale.order.submit", saleOrder.getId(), userId);
        erpSaleOrderMapper.updateById(new ErpSaleOrderDO()
                .setId(saleOrder.getId())
                .setStatus(ErpAuditStatus.PROCESS.getStatus()));
        if (ObjectUtil.equal(saleOrder.getStatus(), ErpAuditStatus.REJECT.getStatus())) {
            erpSaleOrderAuditLogMapper.insert(new ErpSaleOrderAuditLogDO()
                    .setOrderId(saleOrder.getId())
                    .setActionType(ErpSaleOrderAuditActionTypeConstants.RESUBMIT)
                    .setBeforeStatus(ErpAuditStatus.REJECT.getStatus())
                    .setAfterStatus(ErpAuditStatus.PROCESS.getStatus()));
        }
        return null; // processInstanceId 在事务提交后由 BPM 引擎异步创建，此处无法返回
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
            approvalRuntimeService.cancel("erp.sale.order.submit", saleOrder.getId(), userId, reqVO.getReason());
        } catch (ServiceException ex) {
            if (!ObjectUtil.equal(ex.getCode(), APPROVAL_INSTANCE_NOT_PROCESSING.getCode())) {
                throw ex;
            }
            reconcileStoppedProcessInstance(saleOrder);
        }
    }

    @Override
    public void handleProcessInstanceResult(Long orderId, String processInstanceId, Integer status, String reason) {
        // 旧监听器入口保留兼容，结果回写已收敛到 SaleOrderResultHandler
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
