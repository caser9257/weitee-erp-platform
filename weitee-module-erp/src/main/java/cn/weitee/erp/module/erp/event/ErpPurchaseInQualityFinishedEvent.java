package cn.weitee.erp.module.erp.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 采购入库质检完结事件
 *
 * <p>由 {@code ErpPurchaseInQualityServiceImpl#finishQualityOrder} 在质检单写入 DONE 终态时发布，
 * 库存侧（IQC 合格移可用）通过 AFTER_COMMIT 监听消费，避免质检服务与库存服务互相注入形成循环依赖。
 *
 * @author WeTai
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErpPurchaseInQualityFinishedEvent {

    /**
     * 质检单编号（erp_purchase_in_quality.id）
     */
    private Long qualityId;

    /**
     * 采购入库单编号（erp_purchase_in.id）
     */
    private Long purchaseInId;

}
