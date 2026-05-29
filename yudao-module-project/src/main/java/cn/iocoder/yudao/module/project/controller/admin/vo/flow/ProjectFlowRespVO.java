package cn.iocoder.yudao.module.project.controller.admin.vo.flow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 工作流 Response VO")
@Data
public class ProjectFlowRespVO {
    @Schema(description = "工作流编号")
    private Long id;
    @Schema(description = "项目编号")
    private Long projectId;
    @Schema(description = "工作流名称")
    private String name;
    @Schema(description = "状态节点列表")
    private List<FlowItemRespVO> items;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Data
    public static class FlowItemRespVO {
        private Long id;
        private Long flowId;
        private String name;
        private String status;
        private String color;
        private Integer sort;
        private String turns;
        private String userIds;
        private String userType;
        private Boolean userLimit;
        private Long columnId;
    }
}
