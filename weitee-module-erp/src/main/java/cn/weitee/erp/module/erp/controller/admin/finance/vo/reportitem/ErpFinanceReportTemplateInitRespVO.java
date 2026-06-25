package cn.weitee.erp.module.erp.controller.admin.finance.vo.reportitem;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - ERP 财务报表标准模板初始化 Response VO")
@Data
public class ErpFinanceReportTemplateInitRespVO {

    @Schema(description = "账簿编号", example = "1")
    private Long ledgerId;

    @Schema(description = "新建科目数量", example = "18")
    private Integer createdSubjectCount;

    @Schema(description = "已存在科目数量", example = "0")
    private Integer existedSubjectCount;

    @Schema(description = "新建报表项目数量", example = "16")
    private Integer createdItemCount;

    @Schema(description = "跳过报表项目数量", example = "0")
    private Integer skippedItemCount;

    @Schema(description = "新建报表项目取数映射数量", example = "20")
    private Integer createdMappingCount;

}
