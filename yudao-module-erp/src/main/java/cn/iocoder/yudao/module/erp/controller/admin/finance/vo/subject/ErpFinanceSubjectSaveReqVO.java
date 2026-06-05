package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.subject;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.erp.enums.ErpFinanceSubjectTypeEnum;
import cn.iocoder.yudao.module.erp.enums.ErpFinanceVoucherEntryDirectionEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Schema(description = "管理后台 - ERP 财务科目新增/修改 Request VO")
@Data
public class ErpFinanceSubjectSaveReqVO {

    @Schema(description = "科目编号", example = "1")
    private Long id;

    @Schema(description = "账簿编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "账簿编号不能为空")
    private Long ledgerId;

    @Schema(description = "上级科目编号", example = "1")
    private Long parentId;

    @Schema(description = "科目编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "660201")
    @NotEmpty(message = "科目编码不能为空")
    private String subjectCode;

    @Schema(description = "科目名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "管理费用-研发费")
    @NotEmpty(message = "科目名称不能为空")
    private String subjectName;

    @Schema(description = "科目类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "60")
    @NotNull(message = "科目类型不能为空")
    @InEnum(ErpFinanceSubjectTypeEnum.class)
    private Integer subjectType;

    @Schema(description = "余额方向", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotNull(message = "余额方向不能为空")
    @InEnum(ErpFinanceVoucherEntryDirectionEnum.class)
    private Integer balanceDirection;

    @Schema(description = "是否末级科目", example = "true")
    private Boolean leaf;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    @InEnum(CommonStatusEnum.class)
    private Integer status;

    @Schema(description = "排序", example = "1")
    private Integer sort;

    @Schema(description = "备注", example = "研发费用科目")
    private String remark;

}
