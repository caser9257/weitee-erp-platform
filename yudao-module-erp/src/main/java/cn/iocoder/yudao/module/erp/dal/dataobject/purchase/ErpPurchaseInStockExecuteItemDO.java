package cn.iocoder.yudao.module.erp.dal.dataobject.purchase;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * ERP 采购入库执行单明细 DO
 */
@TableName("erp_purchase_in_stock_execute_item")
@KeySequence("erp_purchase_in_stock_execute_item_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpPurchaseInStockExecuteItemDO extends BaseDO {

    @TableId
    private Long id;

    private Long executeId;

    private Long purchaseInId;

    private Long purchaseInItemId;

    private Long productId;

    private Long warehouseId;

    private BigDecimal count;

    private String remark;

}
