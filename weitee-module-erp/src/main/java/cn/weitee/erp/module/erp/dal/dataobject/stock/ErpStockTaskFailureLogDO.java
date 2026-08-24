package cn.weitee.erp.module.erp.dal.dataobject.stock;

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

import java.math.BigDecimal;

/**
 * 库存任务失败日志
 *
 * 记录 afterCommit 中库存扣减/移可用失败的任务，支持人工或定时重试，避免库存与单据永久不一致。
 */
@TableName("erp_stock_task_failure_log")
@KeySequence("erp_stock_task_failure_log_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpStockTaskFailureLogDO extends BaseDO {

    /**
     * 业务类型：生产领料扣减
     */
    public static final String BIZ_TYPE_PRODUCTION_DEDUCT = "PRODUCTION_DEDUCT";
    /**
     * 业务类型：IQC 合格移可用
     */
    public static final String BIZ_TYPE_IQC_MOVE_AVAILABLE = "IQC_MOVE_AVAILABLE";

    /**
     * 状态：待重试
     */
    public static final Integer STATUS_PENDING_RETRY = 0;
    /**
     * 状态：已恢复
     */
    public static final Integer STATUS_RESOLVED = 1;

    @TableId
    private Long id;

    /**
     * 业务类型：{@link #BIZ_TYPE_PRODUCTION_DEDUCT} / {@link #BIZ_TYPE_IQC_MOVE_AVAILABLE}
     */
    private String bizType;

    /**
     * 业务单据编号：生产领料=生产工单编号；IQC=采购入库质检关联的入库单编号
     */
    private Long bizId;

    /**
     * 明细编号（IQC 场景为质检明细编号，生产领料场景为空）
     */
    private Long itemId;

    private Long productId;

    private Long warehouseId;

    private BigDecimal qty;

    /**
     * 失败原因
     */
    private String reason;

    /**
     * 状态：0 待重试、1 已恢复
     *
     * @see #STATUS_PENDING_RETRY
     * @see #STATUS_RESOLVED
     */
    private Integer status;

    /**
     * 已重试次数
     */
    private Integer retryCount;

}
