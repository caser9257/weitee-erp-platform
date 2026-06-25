package cn.weitee.erp.module.erp.controller.admin.finance.vo.apestimate;

import cn.weitee.erp.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - ERP 暂估单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpApEstimatePageReqVO extends PageParam {

    @Schema(description = "暂估单号", example = "ZG202604270001")
    private String estimateNo;

    @Schema(description = "暂估月份", example = "2026-04")
    private String estimateMonth;

    @Schema(description = "来源入库单号", example = "RK202604270001")
    private String sourcePurchaseInNo;

    @Schema(description = "供应商编号", example = "1")
    private Long supplierId;

    @Schema(description = "暂估状态", example = "10")
    private Integer status;

}
