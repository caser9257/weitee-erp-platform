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

    /**
     * 替代类型：见 {@link cn.weitee.erp.module.erp.enums.product.ErpProductSubstituteTypeEnum}
     */
    private Integer substituteType;

    /**
     * 启用状态：见 {@link cn.weitee.erp.framework.common.enums.CommonStatusEnum}
     */
    private Integer status;

    private String remark;

}
