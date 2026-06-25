package cn.weitee.erp.module.erp.dal.dataobject.stock;

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

@TableName("erp_stock_batch_reservation")
@KeySequence("erp_stock_batch_reservation_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpStockBatchReservationDO extends BaseDO {

    @TableId
    private Long id;

    private Integer bizType;

    private Long bizId;

    private Long bizItemId;

    private String bizNo;

    private Long productId;

    private Long warehouseId;

    private Long stockBatchId;

    private String batchNo;

    private BigDecimal reservedQty;

    private LocalDateTime inboundTime;

    private LocalDate produceDate;

    private LocalDate expireDate;

    private String remark;

}
