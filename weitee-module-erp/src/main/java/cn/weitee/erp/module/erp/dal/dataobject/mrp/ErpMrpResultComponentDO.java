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

@TableName("erp_mrp_result_component")
@KeySequence("erp_mrp_result_component_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpMrpResultComponentDO extends BaseDO {

    @TableId
    private Long id;

    private Long planId;

    private Long resultId;

    private Long materialId;

    private String componentCode;

    private String componentName;

    private String componentRole;

    private Integer sequenceNo;

    private Boolean enableFlag;

    private BigDecimal baseQty;

    private BigDecimal consumedQty;

    private BigDecimal remainingQty;

}
