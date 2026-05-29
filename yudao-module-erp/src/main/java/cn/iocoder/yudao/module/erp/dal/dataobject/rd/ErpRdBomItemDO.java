package cn.iocoder.yudao.module.erp.dal.dataobject.rd;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
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

@TableName("erp_rd_bom_item")
@KeySequence("erp_rd_bom_item_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpRdBomItemDO extends BaseDO {

    @TableId
    private Long id;

    private Long bomId;

    private Long materialId;

    private Integer materialType;

    private Long unitId;

    private BigDecimal usageQty;

    private BigDecimal lossRate;

    private Integer leadTimeDay;

    private Integer sort;

    private String remark;

}
