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

@TableName("erp_stock_batch_record")
@KeySequence("erp_stock_batch_record_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpStockBatchRecordDO extends BaseDO {

    @TableId
    private Long id;

    private Long productId;

    private Long warehouseId;

    private Long stockBatchId;

    private String batchNo;

    private BigDecimal count;

    private BigDecimal afterAvailableQty;

    private Integer bizType;

    private Long bizId;

    private Long bizItemId;

    private String bizNo;

    private String remark;

}
