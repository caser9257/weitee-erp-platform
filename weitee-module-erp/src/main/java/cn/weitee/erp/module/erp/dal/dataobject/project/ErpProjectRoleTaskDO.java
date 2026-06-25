package cn.weitee.erp.module.erp.dal.dataobject.project;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * ERP 项目责任任务 DO
 */
@TableName("erp_project_role_task")
@KeySequence("erp_project_role_task_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpProjectRoleTaskDO extends BaseDO {

    @TableId
    private Long id;

    private Long projectId;

    private String roleCode;

    private String taskType;

    private String taskStatus;

    private Long assigneeUserId;

    private String sourceType;

    private Long sourceId;

    private String summary;

    private LocalDateTime dueTime;

    private LocalDateTime finishTime;

    private String remark;

}
