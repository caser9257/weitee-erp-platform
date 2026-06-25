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

import java.time.LocalDateTime;

/**
 * ERP 产品双账成本重跑日志 DO
 */
@TableName("erp_finance_dual_product_cost_rebuild_log")
@KeySequence("erp_finance_dual_product_cost_rebuild_log_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class ErpFinanceDualProductCostRebuildLogDO extends BaseDO {

    @TableId
    private Long id;
    private Long productId;
    private String productBatchNo;
    private Long productionOrderId;
    private String period;
    private Integer status;
    private Integer triggerType;
    private String remark;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private Long operatorId;
    private String errorMessage;
    private Integer affectedCount;
}
