package cn.weitee.erp.module.erp.dal.dataobject.stock;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ERP 盘点快照 DO
 *
 * 记录盘点时点的库存账面数据，用于差异分析
 *
 * @author weitee
 */
@TableName("erp_stock_check_snapshot")
@KeySequence("erp_stock_check_snapshot_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpStockCheckSnapshotDO extends BaseDO {

    /**
     * 快照编号
     */
    @TableId
    private Long id;

    /**
     * 盘点单ID
     *
     * 关联 {@link ErpStockCheckDO#getId()}
     */
    private Long checkId;

    /**
     * 产品ID
     *
     * 关联 {@link cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductDO#getId()}
     */
    private Long productId;

    /**
     * 仓库ID
     *
     * 关联 {@link ErpWarehouseDO#getId()}
     */
    private Long warehouseId;

    /**
     * 账面数量
     */
    private BigDecimal bookQty;

    /**
     * 账面金额
     */
    private BigDecimal bookAmount;

    /**
     * 快照时加权平均单浠?
     */
    private BigDecimal averageCost;

    /**
     * 快照时间
     */
    private LocalDateTime snapshotTime;

}
