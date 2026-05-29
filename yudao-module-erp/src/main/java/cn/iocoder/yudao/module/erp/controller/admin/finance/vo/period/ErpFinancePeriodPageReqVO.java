package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.period;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - ERP 会计期间分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpFinancePeriodPageReqVO extends PageParam {

    @Schema(description = "账簿编号", example = "1")
    private Long ledgerId;

    @Schema(description = "期间编码", example = "2026-04")
    private String periodCode;

    @Schema(description = "会计年度", example = "2026")
    private Integer periodYear;

    @Schema(description = "会计月份", example = "4")
    private Integer periodMonth;

    @Schema(description = "期间状态", example = "10")
    private Integer status;
}
