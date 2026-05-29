package cn.iocoder.yudao.module.bpm.dal.dataobject.approval;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 审批方案版本 DO
 */
@TableName("bpm_approval_scheme_version")
@KeySequence("bpm_approval_scheme_version_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BpmApprovalSchemeVersionDO extends BaseDO {

    @TableId
    private Long id;

    private Long schemeId;

    private Integer versionNo;

    private Integer status;

    private Long sourceVersionId;

    private String sourceType;

    private String designJson;

    private String changeSummary;

    private String publishedBy;

    private LocalDateTime publishedTime;

    /**
     * 通知配置 JSON
     *
     * 存储该版本的通知策略，如短信/邮件/站内信模板与触发条件。
     * 由方案设计器在编辑时写入，发布后不可修改。
     */
    private String notifyJson;

}
