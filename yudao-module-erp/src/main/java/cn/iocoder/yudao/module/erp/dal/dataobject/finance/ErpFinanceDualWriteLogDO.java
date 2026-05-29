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

/**
 * ERP 双写日志 DO
 */
@TableName("erp_finance_dual_write_log")
@KeySequence("erp_finance_dual_write_log_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class ErpFinanceDualWriteLogDO extends BaseDO {

    @TableId
    private Long id;

    /**
     * 源凭证编号（对外账簿）
     */
    private Long sourceVoucherId;

    /**
     * 目标凭证编号（内部账簿）
     */
    private Long targetVoucherId;

    /**
     * 源账簿编号
     */
    private Long sourceLedgerId;

    /**
     * 目标账簿编号
     */
    private Long targetLedgerId;

    /**
     * 业务类型
     */
    private Integer bizType;

    /**
     * 业务单据编号
     */
    private Long bizId;

    /**
     * 双写状态（成功、失败、待重试）
     */
    private Integer status;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 备注
     */
    private String remark;
}