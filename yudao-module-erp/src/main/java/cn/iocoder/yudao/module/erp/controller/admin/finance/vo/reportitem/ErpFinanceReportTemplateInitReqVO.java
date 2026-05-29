package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.reportitem;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - ERP 财务报表标准模板初始化 Request VO")
@Data
public class ErpFinanceReportTemplateInitReqVO {

    @Schema(description = "账簿编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "账簿编号不能为空")
    private Long ledgerId;

    @Schema(description = "是否覆盖已存在报表项目映射", example = "false")
    private Boolean overrideExisting;

}
