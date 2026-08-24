package cn.weitee.erp.module.erp.dal.dataobject.mrp;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 工序质检单。 */
@TableName("erp_production_step_quality")
@KeySequence("erp_production_step_quality_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpProductionStepQualityDO extends BaseDO {

    @TableId
    private Long id;
    private String no;
    private Long productionOrderId;
    private Long productionOrderStepId;
    private Long reportId;
    private Integer stepNo;
    private String stepCode;
    private String stepName;
    private BigDecimal reportQty;
    private BigDecimal qualifiedQty;
    private BigDecimal unqualifiedQty;
    private Integer status;
    private Long checkerUserId;
    private LocalDateTime checkTime;
    private String remark;
}
