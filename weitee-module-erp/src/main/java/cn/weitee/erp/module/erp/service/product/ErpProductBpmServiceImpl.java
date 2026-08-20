package cn.weitee.erp.module.erp.service.product;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.weitee.erp.module.bpm.service.approval.BpmApprovalRuntimeService;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductDO;
import cn.weitee.erp.module.erp.dal.mysql.product.ErpProductMapper;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.enums.ErpProductBpmConstants;
import cn.weitee.erp.module.erp.util.ErpTransactionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCT_APPROVE_FAIL;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCT_BPM_CANCEL_FAIL;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCT_BPM_SUBMIT_FAIL;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCT_NOT_EXISTS;

@Service
@Validated
@Slf4j
public class ErpProductBpmServiceImpl implements ErpProductBpmService {

    @Resource
    private ErpProductMapper productMapper;
    @Resource
    private BpmApprovalRuntimeService approvalRuntimeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String submitProduct(Long userId, Long productId) {
        ErpProductDO product = getRequiredProduct(productId);
        Integer auditStatus = product.getAuditStatus() != null ? product.getAuditStatus() : ErpAuditStatus.DRAFT.getStatus();
        if (ObjectUtil.equal(auditStatus, ErpAuditStatus.APPROVE.getStatus())) {
            throw exception(PRODUCT_APPROVE_FAIL);
        }
        if (ObjectUtil.equal(auditStatus, ErpAuditStatus.PROCESS.getStatus())
                && StrUtil.isNotBlank(product.getProcessInstanceId())) {
            throw exception(PRODUCT_BPM_SUBMIT_FAIL);
        }
        productMapper.updateById(new ErpProductDO()
                .setId(productId)
                .setAuditStatus(ErpAuditStatus.PROCESS.getStatus())
                .setProcessInstanceId(null));
        ErpTransactionUtils.afterCommit(() -> {
            try {
                String processInstanceId = approvalRuntimeService.submit(
                        ErpProductBpmConstants.SCENE_CODE, productId, userId);
                productMapper.updateById(new ErpProductDO()
                        .setId(productId)
                        .setProcessInstanceId(processInstanceId));
            } catch (Exception e) {
                log.error("[submitProduct] BPM 创建失败，productId={}", productId, e);
                productMapper.updateById(new ErpProductDO()
                        .setId(productId)
                        .setAuditStatus(ErpAuditStatus.FAILED.getStatus())
                        .setProcessInstanceId(null));
            }
        });
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelProductApproval(Long userId, Long productId, String reason) {
        ErpProductDO product = getRequiredProduct(productId);
        Integer auditStatus = product.getAuditStatus() != null ? product.getAuditStatus() : ErpAuditStatus.DRAFT.getStatus();
        if (!ObjectUtil.equal(auditStatus, ErpAuditStatus.PROCESS.getStatus())
                || StrUtil.isBlank(product.getProcessInstanceId())) {
            throw exception(PRODUCT_BPM_CANCEL_FAIL);
        }
        ErpTransactionUtils.afterCommit(() -> {
            try {
                approvalRuntimeService.cancel(ErpProductBpmConstants.SCENE_CODE, productId, userId, reason);
            } catch (Exception e) {
                log.warn("[cancelProductApproval] BPM 撤回失败，productId={}", productId, e);
                throw e;
            }
        });
    }

    private ErpProductDO getRequiredProduct(Long productId) {
        ErpProductDO product = productMapper.selectById(productId);
        if (product == null) {
            throw exception(PRODUCT_NOT_EXISTS);
        }
        return product;
    }

}
