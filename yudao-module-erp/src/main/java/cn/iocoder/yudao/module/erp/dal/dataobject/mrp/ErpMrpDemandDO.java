package cn.iocoder.yudao.module.erp.dal.dataobject.mrp;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@TableName("erp_mrp_demand")
@KeySequence("erp_mrp_demand_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpMrpDemandDO extends BaseDO {

    @TableId
    private Long id;

    private Long planId;

    private Long projectId;

    private String sourceType;

    private Long sourceId;

    private Long sourceItemId;

    private Long productId;

    private BigDecimal demandQty;

    private LocalDate demandDate;

}
