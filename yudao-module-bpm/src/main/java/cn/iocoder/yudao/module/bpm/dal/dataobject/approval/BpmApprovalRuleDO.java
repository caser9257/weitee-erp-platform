package cn.iocoder.yudao.module.bpm.dal.dataobject.approval;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 审批规则 DO
 */
@TableName("bpm_approval_rule")
@KeySequence("bpm_approval_rule_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BpmApprovalRuleDO extends BaseDO {

    @TableId
    private Long id;

    private Long schemeVersionId;

    private String ruleName;

    private String ruleType;

    private Integer priority;

    @TableField("is_default")
    private Boolean defaultRule;

    private String conditionJson;

    private String processJson;

    private Boolean enabled;

}
