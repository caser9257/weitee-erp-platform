package cn.weitee.erp.module.mes.controller.admin.vo.taskexecution;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 现场执行任务上下文。 */
@Data
public class MesTaskExecutionRespVO {

    private Long taskId;
    private String taskNo;
    private Long productionOrderId;
    private String productionOrderNo;
    private Long orderStepId;
    private Integer stepNo;
    private String stepCode;
    private String stepName;
    private Long workCenterId;
    private BigDecimal planQty;
    private Integer taskStatus;
    private LocalDateTime planStartTime;
    private LocalDateTime planEndTime;

    private Long dispatchId;
    private Long deviceId;
    private Long teamId;
    private Long workerUserId;
    private Integer dispatchStatus;

    private Integer erpStepStatus;
    private Boolean qcFlag;
    private BigDecimal reportedQty;
    private BigDecimal qualifiedQty;
    private BigDecimal scrapQty;
}
