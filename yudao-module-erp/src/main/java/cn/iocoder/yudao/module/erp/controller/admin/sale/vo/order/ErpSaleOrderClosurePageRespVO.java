package cn.iocoder.yudao.module.erp.controller.admin.sale.vo.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 销售闭环工作台分页 Response VO")
@Data
public class ErpSaleOrderClosurePageRespVO {

    @Schema(description = "销售单编号", example = "1024")
    private Long id;

    @Schema(description = "销售单号", example = "XS202604210001")
    private String no;

    @Schema(description = "客户编号", example = "11")
    private Long customerId;

    @Schema(description = "客户名称", example = "测试客户")
    private String customerName;

    @Schema(description = "项目编号", example = "21")
    private Long projectId;

    @Schema(description = "项目名称", example = "测试项目")
    private String projectName;

    @Schema(description = "销售员编号", example = "31")
    private Long saleUserId;

    @Schema(description = "销售员名称", example = "销售员A")
    private String saleUserName;

    @Schema(description = "销售状态", example = "20")
    private Integer status;

    @Schema(description = "下单时间")
    private LocalDateTime orderTime;

    @Schema(description = "交期")
    private LocalDateTime deliveryDate;

    @Schema(description = "交付准备状态", example = "PART_READY")
    private String deliveryReadyStatus;

    @Schema(description = "销售闭环摘要")
    private ErpSaleOrderClosureSummaryRespVO closureSummary;
}
