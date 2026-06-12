package cn.iocoder.yudao.module.bpm.dal.dataobject.approval;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 审批场景 DO
 *
 * 审批场景是平台第一公民，代表某个业务模块中的某个业务动作可接入审批
 * 例如：erp.finance.payment.submit 表示付款单的提交审批动作
 */
@TableName("bpm_approval_scene")
@KeySequence("bpm_approval_scene_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BpmApprovalSceneDO extends BaseDO {

    @TableId
    private Long id;

    /**
     * 场景编码，如 erp.finance.payment.submit
     */
    private String sceneCode;

    /**
     * 场景名称
     */
    private String name;

    /**
     * 模块编码，如 erp_finance
     */
    private String moduleCode;

    /**
     * 业务类型，如 payment
     */
    private String bizType;

    /**
     * 动作编码，如 submit
     */
    private String actionCode;

    /**
     * 当前生效方案编号
     * 
     * 使用 FieldStrategy.ALWAYS 确保可以更新为 NULL（解绑场景）
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Long activeSchemeId;

    /**
     * 归属用户ID（配置管理员）
     * 
     * 用于数据权限控制：流程配置管理员只能操作自己配置的流程
     */
    private Long ownerUserId;

    /**
     * 状态，1-启用 0-禁用
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

}
