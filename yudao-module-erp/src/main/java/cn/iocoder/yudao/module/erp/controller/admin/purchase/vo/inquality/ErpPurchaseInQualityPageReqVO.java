package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.inquality;

import cn.iocoder.yudao.framework.common.pojo.SortablePageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 采购入库轻量质检单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpPurchaseInQualityPageReqVO extends SortablePageParam {

    @Schema(description = "质检单号", example = "CGZJ20260410000001")
    private String no;

    @Schema(description = "采购入库单号", example = "CGRK20260410000001")
    private String purchaseInNo;

    @Schema(description = "质检单状态", example = "20")
    private Integer status;

    @Schema(description = "质检结果", example = "30")
    private Integer result;

    @Schema(description = "指派质检人", example = "1")
    private Long assignedCheckerUserId;

    @Schema(description = "质检人", example = "1")
    private Long checkerUserId;

    @Schema(description = "质检时间")
    private LocalDateTime[] checkTime;

}
