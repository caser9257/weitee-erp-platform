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

import java.time.LocalDateTime;

/**
 * ERP 项目双账成本重跑日志 DO
 */
@TableName("erp_finance_dual_project_cost_rebuild_log")
@KeySequence("erp_finance_dual_project_cost_rebuild_log_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class ErpFinanceDualProjectCostRebuildLogDO extends BaseDO {

    @TableId
    private Long id;

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 期间
     */
    private String period;

    /**
     * 状态（10-成功, 20-失败, 30-进行中）
     */
    private Integer status;

    /**
     * 触发类型（10-手动, 20-定时）
     */
    private Integer triggerType;

    /**
     * 备注
     */
    private String remark;

    /**
     * 开始时间
     */
    private LocalDateTime startedAt;

    /**
     * 结束时间
     */
    private LocalDateTime finishedAt;

    /**
     * 操作人ID
     */
    private Long operatorId;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 影响记录数
     */
    private Integer affectedCount;
}
