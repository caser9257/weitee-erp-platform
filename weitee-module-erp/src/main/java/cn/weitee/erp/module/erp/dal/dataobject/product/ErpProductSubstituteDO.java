package cn.weitee.erp.module.erp.dal.dataobject.product;

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

@TableName("erp_product_substitute")
@KeySequence("erp_product_substitute_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpProductSubstituteDO extends BaseDO {

    @TableId
    private Long id;

    private Long productId;

    private Long substituteProductId;

    private Integer priority;

    private BigDecimal replaceRatio;

    private String remark;

}
