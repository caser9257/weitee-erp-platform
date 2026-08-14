package cn.weitee.erp.module.mes.controller.admin.vo.sopimport;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MesSopImportRecordRespVO {

    private Long id;

    private String fileName;

    private String fileUrl;

    private String ocrText;

    private Integer status;

    private Long sopId;

    private String errorMsg;

    private LocalDateTime createTime;

}
