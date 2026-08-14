package cn.weitee.erp.module.mes.controller.admin.vo.sopimport;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MesSopImportConfirmReqVO {

    @jakarta.validation.constraints.NotNull(message = "导入记录不能为空")
    private Long importRecordId;

    @NotBlank(message = "SOP 编码不能为空")
    private String sopNo;

    @NotBlank(message = "SOP 标题不能为空")
    private String title;

    /** 校对后的步骤内容（JSON） */
    private String content;

    private String version;

    private String remark;

}
