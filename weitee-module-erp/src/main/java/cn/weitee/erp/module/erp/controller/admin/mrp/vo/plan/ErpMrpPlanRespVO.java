package cn.weitee.erp.module.erp.controller.admin.mrp.vo.plan;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP MRP 计划 Response VO")
@Data
public class ErpMrpPlanRespVO {

    private Long id;

    private String planNo;

    private String planName;

    private LocalDate planStartDate;

    private LocalDate planEndDate;

    private Integer status;

    private LocalDateTime runTime;

    private Long operatorId;

    private String remark;

    private LocalDateTime createTime;

}
