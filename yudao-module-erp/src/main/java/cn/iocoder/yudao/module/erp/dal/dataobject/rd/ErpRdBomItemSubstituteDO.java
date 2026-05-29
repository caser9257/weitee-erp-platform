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

@TableName("erp_rd_bom_item_substitute")
@KeySequence("erp_rd_bom_item_substitute_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpRdBomItemSubstituteDO extends BaseDO {

    @TableId
    private Long id;

    private Long bomItemId;

    private Long substituteMaterialId;

    private Integer priority;

    private BigDecimal replaceRatio;

    private Boolean enableAutoRecommend;

    private Integer sort;

    private String remark;

}
