package cn.iocoder.yudao.module.erp.dal.dataobject.finance;

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
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@TableName("erp_finance_asset_depreciation")
@KeySequence("erp_finance_asset_depreciation_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class ErpFinanceAssetDepreciationDO extends BaseDO {

    @TableId
    private Long id;
    private Long assetId;
    private String assetNo;
    private String period;
    private BigDecimal depreciationAmount;
    private BigDecimal beforeDepreciatedAmount;
    private BigDecimal afterDepreciatedAmount;
    private BigDecimal beforeCurrentAmount;
    private BigDecimal afterCurrentAmount;
    private Integer status;
    private Long voucherId;
    private String remark;
}
