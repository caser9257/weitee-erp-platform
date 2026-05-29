package cn.iocoder.yudao.module.bpm.dal.dataobject.approval;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
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
 * 审批运行时快照 DO
 *
 * 固化审批发起时命中的场景、版本、规则、流程、通知、上下文
 */
@TableName(value = "bpm_approval_instance_snapshot", autoResultMap = true)
@KeySequence("bpm_approval_instance_snapshot_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BpmApprovalInstanceSnapshotDO extends BaseDO {

    @TableId
    private Long id;

    /**
     * 场景编码
     */
    private String sceneCode;

    /**
     * 业务单据 ID
     */
    private String bizId;

    /**
     * 方案编号
     */
    private Long schemeId;

    /**
     * 方案版本编号
     */
    private Long schemeVersionId;

    /**
     * 规则编号
     */
    private Long ruleId;

    /**
     * 流程实例 ID
     */
    private String processInstanceId;

    /**
     * 流程定义 Key
     */
    private String processDefinitionKey;

    /**
     * 业务上下文 JSON
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> contextJson;

    /**
     * 流程 JSON
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> processJson;

    /**
     * 通知配置 JSON
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> notifyJson;

    /**
     * 状态，1-审批中 2-通过 3-驳回 4-撤回
     */
    private Integer status;

    /**
     * 审批结果原因
     */
    private String resultReason;

}
