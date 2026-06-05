package cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.policy;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Schema(description = "管理后台 - ERP 净需求策略新增/修改 Request VO")
@Data
public class ErpMrpNettingPolicySaveReqVO {

    private Long id;

    @NotBlank(message = "策略编码不能为空")
    private String code;

    @NotBlank(message = "策略名称不能为空")
    private String name;

    private Boolean enableFlag;

    private Boolean defaultFlag;

    private String remark;

    private List<String> businessTypes;

    @Valid
    @NotEmpty(message = "组件配置不能为空")
    private List<Line> lines;

    @Data
    public static class Line {

        @NotBlank(message = "组件编码不能为空")
        private String componentCode;

        private Boolean enableFlag;

        private Integer sequenceNo;
    }
}
