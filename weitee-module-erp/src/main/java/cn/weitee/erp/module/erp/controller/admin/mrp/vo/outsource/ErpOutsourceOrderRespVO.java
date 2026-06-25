package cn.weitee.erp.module.erp.controller.admin.mrp.vo.outsource;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 委外订单 Response VO")
@Data
public class ErpOutsourceOrderRespVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "委外订单号", example = "WWDD202604280001")
    private String no;

    @Schema(description = "委外类型", example = "10")
    private Integer orderType;

    @Schema(description = "委外类型名称", example = "有 BOM 委外")
    private String orderTypeName;

    @Schema(description = "供应商编号", example = "1")
    private Long supplierId;

    @Schema(description = "供应商名称", example = "供应商A")
    private String supplierName;

    @Schema(description = "成品编号", example = "1")
    private Long productId;

    @Schema(description = "成品名称", example = "喷涂件")
    private String productName;

    @Schema(description = "BOM 编号", example = "1")
    private Long bomId;

    @Schema(description = "项目编号", example = "1")
    private Long projectId;

    @Schema(description = "工艺/工序名称", example = "喷涂")
    private String processName;

    @Schema(description = "计划数量", example = "100")
    private BigDecimal plannedQty;

    @Schema(description = "发料数量", example = "80")
    private BigDecimal issuedQty;

    @Schema(description = "退料数量", example = "5")
    private BigDecimal returnedQty;

    @Schema(description = "完工入库数量", example = "70")
    private BigDecimal finishedQty;

    @Schema(description = "损耗数量", example = "2")
    private BigDecimal lossQty;

    @Schema(description = "状态", example = "20")
    private Integer status;

    @Schema(description = "状态名称", example = "处理中")
    private String statusName;

    @Schema(description = "备注", example = "喷涂委外")
    private String remark;

    @Schema(description = "结案备注", example = "已确认损耗并结案")
    private String closeRemark;

    @Schema(description = "创建人名称", example = "张三")
    private String creatorName;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "结案时间")
    private LocalDateTime closeTime;

}
