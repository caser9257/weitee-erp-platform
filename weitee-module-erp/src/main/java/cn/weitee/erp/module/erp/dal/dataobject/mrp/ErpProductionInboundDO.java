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
import java.time.LocalDateTime;

@TableName("erp_production_inbound")
@KeySequence("erp_production_inbound_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpProductionInboundDO extends BaseDO {

    @TableId
    private Long id;

    private String no;

    private Long finishQualityId;

    private String finishQualityNo;

    private Long productionOrderId;

    private String productionOrderNo;

    private Long projectId;

    private Long productId;

    private Long warehouseId;

    private BigDecimal inboundQty;

    private BigDecimal unitCost;

    private BigDecimal totalCost;

    /**
     * 枚举 {@link cn.weitee.erp.module.erp.enums.mrp.ErpProductionInboundStatusEnum}
     */
    private Integer status;

    private LocalDateTime inboundTime;

    private Long executedBy;

    private LocalDateTime executedTime;

    private String remark;

}
