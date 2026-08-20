package cn.weitee.erp.module.mes.dal.dataobject;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/** MES 工序任务派工记录。 */
@TableName("mes_work_task_dispatch")
@KeySequence("mes_work_task_dispatch_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesTaskDispatchDO extends BaseDO {

    @TableId
    private Long id;
    private Long taskId;
    private String taskNo;
    private Long productionOrderId;
    private String productionOrderNo;
    private Long orderStepId;
    private Integer stepNo;
    private String stepCode;
    private String stepName;
    private Long workCenterId;
    private Long deviceId;
    private Long teamId;
    private Long workerUserId;
    private Integer dispatchStatus;
    private Integer activeFlag;
    private LocalDateTime dispatchTime;
    private LocalDateTime revokeTime;
    private String remark;
}
