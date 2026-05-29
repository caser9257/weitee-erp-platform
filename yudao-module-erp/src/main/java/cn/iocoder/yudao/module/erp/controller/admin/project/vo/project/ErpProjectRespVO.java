package cn.iocoder.yudao.module.erp.controller.admin.project.vo.project;

import org.apache.fesod.sheet.annotation.ExcelIgnoreUnannotated;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Admin - ERP Project Response VO")
@Data
@ExcelIgnoreUnannotated
public class ErpProjectRespVO {

    @Schema(description = "ID", example = "1024")
    @ExcelProperty("ID")
    private Long id;

    @Schema(description = "Project number", example = "XM202604020001")
    @ExcelProperty("Project No")
    private String no;

    @Schema(description = "Project name", example = "MES Phase 1")
    @ExcelProperty("Project Name")
    private String name;

    @Schema(description = "Project type", example = "DELIVERY")
    @ExcelProperty("Project Type")
    private String projectType;

    @Schema(description = "Business type", example = "SELF_RESEARCH")
    @ExcelProperty("Business Type")
    private String businessType;

    @Schema(description = "Source type", example = "SALE_ORDER")
    private String sourceType;

    @Schema(description = "Source project id", example = "1")
    private Long sourceProjectId;

    @Schema(description = "Sale order id", example = "1")
    private Long saleOrderId;

    @Schema(description = "Project manager id", example = "1")
    private Long projectManagerId;

    @Schema(description = "Plan coordinator id", example = "1")
    private Long planCoordinatorId;

    @Schema(description = "Plan coordinator name", example = "Zhang San")
    @ExcelProperty("PC Owner")
    private String planCoordinatorName;

    @Schema(description = "Material controller id", example = "1")
    private Long materialControllerId;

    @Schema(description = "Material controller name", example = "Li Si")
    @ExcelProperty("MC Owner")
    private String materialControllerName;

    @Schema(description = "Owner department id", example = "1")
    private Long ownerDeptId;

    @Schema(description = "Current stage code", example = "INIT")
    @ExcelProperty("Current Stage")
    private String currentStageCode;

    @Schema(description = "Risk level", example = "NORMAL")
    @ExcelProperty("Risk Level")
    private String riskLevel;

    @Schema(description = "PC status", example = "PENDING")
    @ExcelProperty("PC Status")
    private String pcStatus;

    @Schema(description = "MC status", example = "PENDING")
    @ExcelProperty("MC Status")
    private String mcStatus;

    @Schema(description = "Customer id", example = "1")
    private Long customerId;

    @Schema(description = "Customer name", example = "Acme")
    @ExcelProperty("Customer Name")
    private String customerName;

    @Schema(description = "Status", example = "0")
    @ExcelProperty("Status")
    private Integer status;

    @Schema(description = "Delivery date")
    @ExcelProperty("Delivery Date")
    private LocalDateTime deliveryDate;

    @Schema(description = "PC confirm time")
    @ExcelProperty("PC Confirm Time")
    private LocalDateTime pcConfirmTime;

    @Schema(description = "MC confirm time")
    @ExcelProperty("MC Confirm Time")
    private LocalDateTime mcConfirmTime;

    @Schema(description = "PC remark", example = "Plan confirmed")
    @ExcelProperty("PC Remark")
    private String pcRemark;

    @Schema(description = "MC remark", example = "Material confirmed")
    @ExcelProperty("MC Remark")
    private String mcRemark;

    @Schema(description = "Remark", example = "Phase one project")
    @ExcelProperty("Remark")
    private String remark;

    @Schema(description = "Create time")
    @ExcelProperty("Create Time")
    private LocalDateTime createTime;

    @Schema(description = "Todo tasks")
    private List<ErpProjectRoleTaskRespVO> todoTasks;

}
