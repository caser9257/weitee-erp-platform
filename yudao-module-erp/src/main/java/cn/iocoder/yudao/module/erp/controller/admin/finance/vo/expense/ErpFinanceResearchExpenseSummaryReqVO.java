package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.expense;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.erp.enums.ErpResearchExpenseCategoryEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 研发费用项目维度汇总 Request VO")
@Data
public class ErpFinanceResearchExpenseSummaryReqVO {
    
    @Schema(description = "项目编号", example = "1")
    private Long projectId;
    
    @Schema(description = "开始时间")
    private LocalDateTime beginTime;
    
    @Schema(description = "结束时间")
    private LocalDateTime endTime;
    
    @Schema(description = "研发支出分类", example = "10")
    @InEnum(ErpResearchExpenseCategoryEnum.class)
    private Integer researchCategory;
}