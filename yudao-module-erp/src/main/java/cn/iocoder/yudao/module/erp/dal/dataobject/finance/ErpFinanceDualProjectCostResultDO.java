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
 * ERP 项目双账成本结果 DO
 */
@TableName("erp_finance_dual_project_cost_result")
@KeySequence("erp_finance_dual_project_cost_result_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class ErpFinanceDualProjectCostResultDO extends BaseDO {

    @TableId
    private Long id;

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 项目编号
     */
    private String projectNo;

    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 期间（YYYY-MM）
     */
    private String period;

    /**
     * 成本类别（10-材料, 20-人工, 30-折旧, 40-电费, 50-其他）
     */
    private Integer costType;

    /**
     * 外部账金额
     */
    private BigDecimal externalAmount;

    /**
     * 内部账金额
     */
    private BigDecimal internalAmount;

    /**
     * 差异金额（内部-外部）
     */
    private BigDecimal diffAmount;

    /**
     * 来源单据数
     */
    private Integer sourceCount;

    /**
     * 状态（0-正常, 10-重建中）
     */
    private Integer status;

    /**
     * 版本号
     */
    private Integer versionNo;

    /**
     * 最后重建时间
     */
    private LocalDateTime lastRebuildTime;

    /**
     * 最后重建人
     */
    private Long lastRebuildBy;

    /**
     * 备注
     */
    private String remark;
}
