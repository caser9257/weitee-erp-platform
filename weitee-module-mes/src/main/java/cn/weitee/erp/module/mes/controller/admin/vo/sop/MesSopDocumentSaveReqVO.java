package cn.weitee.erp.module.mes.controller.admin.vo.sop;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class MesSopDocumentSaveReqVO {

    private Long id;

    @NotBlank(message = "SOP 编码不能为空")
    private String sopNo;

    @NotBlank(message = "SOP 标题不能为空")
    private String title;

    private String version;

    /** 步骤内容（结构化 JSON：标题/步骤列表） */
    private String content;

    private String attachmentUrl;

    private LocalDate effectiveDate;

    private LocalDate expireDate;

    private String remark;

    /** 绑定的工艺路线工序编号列表 */
    @NotEmpty(message = "至少绑定一道工序")
    private List<Long> routeStepIds;

}
