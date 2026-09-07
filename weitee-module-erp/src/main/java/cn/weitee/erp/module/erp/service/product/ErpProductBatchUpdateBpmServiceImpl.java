package cn.weitee.erp.module.erp.service.product;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.weitee.erp.module.bpm.service.approval.BpmApprovalRuntimeService;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ProductSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductDO;
import cn.weitee.erp.module.erp.dal.mysql.product.ErpProductMapper;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.enums.ErpProductBpmConstants;
import cn.weitee.erp.module.erp.util.ErpTransactionUtils;
import cn.weitee.erp.module.system.service.notify.ImportNotifyHelper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.List;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCT_AUDIT_STATUS_ILLEGAL;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCT_BATCH_UPDATE_EMPTY;

@Service
@Validated
@Slf4j
public class ErpProductBatchUpdateBpmServiceImpl implements ErpProductBatchUpdateBpmService {

    @Resource
    private ErpProductMapper productMapper;
    @Resource
    private BpmApprovalRuntimeService approvalRuntimeService;
    @Resource
    private ErpProductPendingChangeService pendingChangeService;
    @Resource
    private ErpProductService productService;
    @Resource
    private ImportNotifyHelper importNotifyHelper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductBatchSubmitResult submitBatchUpdate(Long userId, List<ProductBatchUpdateItem> items,
                                                      String sceneDescription) {
        // 0. 前置校验
        if (CollUtil.isEmpty(items)) {
            throw exception(PRODUCT_BATCH_UPDATE_EMPTY);
        }
        long batchId = IdUtil.getSnowflakeNextId();
        List<Long> productIds = items.stream().map(ProductBatchUpdateItem::productId).toList();
        // 1. 主表 CAS 批量锁定 APPROVE -> PROCESS（任一未命中即抛异常，整批回滚，保证批次原子性）
        for (Long productId : productIds) {
            int count = productMapper.update(null, new LambdaUpdateWrapper<ErpProductDO>()
                    .eq(ErpProductDO::getId, productId)
                    .eq(ErpProductDO::getAuditStatus, ErpAuditStatus.APPROVE.getStatus())
                    .set(ErpProductDO::getAuditStatus, ErpAuditStatus.PROCESS.getStatus()));
            if (count == 0) {
                throw exception(PRODUCT_AUDIT_STATUS_ILLEGAL);
            }
        }
        // 2. 写暂存（batch_id 归组；delete + insert 覆盖在途记录）
        for (ProductBatchUpdateItem item : items) {
            pendingChangeService.upsertBatchPendingChange(item.productId(), batchId,
                    item.target(), item.changedFields(), null);
        }
        // 3. 事务外创建 BPM 流程；失败整批标记 FAILED 并回滚主表状态，发送失败站内信。
        //    afterCommit 在事务提交点同步执行，方法返回时结果已确定，调用方可据此决定是否发成功统计通知
        java.util.concurrent.atomic.AtomicReference<ProductBatchSubmitResult> submitResult =
                new java.util.concurrent.atomic.AtomicReference<>(ProductBatchSubmitResult.ok(batchId));
        ErpTransactionUtils.afterCommit(() -> {
            try {
                String processInstanceId = approvalRuntimeService.submit(
                        ErpProductBpmConstants.SCENE_CODE_UPDATE_BATCH, batchId, userId);
                pendingChangeService.updateBatchProcessInstanceId(batchId, processInstanceId);
                productMapper.update(null, new LambdaUpdateWrapper<ErpProductDO>()
                        .in(ErpProductDO::getId, productIds)
                        .eq(ErpProductDO::getAuditStatus, ErpAuditStatus.PROCESS.getStatus())
                        .set(ErpProductDO::getProcessInstanceId, processInstanceId));
            } catch (Exception e) {
                log.error("[submitBatchUpdate] BPM 创建失败，batchId={}, size={}", batchId, items.size(), e);
                String failReason = "流程创建失败：" + e.getMessage();
                pendingChangeService.markBatchFailed(batchId, failReason);
                rollbackProductsToApprove(productIds);
                // 关键修复（Finding 1）：流程创建成功但后续绑定失败时，立即终止孤儿流程，
                // 防止已 FAILED 的批次被孤儿审批通过后错误应用变更（状态分裂）
                cancelOrphanProcess(batchId, userId, failReason);
                // 失败必须明确告知操作人：导入未生效，禁止把"请求已受理"表述成成功
                importNotifyHelper.sendImportResult("erp_import_result_product", sceneDescription,
                        items.size(), 0, items.size(),
                        List.of(failReason + "。本次导入未生效，物料数据未变更，请稍后重新导入"));
                submitResult.set(ProductBatchSubmitResult.failed(batchId, e.getMessage()));
            }
        });
        return submitResult.get();
    }

    private void rollbackProductsToApprove(List<Long> productIds) {
        try {
            productService.restoreProductsAfterUpdateApproval(productIds, null);
        } catch (Exception ex) {
            log.error("[submitBatchUpdate] 主表状态回滚失败，productIds={}", productIds, ex);
        }
    }

    /**
     * Finding 1 修复：流程创建成功但后续绑定失败时，立即终止孤儿流程。
     * 若撤回也失败，保留快照 FAILED + processInstanceId，由人工或定时重试清理，
     * 防止审批人面对一个"批了也没用"的僵尸任务。
     */
    private void cancelOrphanProcess(Long batchId, Long userId, String failReason) {
        try {
            approvalRuntimeService.cancel(
                    ErpProductBpmConstants.SCENE_CODE_UPDATE_BATCH, batchId,
                    userId,
                    failReason);
        } catch (Exception cancelEx) {
            log.warn("[submitBatchUpdate] 孤儿流程撤回失败，需人工清理，batchId={}, error={}",
                    batchId, cancelEx.getMessage());
        }
    }

}
