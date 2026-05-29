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
import java.time.LocalDateTime;

/**
 * ERP 产品双账成本结果 DO
 */
@TableName("erp_finance_dual_product_cost_result")
@KeySequence("erp_finance_dual_product_cost_result_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class ErpFinanceDualProductCostResultDO extends BaseDO {

    @TableId
    private Long id;
    private Long productId;
    private String productNo;
    private String productName;
    private String productBatchNo;
    private Long productionOrderId;
    private String productionOrderNo;
    private String period;
    private BigDecimal externalMaterialAmount;
    private BigDecimal internalMaterialAmount;
    private BigDecimal externalLaborAmount;
    private BigDecimal internalLaborAmount;
    private BigDecimal externalOverheadAmount;
    private BigDecimal internalOverheadAmount;
    private BigDecimal externalTotalAmount;
    private BigDecimal internalTotalAmount;
    private BigDecimal diffAmount;
    private Integer status;
    private Integer versionNo;
    private LocalDateTime lastRebuildTime;
    private Long lastRebuildBy;
    private String remark;
}
