package cn.iocoder.yudao.module.erp.controller.admin.project.vo.project;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 项目新增/修改 Request VO")
@Data
public class ErpProjectSaveReqVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "项目编号", example = "XM202604020001")
    private String no;

    @Schema(description = "项目名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "MES 阶段一")
    @NotNull(message = "项目名称不能为空")
    private String name;

    @Schema(description = "项目类型", example = "DELIVERY")
    private String projectType;

    @Schema(description = "业务类型", example = "SELF_RESEARCH")
    private String businessType;

    @Schema(description = "来源类型", example = "SALE_ORDER")
    private String sourceType;

    @Schema(description = "来源项目编号", example = "1")
    private Long sourceProjectId;

    @Schema(description = "销售订单编号", example = "1")
    private Long saleOrderId;

    @Schema(description = "项目经理", example = "1")
    private Long projectManagerId;

    @Schema(description = "计划负责人", example = "1")
    private Long planCoordinatorId;

    @Schema(description = "MC负责人", example = "1")
    private Long materialControllerId;

    @Schema(description = "归属部门", example = "1")
    private Long ownerDeptId;

    @Schema(description = "当前阶段编码", example = "INIT")
    private String currentStageCode;

    @Schema(description = "风险等级", example = "NORMAL")
    private String riskLevel;

    @Schema(description = "客户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "客户不能为空")
    private Long customerId;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "交期")
    @NotNull(message = "交期不能为空")
    private LocalDateTime deliveryDate;

    @Schema(description = "备注", example = "阶段一项目")
    private String remark;

}
