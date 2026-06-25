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

/**
 * ERP 审计操作日志 DO
 */
@TableName("erp_finance_audit_operation_log")
@KeySequence("erp_finance_audit_operation_log_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class ErpFinanceAuditOperationLogDO extends BaseDO {

    @TableId
    private Long id;

    /**
     * 操作人ID
     */
    private Long userId;

    /**
     * 操作类型（query/export/trace）
     */
    private String operationType;

    /**
     * 操作描述
     */
    private String operationDesc;

    /**
     * 操作对象类型（voucher/ledger/balance）
     */
    private String targetType;

    /**
     * 操作对象ID
     */
    private Long targetId;

    /**
     * 账簿ID
     */
    private Long ledgerId;

    /**
     * 查询参数（JSON）
     */
    private String queryParams;

    /**
     * 结果数量
     */
    private Integer resultCount;

    /**
     * 操作IP
     */
    private String ip;

    /**
     * 用户代理
     */
    private String userAgent;

}
