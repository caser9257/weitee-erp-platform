package cn.weitee.erp.module.erp.controller.admin.finance.vo.subject;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 财务科目 Response VO")
@Data
public class ErpFinanceSubjectRespVO {

    @Schema(description = "科目编号", example = "1")
    private Long id;

    @Schema(description = "账簿编号", example = "1")
    private Long ledgerId;

    @Schema(description = "账簿名称", example = "标准账簿")
    private String ledgerName;

    @Schema(description = "上级科目编号", example = "1")
    private Long parentId;

    @Schema(description = "科目编码", example = "660201")
    private String subjectCode;

    @Schema(description = "科目名称", example = "管理费用-研发费")
    private String subjectName;

    @Schema(description = "科目类型", example = "60")
    private Integer subjectType;

    @Schema(description = "科目类型名称", example = "费用")
    private String subjectTypeName;

    @Schema(description = "余额方向", example = "10")
    private Integer balanceDirection;

    @Schema(description = "余额方向名称", example = "借方")
    private String balanceDirectionName;

    @Schema(description = "是否末级科目", example = "true")
    private Boolean leaf;

    @Schema(description = "状态", example = "0")
    private Integer status;

    @Schema(description = "排序", example = "1")
    private Integer sort;

    @Schema(description = "备注", example = "研发费用科目")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
