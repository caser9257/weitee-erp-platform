package cn.weitee.erp.module.erp.service.mrp;

/**
 * 生产闭环（P8）：原材料入库质检（IQC）→ 可用库存 → 生产发料扣减（关联生产任务单）
 *
 * <p>设计原则：
 * <ul>
 *   <li>采购入库后必须经 IQC 合格方可计入可用库存</li>
 *   <li>生产发料必须关联生产任务单，扣减时校验任务单状态与可用库存</li>
 *   <li>事务内仅写本地状态，跨服务调用放 afterCommit，失败回写 FAILED 可重试</li>
 * </ul>
 */
public interface ErpProductionIqcStockService {

    /**
     * 生产发料扣减库存（关联生产任务单）
     *
     * @param productionOrderId 生产任务单编号
     * @param productId         物料编号
     * @param qty               扣减数量
     */
    void deductStockForProduction(Long productionOrderId, Long productId, java.math.BigDecimal qty);

    /**
     * IQC 合格后将采购入库数量计入可用库存
     *
     * @param purchaseInId 采购入库单编号
     */
    void handleIqcPassed(Long purchaseInId);

}
