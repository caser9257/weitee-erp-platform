package cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 生产工时归集 Response VO")
@Data
public class ErpProductionManHourRespVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "生产工单编号", example = "1")
    private Long productionOrderId;

    @Schema(description = "生产工单号", example = "SCGD202604280001")
    private String productionOrderNo;

    @Schema(description = "产品编号", example = "1001")
    private Long productId;

    @Schema(description = "产品名称", example = "精密组件A")
    private String productName;

    @Schema(description = "项目编号", example = "2001")
    private Long projectId;

    @Schema(description = "项目号", example = "XM202604280001")
    private String projectNo;

    @Schema(description = "项目名称", example = "新产品项目")
    private String projectName;

    @Schema(description = "核算月份", example = "2026-04")
    private String accountingMonth;

    @Schema(description = "工时日期")
    private LocalDate workDate;

    @Schema(description = "工时", example = "8.50")
    private BigDecimal manHour;

    @Schema(description = "备注", example = "4 月总装工时")
    private String remark;

    @Schema(description = "创建人", example = "张三")
    private String creatorName;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
