package cn.weitee.erp.module.erp.dal.dataobject.stock;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/**
 * ERP 库存调拨单项 DO
 *
 * @author WeTai
 */
@TableName("erp_stock_move_item")
@KeySequence("erp_stock_move_item_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpStockMoveItemDO extends BaseDO {

    /**
     * 调拨项编号
     */
    @TableId
    private Long id;
    /**
     * 调拨编号
     *
     * 关联 {@link ErpStockMoveDO#getId()}
     */
    private Long moveId;
    /**
     * 调出仓库编号
     *
     * 关联 {@link ErpWarehouseDO#getId()}
     */
    private Long fromWarehouseId;
    /**
     * 调入仓库编号
     *
     * 关联 {@link ErpWarehouseDO#getId()}
     */
    private Long toWarehouseId;
    /**
     * 产品编号
     *
     * 关联 {@link ErpProductDO#getId()}
     */
    private Long productId;
    /**
     * 产品单位编号
     *
     * 冗余 {@link ErpProductDO#getUnitId()}
     */
    private Long productUnitId;

    /**
     * 录入数量（录入单位口径）；null 表示与 count 相同
     */
    private BigDecimal inputCount;

    /**
     * 换算率快照：1 录入单位 = conversionRate 基本单位；基本单位为 null
     */
    private BigDecimal conversionRate;
    /**
     * 产品单价
     */
    private BigDecimal productPrice;
    /**
     * 产品数量
     */
    private BigDecimal count;
    /**
     * 合计金额，单位：元
     */
    private BigDecimal totalPrice;
    /**
     * 备注
     */
    private String remark;

}