package cn.weitee.erp.module.erp.dal.dataobject.product;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    /**
     * 最近一次变更前物料编码（无改码历史为 NULL；完整沿革见 erp_product_code_history）
     */
    private String prevMaterialCode;

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

    private Integer auditStatus;

    private String processInstanceId;

    /**
     * 是否已废除（废除=销号，编码释放可复用，须留痕）
     */
    private Boolean abolishFlag;

    /**
     * 废除时间
     */
    private LocalDateTime abolishTime;

    /**
     * 废除原因
     */
    private String abolishReason;

    /**
     * 废除操作人
     */
    private Long abolishBy;

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

    /**
     * 是否 PCB 元器件，对应迁移 188-erp-product-pcb-component.sql 的 is_pcb_component 列
     */
    @TableField("is_pcb_component")
    private Boolean pcbComponent;

    @TableField(exist = false)
    private String schematicPart;

    @TableField(exist = false)
    private String pcbFootprint;

    @TableField(exist = false)
    private String cadenceDescription;

    @TableField(exist = false)
    private String manufacturerPartNumber;

    @TableField(exist = false)
    private String dimension;

    @TableField(exist = false)
    private String threeDLib;

    @TableField(exist = false)
    private String datasheet;

    @TableField(exist = false)
    private String lifecycle;

    @TableField(exist = false)
    private Boolean preferredPart;

    @TableField(exist = false)
    private String operatingTemperature;

    @TableField(exist = false)
    private String mountingType;

    @TableField(exist = false)
    private Boolean dnp;

    @TableField(exist = false)
    private String importedOrReplacement;

    @TableField(exist = false)
    private String secondDescription;

    @TableField(exist = false)
    private String thirdDescription;

    @TableField(exist = false)
    private String fourthDescription;

}
