package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 采购订单驳回日志 Response VO")
@Data
public class ErpPurchaseOrderRejectLogRespVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "采购订单编号", example = "1")
    private Long orderId;

    @Schema(description = "驳回原因", example = "资料不完整")
    private String reason;

    @Schema(description = "驳回人编号", example = "1")
    private Long rejectUserId;

    @Schema(description = "驳回人名称", example = "管理员")
    private String rejectUserName;

    @Schema(description = "驳回人昵称", example = "管理员")
    private String rejectUserNickname;

    @Schema(description = "创建人", example = "1")
    private String creator;

    @Schema(description = "创建人名称", example = "管理员")
    private String creatorName;

    @Schema(description = "驳回时间")
    private LocalDateTime createTime;

}
