package cn.weitee.erp.module.erp.dal.dataobject.sale;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/**
 * 市场预警规则配置 DO
 *
 * @author system
 */
@TableName("erp_market_alert_rule")
@KeySequence("erp_market_alert_rule_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpMarketAlertRuleDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;

    /**
     * 规则编码
     */
    private String ruleCode;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 规则描述
     */
    private String description;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 阈值天数
     */
    private Integer thresholdDays;

    /**
     * 阈值金额
     */
    private BigDecimal thresholdAmount;

    /**
     * 预警级别（WARNING / DANGER / INFO）
     */
    private String level;

    /**
     * 通知渠道（INTERNAL_MSG / EMAIL / SMS）
     */
    private String notifyChannels;

    /**
     * 备注
     */
    private String remark;

}
