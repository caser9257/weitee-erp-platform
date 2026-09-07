package cn.weitee.erp.module.erp.dal.dataobject.product;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 物料待审变更暂存 DO
 *
 * 物料修改审批采用暂存表模式：提交修改时变更字段写入本表，主表保持原值；
 * 审批通过后按 changed_fields 将变更落至物料主表；驳回仅丢弃本记录。
 */
@TableName(value = "erp_product_pending_change", autoResultMap = true)
@KeySequence("erp_product_pending_change_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpProductPendingChangeDO extends BaseDO {

    /**
     * 状态 - 待审
     */
    public static final Integer STATUS_PENDING = 1;
    /**
     * 状态 - 通过
     */
    public static final Integer STATUS_APPROVED = 2;
    /**
     * 状态 - 驳回
     */
    public static final Integer STATUS_REJECTED = 3;
    /**
     * 状态 - 失败（BPM 创建失败，可重新提交）
     */
    public static final Integer STATUS_FAILED = 4;

    @TableId
    private Long id;

    /**
     * 物料编号（唯一约束：一个物料同时仅一笔在途修改）
     */
    private Long productId;

    /**
     * 批量导入批次编号（同一批导入的暂存变更归组；单条修改为 NULL）
     */
    private Long batchId;

    /**
     * 变更字段值（JSON，仅变更字段）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> changeData;

    /**
     * 变更字段名清单，逗号分隔
     */
    private String changedFields;

    /**
     * 状态：1待审 2通过 3驳回 4失败
     */
    private Integer status;

    /**
     * BPM 流程实例编号
     */
    private String processInstanceId;

    /**
     * 提交说明/审批结果原因
     */
    private String reason;

}
