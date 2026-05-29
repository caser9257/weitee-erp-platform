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

import java.math.BigDecimal;

/**
 * ERP 暂估单项 DO
 */
@TableName("erp_ap_estimate_item")
@KeySequence("erp_ap_estimate_item_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpApEstimateItemDO extends BaseDO {

    @TableId
    private Long id;

    private Long estimateId;

    private Long sourcePurchaseInItemId;
    private Long sourcePurchaseInId;
    private String sourcePurchaseInNo;

    private Long sourceOrderId;
    private Long sourceOrderItemId;
    private String sourceOrderNo;

    private Long productId;
    private Long warehouseId;
    private Long projectId;

    /**
     * 数量
     */
    private BigDecimal count;

    /**
     * 来源未税金额，单位：元
     */
    private BigDecimal sourceAmount;

    /**
     * 税额，单位：元
     */
    private BigDecimal taxAmount;

    /**
     * 暂估金额，单位：元
     */
    private BigDecimal amount;

    private String remark;

}
