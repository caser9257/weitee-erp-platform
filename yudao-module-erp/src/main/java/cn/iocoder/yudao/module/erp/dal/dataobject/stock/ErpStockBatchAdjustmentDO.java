package cn.iocoder.yudao.module.erp.dal.dataobject.stock;

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

@TableName("erp_stock_batch_adjustment")
@KeySequence("erp_stock_batch_adjustment_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpStockBatchAdjustmentDO extends BaseDO {

    @TableId
    private Long id;

    private String adjustNo;

    private Long stockBatchId;

    private Long productId;

    private Long warehouseId;

    private String batchNo;

    private Integer adjustType;

    private BigDecimal adjustQty;

    private BigDecimal beforeTotalQty;

    private BigDecimal beforeAvailableQty;

    private BigDecimal afterTotalQty;

    private BigDecimal afterAvailableQty;

    private String remark;

}
