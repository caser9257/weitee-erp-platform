package cn.weitee.erp.module.project.controller.admin.vo.flow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "管理后台 - 工作流保存 Request VO")
@Data
public class ProjectFlowSaveReqVO {
    @Schema(description = "项目编号", required = true)
    @NotNull(message = "项目编号不能为空")
    private Long projectId;
    @Schema(description = "工作流名称")
    private String name;
    @Schema(description = "状态节点列表")
    private List<FlowItemVO> items;

    @Data
    public static class FlowItemVO {
        private Long id;
        private String name;
        private String status;
        private String color;
        private Integer sort;
        private List<Long> turns;
        private List<Long> userIds;
        private String userType;
        private Boolean userLimit;
        private Long columnId;
    }
}
