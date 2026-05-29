package cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.policy;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - ERP 净需求策略 Response VO")
@Data
public class ErpMrpNettingPolicyRespVO {

    private Long id;

    private String code;

    private String name;

    private Integer version;

    private Boolean enableFlag;

    private Boolean defaultFlag;

    private String remark;

    private List<String> businessTypes;

    private List<Line> lines;

    private LocalDateTime updateTime;

    @Data
    public static class Line {

        private String componentCode;

        private String componentName;

        private String componentRole;

        private Boolean enableFlag;

        private Integer sequenceNo;
    }
}
