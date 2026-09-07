package cn.weitee.erp.module.erp.dal.dataobject.finance;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import cn.weitee.erp.module.erp.enums.ErpFinanceVoucherFailureStatusEnum;
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
 * ERP 凭证生成失败记录 DO
 *
 * 业务审核通过但凭证生成失败时落库，支持查询、重试、人工确认
 *
 * @author WeTai
 */
@TableName("erp_finance_voucher_failure")
@KeySequence("erp_finance_voucher_failure_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ErpFinanceVoucherFailureDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;

    /**
     * 业务类型
     *
     * 枚举 {@link cn.weitee.erp.module.erp.enums.common.ErpBizTypeEnum}
     */
    private Integer bizType;

    /**
     * 业务单据编号
     */
    private Long bizId;

    /**
     * 最近一次失败原因
     */
    private String errorMessage;

    /**
     * 最近一次异常堆栈（截断）
     */
    private String errorStack;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 最近一次重试时间
     */
    private LocalDateTime lastRetryTime;

    /**
     * 状态
     *
     * 枚举 {@link ErpFinanceVoucherFailureStatusEnum}
     */
    private Integer status;

    /**
     * 人工确认关闭原因
     */
    private String confirmReason;

}
