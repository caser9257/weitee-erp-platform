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

@TableName("erp_production_finish_quality")
@KeySequence("erp_production_finish_quality_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpProductionFinishQualityDO extends BaseDO {

    @TableId
    private Long id;

    private String no;

    private Long productionOrderId;

    private String productionOrderNo;

    private Long sourceOrderId;

    private Long sourceItemId;

    private Long productId;

    private BigDecimal reportQty;

    private BigDecimal qualifiedQty;

    private BigDecimal unqualifiedQty;

    /**
     * 枚举 {@link cn.weitee.erp.module.erp.enums.ErpQaStatusEnum}
     */
    private Integer status;

    private Long checkerUserId;

    private LocalDateTime checkTime;

    private String remark;

}
