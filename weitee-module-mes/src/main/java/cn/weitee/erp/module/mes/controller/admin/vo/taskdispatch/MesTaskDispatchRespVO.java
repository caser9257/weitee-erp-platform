package cn.weitee.erp.module.mes.controller.admin.vo.taskdispatch;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 派工工作台及历史记录响应。 */
@Data
public class MesTaskDispatchRespVO {

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
    private BigDecimal planQty;
    private Integer taskStatus;
    private LocalDateTime planStartTime;
    private LocalDateTime planEndTime;

    private Long deviceId;
    private Long teamId;
    private Long workerUserId;
    private Integer dispatchStatus;
    private Integer activeFlag;
    private LocalDateTime dispatchTime;
    private LocalDateTime revokeTime;
    private String remark;
}
