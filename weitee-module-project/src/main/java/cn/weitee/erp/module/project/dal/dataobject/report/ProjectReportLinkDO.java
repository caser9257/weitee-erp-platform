package cn.weitee.erp.module.project.dal.dataobject.report;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

@TableName("project_report_link")
@KeySequence("project_report_link_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectReportLinkDO extends BaseDO {
    @TableId
    private Long id;
    private Long reportId;
    private String sourceType;
    private Long sourceId;
}
