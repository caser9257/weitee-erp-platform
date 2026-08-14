package cn.weitee.erp.module.erp.dal.dataobject.mrp;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/** 生产工单工序快照。 */
@TableName("erp_production_order_step")
@KeySequence("erp_production_order_step_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpProductionOrderStepDO extends BaseDO {

    @TableId
    private Long id;
    private Long productionOrderId;
    private Long routeStepId;
    private Integer stepNo;
    private String stepCode;
    private String stepName;
    private Long workCenterId;
    private Long deviceId;
    private Boolean qcFlag;
    private BigDecimal planQty;
    private BigDecimal reportedQty;
    private BigDecimal qualifiedQty;
    private BigDecimal scrapQty;
    private Integer stepStatus;
    private String remark;
}
