package cn.iocoder.yudao.module.erp.dal.dataobject.mrp;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("erp_production_order")
@KeySequence("erp_production_order_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpProductionOrderDO extends BaseDO {

    @TableId
    private Long id;

    private String orderNo;

    private Long productId;

    private Long projectId;

    private BigDecimal planQty;

    private BigDecimal finishedQty;

    private Long warehouseId;

    private LocalDateTime planStartTime;

    private LocalDateTime planEndTime;

    private Integer status;

    private String sourceType;

    private Long sourceId;

    private Long sourceOrderId;

    private Long sourceItemId;

    private BigDecimal machineHour;

    private String remark;

}
