package cn.weitee.erp.module.erp.service.product;

import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ProductSaveReqVO;

import java.util.Set;

/**
 * 物料批量修改审批明细项（一次批量导入的其中一个物料变更）
 *
 * @param productId     物料编号
 * @param target        变更后完整字段快照（与单条修改审批的暂存快照同构）
 * @param changedFields 变更字段名集合（导入校验阶段已完成 diff 计算与冻结校验）
 */
public record ProductBatchUpdateItem(Long productId, ProductSaveReqVO target, Set<String> changedFields) {
}
