package cn.weitee.erp.module.erp.dal.dataobject.product;

import lombok.*;
import java.math.BigDecimal;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;

/**
 * ERP 产品单位 DO
 *
 * @author WeTai
 */
@TableName("erp_product_unit")
@KeySequence("erp_product_unit_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpProductUnitDO extends BaseDO {

    /**
     * 单位编号
     */
    @TableId
    private Long id;
    /**
     * 单位名字
     */
    private String name;
    /**
     * 单位状态
     */
    private Integer status;

    /**
     * 单位类型
     *
     * 枚举 {@link cn.weitee.erp.module.erp.enums.product.ErpProductUnitTypeEnum}
     */
    private Integer unitType;

    /**
     * 基本单位编号，辅助单位归属；基本单位为 null
     *
     * 关联 {@link ErpProductUnitDO#getId()}
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Long baseUnitId;

    /**
     * 换算率：1 辅助单位 = conversionRate 基本单位；基本单位为 null
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private BigDecimal conversionRate;

    /**
     * 数量精度，0 表示只允许整数
     */
    private Integer quantityPrecision;

}
