package cn.weitee.erp.module.bpm.dal.dataobject.approval;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 审批方案 DO
 */
@TableName("bpm_approval_scheme")
@KeySequence("bpm_approval_scheme_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BpmApprovalSchemeDO extends BaseDO {

    @TableId
    private Long id;

    private String code;

    private String name;

    private String moduleCode;

    private String bizType;

    /**
     * 关联场景ID
     */
    private Long sceneId;

    private String remark;

    private Long activeVersionId;

    private Long latestVersionId;

    /**
     * 归属用户ID（配置管理员）
     * 
     * 用于数据权限控制：流程配置管理员只能操作自己配置的方案
     */
    private Long ownerUserId;

}
