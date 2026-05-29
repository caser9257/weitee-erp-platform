package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.projectdualcost;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 项目双账成本分页查询 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpFinanceDualProjectCostPageReqVO extends PageParam {

    @Schema(description = "项目ID", example = "1")
    private Long projectId;

    @Schema(description = "项目编号", example = "PRJ-2026-001")
    private String projectNo;

    @Schema(description = "项目名称", example = "研发项目A")
    private String projectName;

    @Schema(description = "期间（YYYY-MM）", example = "2026-05")
    private String period;

    @Schema(description = "成本类别", example = "10")
    private Integer costType;
}
