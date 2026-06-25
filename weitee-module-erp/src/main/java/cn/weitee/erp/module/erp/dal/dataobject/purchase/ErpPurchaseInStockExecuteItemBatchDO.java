package cn.weitee.erp.module.erp.dal.dataobject.purchase;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
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
import java.time.LocalDate;
import java.time.LocalDateTime;

@TableName("erp_purchase_in_stock_execute_item_batch")
@KeySequence("erp_purchase_in_stock_execute_item_batch_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpPurchaseInStockExecuteItemBatchDO extends BaseDO {

    @TableId
    private Long id;

    private Long executeItemId;

    private Long purchaseInItemId;

    private Long stockBatchId;

    private Long purchaseSourceBatchId;

    private Long productId;

    private Long warehouseId;

    private String batchNo;

    private String purchaseSourceBatchNo;

    private BigDecimal count;

    private LocalDateTime inboundTime;

    private LocalDate produceDate;

    private LocalDate expireDate;

    private String remark;

}
