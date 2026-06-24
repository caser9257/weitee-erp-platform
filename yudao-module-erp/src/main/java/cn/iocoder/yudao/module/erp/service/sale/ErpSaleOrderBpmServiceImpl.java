package cn.iocoder.yudao.module.erp.service.sale;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.bpm.service.approval.BpmApprovalRuntimeService;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.order.ErpSaleOrderCancelApprovalReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.order.ErpSaleOrderSubmitReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOrderAuditLogDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOrderDO;
import cn.iocoder.yudao.module.erp.dal.mysql.sale.ErpSaleOrderAuditLogMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.sale.ErpSaleOrderMapper;
import cn.iocoder.yudao.module.erp.enums.ErpAuditStatus;
import cn.iocoder.yudao.module.erp.enums.ErpSaleOrderAuditActionTypeConstants;
import cn.iocoder.yudao.module.erp.util.ErpTransactionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.SALE_ORDER_APPROVE_FAIL;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.SALE_ORDER_BPM_CANCEL_FAIL;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.SALE_ORDER_BPM_SUBMIT_FAIL;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.SALE_ORDER_NOT_EXISTS;

@Service
@Validated
@Slf4j
public class ErpSaleOrderBpmServiceImpl implements ErpSaleOrderBpmService {

    @Resource
    private ErpSaleOrderMapper erpSaleOrderMapper;
    @Resource
    private ErpSaleOrderAuditLogMapper erpSaleOrderAuditLogMapper;
    @Resource
    private ErpSaleOrderService saleOrderService;

    @Resource
    private BpmApprovalRuntimeService approvalRuntimeService;

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
        // 事务内：只写本地状态
        Long orderId = saleOrder.getId();
        erpSaleOrderMapper.updateById(new ErpSaleOrderDO()
                .setId(orderId)
                .setStatus(ErpAuditStatus.PROCESS.getStatus())
                .setProcessInstanceId(null));
        if (ObjectUtil.equal(saleOrder.getStatus(), ErpAuditStatus.REJECT.getStatus())) {
            erpSaleOrderAuditLogMapper.insert(new ErpSaleOrderAuditLogDO()
                    .setOrderId(orderId)
                    .setActionType(ErpSaleOrderAuditActionTypeConstants.RESUBMIT)
                    .setBeforeStatus(ErpAuditStatus.REJECT.getStatus())
                    .setAfterStatus(ErpAuditStatus.PROCESS.getStatus()));
        }

        // 事务外：调 BPM 创建流程
        ErpTransactionUtils.afterCommit(() -> {
            try {
                String processInstanceId = approvalRuntimeService.submit(
                        "erp.sale.order.submit", orderId, userId);
                erpSaleOrderMapper.updateById(new ErpSaleOrderDO()
                        .setId(orderId)
                        .setProcessInstanceId(processInstanceId));
            } catch (Exception e) {
                log.error("[submitSaleOrder] BPM 创建失败，orderId={}", orderId, e);
                erpSaleOrderMapper.updateById(new ErpSaleOrderDO()
                        .setId(orderId)
                        .setStatus(ErpAuditStatus.FAILED.getStatus())
                        .setProcessInstanceId(null));
            }
        });
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelSaleOrderApproval(Long userId, ErpSaleOrderCancelApprovalReqVO reqVO) {
        ErpSaleOrderDO saleOrder = getRequiredSaleOrder(reqVO.getId());
        if (!ObjectUtil.equal(saleOrder.getStatus(), ErpAuditStatus.PROCESS.getStatus())
                || StrUtil.isBlank(saleOrder.getProcessInstanceId())) {
            throw exception(SALE_ORDER_BPM_CANCEL_FAIL);
        }
        // 事务内：只做本地校验
        Long orderId = saleOrder.getId();
        ErpTransactionUtils.afterCommit(() -> {
            try {
                approvalRuntimeService.cancel("erp.sale.order.submit", orderId, userId, reqVO.getReason());
                // clearProcessBinding 由 SaleOrderResultHandler.onCancel 统一处理
            } catch (Exception e) {
                log.warn("[cancelSaleOrderApproval] BPM 撤回失败，orderId={}", orderId, e);
            }
        });
    }

    @Override
    public void handleProcessInstanceResult(Long orderId, String processInstanceId, Integer status, String reason) {
        // 结果回写已收敛到 SaleOrderResultHandler，由 BpmApprovalEventDispatcher 统一分发
    }

    private ErpSaleOrderDO getRequiredSaleOrder(Long orderId) {
        ErpSaleOrderDO saleOrder = erpSaleOrderMapper.selectById(orderId);
        if (saleOrder == null) {
            throw exception(SALE_ORDER_NOT_EXISTS);
        }
        return saleOrder;
    }

}
