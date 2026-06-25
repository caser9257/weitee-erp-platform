package cn.weitee.erp.module.erp.controller.admin.project.vo.project;

import cn.weitee.erp.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - ERP 项目分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpProjectPageReqVO extends PageParam {

    @Schema(description = "项目编号", example = "XM202604020001")
    private String no;

    @Schema(description = "项目名称", example = "MES 阶段一")
    private String name;

    @Schema(description = "客户编号", example = "1")
    private Long customerId;

    @Schema(description = "项目类型", example = "DELIVERY")
    private String projectType;

    @Schema(description = "业务类型", example = "SELF_RESEARCH")
    private String businessType;

    @Schema(description = "当前阶段编码", example = "INIT")
    private String currentStageCode;

    @Schema(description = "PC负责人", example = "1")
    private Long planCoordinatorId;

    @Schema(description = "MC负责人", example = "2")
    private Long materialControllerId;

    @Schema(description = "责任视图", example = "PC")
    private String roleView;

    @Schema(description = "状态", example = "0")
    private Integer status;

}
