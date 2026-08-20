package cn.weitee.erp.module.erp.controller.admin.rd.vo.bom;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 研发 BOM 变更记录 Response VO")
@Data
public class ErpRdBomChangeLogRespVO {

    private Long id;

    private Long bomId;

    private String changeType;

    private String changeDetail;

    private String creator;

    private LocalDateTime createTime;

}
