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

/**
 * ERP 产品 DO
 */
@TableName("erp_product")
@KeySequence("erp_product_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpProductDO extends BaseDO {

    @TableId
    private Long id;

    private String name;

    private String materialCode;

    private String barCode;

    private Long categoryId;

    private Long unitId;

    private Integer productType;

    private Integer produceType;

    private Boolean batchEnable;

    private Boolean snEnable;

    private Long defaultRouteId;

    private Boolean qcEnable;

    private Boolean outsourceEnable;

    private Integer costMethod;

    private Integer status;

    private String standard;

    private String packaging;

    private String qualityGrade;

    private String brandManufacturer;

    private String alternativeModel;

    private String remark;

    private Integer expiryDay;

    private Boolean batchControlFlag;

    private Boolean inspectionRequiredFlag;

    private BigDecimal weight;

    private BigDecimal purchasePrice;

    private BigDecimal salePrice;

    private BigDecimal minPrice;

    private Boolean mrpEnable;

    private Boolean assetFlag;

}
