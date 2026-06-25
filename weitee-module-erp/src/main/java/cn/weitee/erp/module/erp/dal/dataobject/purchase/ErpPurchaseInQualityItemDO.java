package cn.weitee.erp.module.erp.dal.dataobject.purchase;

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

@TableName("erp_purchase_in_quality_item")
@KeySequence("erp_purchase_in_quality_item_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ErpPurchaseInQualityItemDO extends BaseDO {

    @TableId
    private Long id;

    private Long qualityId;

    private Long purchaseInItemId;

    private Long productId;

    private Long warehouseId;

    private BigDecimal count;

    /**
     * 抽检数量
     */
    private BigDecimal sampleCount;

    private BigDecimal qaPassCount;

    private BigDecimal qaRejectCount;

    private Integer qaResult;

    private String qaRemark;

}
