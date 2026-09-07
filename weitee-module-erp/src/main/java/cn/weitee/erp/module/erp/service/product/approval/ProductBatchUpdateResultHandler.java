package cn.weitee.erp.module.erp.service.product.approval;

import cn.hutool.core.collection.CollUtil;
import cn.weitee.erp.module.bpm.service.approval.handler.ApprovalResultHandler;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductPendingChangeDO;
import cn.weitee.erp.module.erp.enums.ErpProductBpmConstants;
import cn.weitee.erp.module.erp.service.product.ErpProductPendingChangeService;
import cn.weitee.erp.module.erp.service.product.ErpProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 物料批量修改审批结果处理器
 *
 * 批次整体三条路径统一收口（终态单一路径），逐物料复用单条暂存的幂等方法：
 * - 通过：整批暂存快照落主表 → 主表批量恢复 APPROVE
 * - 驳回：整批暂存留痕 REJECTED → 主表批量恢复 APPROVE（主表从未变更，无回滚动作）
 * - 撤回：删除整批暂存 → 主表批量恢复 APPROVE
 */
@Component
@Slf4j
public class ProductBatchUpdateResultHandler implements ApprovalResultHandler {

    @Resource
    private ErpProductPendingChangeService pendingChangeService;
    @Resource
    private ErpProductService productService;

    @Override
    public String getSceneCode() {
        return ErpProductBpmConstants.SCENE_CODE_UPDATE_BATCH;
    }

    @Override
    public void onApprove(Long bizId, String processInstanceId, String reason) {
        List<ErpProductPendingChangeDO> pendings = requireBatch(bizId);
        if (pendings.isEmpty()) {
            return;
        }
        // Finding 1 前置校验：只处理 PROCESSING 状态的批次，FAILED/COMPENSATING 批次即使收到回调也拒绝应用，
        // 防止孤儿流程通过审批后错误落库（状态分裂）
        if (!isCurrentBatch(pendings, processInstanceId)) {
            log.warn("[onApprove] 批次暂存状态或流程实例不匹配，拒绝应用，batchId={}, pi={}",
                    bizId, processInstanceId);
            return;
        }
        // 先业务后终态：逐物料落库成功后才恢复主表状态；重复事件由 apply 幂等跳过。
        // 恢复动作无条件执行：CAS 幂等，且防止"上次落库成功但恢复前崩溃"导致主表卡死在审批中
        for (ErpProductPendingChangeDO pending : pendings) {
            pendingChangeService.applyPendingChange(
                    pending.getProductId(), processInstanceId, reason);
        }
        productService.restoreProductsAfterUpdateApproval(productIds(pendings), processInstanceId);
        log.info("[onApprove] 物料批量修改审批通过并已生效，batchId={}, size={}, pi={}",
                bizId, pendings.size(), processInstanceId);
    }

    @Override
    public void onReject(Long bizId, String processInstanceId, String reason) {
        List<ErpProductPendingChangeDO> pendings = requireBatch(bizId);
        if (pendings.isEmpty() || !isCurrentBatch(pendings, processInstanceId)) {
            log.warn("[onReject] 批次暂存状态或流程实例不匹配，拒绝处理，batchId={}, pi={}",
                    bizId, processInstanceId);
            return;
        }
        // 驳回整批作废：暂存留痕 REJECTED，主表从未变过，无需回滚；恢复动作 CAS 幂等
        for (ErpProductPendingChangeDO pending : pendings) {
            pendingChangeService.rejectPendingChange(
                    pending.getProductId(), processInstanceId, reason);
        }
        productService.restoreProductsAfterUpdateApproval(productIds(pendings), processInstanceId);
        log.info("[onReject] 物料批量修改审批驳回，整批变更已丢弃，batchId={}, size={}, pi={}",
                bizId, pendings.size(), processInstanceId);
    }

    @Override
    public void onCancel(Long bizId, String processInstanceId, String reason) {
        List<ErpProductPendingChangeDO> pendings = requireBatch(bizId);
        if (pendings.isEmpty() || !isCurrentBatch(pendings, processInstanceId)) {
            log.warn("[onCancel] 批次暂存状态或流程实例不匹配，拒绝处理，batchId={}, pi={}",
                    bizId, processInstanceId);
            return;
        }
        for (ErpProductPendingChangeDO pending : pendings) {
            pendingChangeService.discardPendingChange(pending.getProductId());
        }
        productService.restoreProductsAfterCancel(productIds(pendings));
        log.info("[onCancel] 物料批量修改审批撤回，整批暂存已删除，batchId={}, size={}, pi={}",
                bizId, pendings.size(), processInstanceId);
    }

    private List<ErpProductPendingChangeDO> requireBatch(Long batchId) {
        List<ErpProductPendingChangeDO> pendings = pendingChangeService.getPendingChangesByBatch(batchId);
        if (CollUtil.isEmpty(pendings)) {
            log.warn("[requireBatch] 批次暂存记录不存在（重复回调或已被处理），batchId={}", batchId);
        }
        return pendings;
    }

    private List<Long> productIds(List<ErpProductPendingChangeDO> pendings) {
        return pendings.stream().map(ErpProductPendingChangeDO::getProductId).toList();
    }

    private boolean isCurrentBatch(List<ErpProductPendingChangeDO> pendings, String processInstanceId) {
        return processInstanceId != null && pendings.stream().allMatch(pending ->
                ErpProductPendingChangeDO.STATUS_PENDING.equals(pending.getStatus())
                        && processInstanceId.equals(pending.getProcessInstanceId()));
    }

}
