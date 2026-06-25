package cn.weitee.erp.module.project.dal.dataobject.report;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

@TableName("project_report")
@KeySequence("project_report_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectReportDO extends BaseDO {
    @TableId
    private Long id;
    private Long userId;
    private String type;
    private String content;
    private String sign;
}
