package cn.iocoder.yudao.module.project.dal.dataobject.report;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.time.LocalDateTime;

@TableName("project_report_receive")
@KeySequence("project_report_receive_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectReportReceiveDO extends BaseDO {
    @TableId
    private Long id;
    private Long reportId;
    private Long userId;
    private LocalDateTime readAt;
}
