package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.reportitem;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - ERP 财务报表项目分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpFinanceReportItemPageReqVO extends PageParam {

    @Schema(description = "账簿编号", example = "1")
    private Long ledgerId;

    @Schema(description = "报表类型", example = "10")
    private Integer reportType;

    @Schema(description = "项目分类", example = "10")
    private Integer itemCategory;

    @Schema(description = "项目编码", example = "BS-ASSET")
    private String itemCode;

    @Schema(description = "项目名称", example = "资产合计")
    private String itemName;

    @Schema(description = "状态", example = "0")
    private Integer status;

}
