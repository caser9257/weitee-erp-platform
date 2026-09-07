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

/**
 * 产品 Cadence 专属配置。
 *
 * 通用物料字段保留在 erp_product，避免双写和同步漂移。
 */
@TableName("erp_product_cadence")
@KeySequence("erp_product_cadence_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpProductCadenceDO extends BaseDO {

    @TableId
    private Long id;

    private Long productId;

    private String schematicPart;

    private String pcbFootprint;

    private String cadenceDescription;

    private String manufacturerPartNumber;

    private String dimension;

    private String threeDLib;

    private String datasheet;

    private String lifecycle;

    private Boolean preferredPart;

    private String operatingTemperature;

    private String mountingType;

    private Boolean dnp;

    private String importedOrReplacement;

    private String secondDescription;

    private String thirdDescription;

    private String fourthDescription;

}
