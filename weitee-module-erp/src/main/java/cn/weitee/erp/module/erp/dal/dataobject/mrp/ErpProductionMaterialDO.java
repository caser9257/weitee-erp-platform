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

@TableName("erp_production_material")
@KeySequence("erp_production_material_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpProductionMaterialDO extends BaseDO {

    @TableId
    private Long id;

    private Long productionOrderId;

    private Long productionOrderStepId;

    private Long bomItemId;

    private Long materialId;

    private BigDecimal requiredQty;

    private BigDecimal issuedQty;

    private BigDecimal returnedQty;

    private BigDecimal scrapQty;

    private Long supplyWarehouseId;

    private Integer issueMode;

    private Boolean backflushFlag;

    private String remark;

}
