package cn.iocoder.yudao.module.bpm.dal.dataobject.approval;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
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

    private String remark;

    private Long activeVersionId;

    private Long latestVersionId;

}
