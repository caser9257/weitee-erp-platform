package cn.weitee.erp.module.erp.dal.dataobject.purchase;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

/**
 * ERP 采购来源批次 DO
 */
@TableName("erp_purchase_source_batch")
@KeySequence("erp_purchase_source_batch_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpPurchaseSourceBatchDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 来源批次号
     */
    private String batchNo;
    /**
     * 产品编号
     *
     * 关联 {@link ErpProductDO#getId()}
     */
    private Long productId;
    /**
     * 采购订单编号
     *
     * 关联 {@link ErpPurchaseOrderDO#getId()}
     */
    private Long purchaseOrderId;
    /**
     * 采购订单明细编号
     *
     * 关联 {@link ErpPurchaseOrderItemDO#getId()}
     */
    private Long purchaseOrderItemId;
    /**
     * 供应商编号
     *
     * 关联 {@link ErpSupplierDO#getId()}
     */
    private Long supplierId;
    /**
     * 状态
     *
     * 枚举 {@link cn.weitee.erp.module.erp.enums.ErpPurchaseSourceBatchStatusEnum}
     */
    private Integer status;
    /**
     * 业务日期
     */
    private LocalDate bizDate;
    /**
     * 备注
     */
    private String remark;
}
