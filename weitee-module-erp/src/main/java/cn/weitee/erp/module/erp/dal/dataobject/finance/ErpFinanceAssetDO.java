package cn.weitee.erp.module.erp.dal.dataobject.finance;

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
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;

@TableName("erp_finance_asset")
@KeySequence("erp_finance_asset_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class ErpFinanceAssetDO extends BaseDO {

    @TableId
    private Long id;
    private String no;
    private String name;
    private String categoryName;
    private Long candidateId;
    private Integer sourceType;
    private Long sourceBizId;
    private String sourceBizNo;
    private Long sourceItemId;
    private Long deptId;
    private Long responsibleUserId;
    private LocalDate purchaseDate;
    private LocalDate startUseDate;
    private BigDecimal originalAmount;
    private BigDecimal salvageRate;
    private BigDecimal salvageAmount;
    private String depreciationMethod;
    private Integer depreciationPeriodMonths;
    private String depreciationStartPeriod;
    private BigDecimal depreciatedAmount;
    private BigDecimal currentAmount;
    private Integer status;
    private String lastDepreciationPeriod;
    private String remark;
    /**
     * 资产类型
     *
     * 0=固定资产, 1=无形资产
     */
    private Integer assetType;
    /**
     * 子分类
     *
     * 无形资产的细分类型，如：专利权、软件著作权、商标权等
     */
    private String subCategory;
}
