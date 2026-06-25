package cn.weitee.erp.module.erp.dal.dataobject.mrp;

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

@TableName("erp_outsource_inbound")
@KeySequence("erp_outsource_inbound_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpOutsourceInboundDO extends BaseDO {

    @TableId
    private Long id;

    private String inboundNo;

    private Long orderId;

    private Long warehouseId;

    private String batchNo;

    private LocalDateTime inboundTime;

    private LocalDate produceDate;

    private LocalDate expireDate;

    private Integer status;

    private BigDecimal inboundQty;

    private BigDecimal materialCost;

    private BigDecimal processFee;

    private BigDecimal totalCost;

    private BigDecimal unitCost;

    private String remark;

}
