package cn.weitee.erp.module.erp.controller.admin.stock.vo.batch;

import cn.weitee.erp.framework.common.pojo.PageParam;
import cn.weitee.erp.framework.common.validation.InEnum;
import cn.weitee.erp.module.erp.enums.stock.ErpStockBatchAdjustTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.weitee.erp.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - ERP 批次调整单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpStockBatchAdjustmentPageReqVO extends PageParam {

    @Schema(description = "调整单号", example = "TZ202604290001")
    private String adjustNo;

    @Schema(description = "批次库存编号", example = "11")
    private Long stockBatchId;

    @Schema(description = "产品编号", example = "1")
    private Long productId;

    @Schema(description = "仓库编号", example = "2")
    private Long warehouseId;

    @Schema(description = "调整类型：1 调增，2 调减", example = "1")
    @InEnum(value = ErpStockBatchAdjustTypeEnum.class, message = "调整类型必须是 {value}")
    private Integer adjustType;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
