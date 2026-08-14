package cn.weitee.erp.module.mes.controller.admin.vo.sop;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class MesSopDocumentRespVO {

    private Long id;

    private String sopNo;

    private String title;

    private String version;

    private String content;

    private String attachmentUrl;

    private Integer status;

    private LocalDate effectiveDate;

    private LocalDate expireDate;

    private String remark;

    private LocalDateTime createTime;

    /** 绑定工序（route_step_id 列表） */
    private List<Long> routeStepIds;

}
