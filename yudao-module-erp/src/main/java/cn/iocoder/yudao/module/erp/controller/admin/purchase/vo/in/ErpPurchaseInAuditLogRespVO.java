package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.in;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 采购入库审批流转日志 Response VO")
@Data
public class ErpPurchaseInAuditLogRespVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "采购入库单编号", example = "1")
    private Long inId;

    @Schema(description = "动作类型", example = "REJECT")
    private String actionType;

    @Schema(description = "流转前状态", example = "10")
    private Integer beforeStatus;

    @Schema(description = "流转后状态", example = "30")
    private Integer afterStatus;

    @Schema(description = "原因", example = "资料不完整")
    private String reason;

    @Schema(description = "操作人编号", example = "1")
    private Long operatorId;

    @Schema(description = "操作人名称", example = "管理员")
    private String operatorName;

    @Schema(description = "操作人昵称", example = "管理员")
    private String operatorNickname;

    @Schema(description = "审批节点名称", example = "仓库负责人")
    private String taskName;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
