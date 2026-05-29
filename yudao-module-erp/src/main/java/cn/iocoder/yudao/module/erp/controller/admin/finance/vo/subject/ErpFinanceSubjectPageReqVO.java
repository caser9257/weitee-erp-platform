package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.subject;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - ERP 财务科目分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpFinanceSubjectPageReqVO extends PageParam {

    @Schema(description = "账簿编号", example = "1")
    private Long ledgerId;

    @Schema(description = "上级科目编号", example = "1")
    private Long parentId;

    @Schema(description = "科目编码", example = "660201")
    private String subjectCode;

    @Schema(description = "科目名称", example = "管理费用-研发费")
    private String subjectName;

    @Schema(description = "科目类型", example = "60")
    private Integer subjectType;

    @Schema(description = "状态", example = "0")
    private Integer status;

}
