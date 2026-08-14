package cn.weitee.erp.module.mes.dal.dataobject;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** MES 工序任务。 */
@TableName("mes_work_task")
@KeySequence("mes_work_task_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesWorkTaskDO extends BaseDO {

    @TableId
    private Long id;
    private String taskNo;
    private Long productionOrderId;
    private String productionOrderNo;
    private Long orderStepId;
    private Integer stepNo;
    private String stepCode;
    private String stepName;
    private Long workCenterId;
    private BigDecimal planQty;
    private Integer priority;
    private LocalDateTime planStartTime;
    private LocalDateTime planEndTime;
    private LocalDateTime actualStartTime;
    private LocalDateTime actualEndTime;
    private Integer status;
    private String remark;
}
