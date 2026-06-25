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
 * ERP 双写配置 DO
 */
@TableName("erp_finance_dual_write_config")
@KeySequence("erp_finance_dual_write_config_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class ErpFinanceDualWriteConfigDO extends BaseDO {

    public static final Integer ENABLE_YES = 1;
    public static final Integer ENABLE_NO = 0;

    @TableId
    private Long id;

    /**
     * 账簿编号
     */
    private Long ledgerId;

    /**
     * 启用状态（1-启用，0-禁用）
     */
    private Integer enableStatus;

    /**
     * 双写模式（1-同步，2-异步）
     */
    private Integer writeMode;

    /**
     * 异常处理策略（1-记录日志，2-抛出异常，3-自动重试）
     */
    private Integer exceptionStrategy;

    /**
     * 最大重试次数
     */
    private Integer maxRetryCount;

    /**
     * 备注
     */
    private String remark;
}
