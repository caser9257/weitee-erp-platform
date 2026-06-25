package cn.weitee.erp.module.erp.dal.dataobject.mrp;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

@TableName("erp_bom_item")
@KeySequence("erp_bom_item_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpBomItemDO extends BaseDO {

    @TableId
    private Long id;

    private Long bomId;

    private Long materialId;

    private Integer materialType;

    private Long unitId;

    private BigDecimal usageQty;

    private BigDecimal lossRate;

    private Integer leadTimeDay;

    private Boolean mrpEnableFlag;

    private String supplyOwner;

    private Integer sort;

    private String remark;

}
