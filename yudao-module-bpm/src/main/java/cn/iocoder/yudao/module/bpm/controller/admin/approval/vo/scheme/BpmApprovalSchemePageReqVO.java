package cn.iocoder.yudao.module.bpm.controller.admin.approval.vo.scheme;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.bpm.enums.approval.BpmApprovalSchemeStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 审批方案分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BpmApprovalSchemePageReqVO extends PageParam {

    @Schema(description = "方案名称", example = "采购申请审批")
    private String name;

    @Schema(description = "模块编码", example = "erp_purchase")
    private String moduleCode;

    @Schema(description = "业务类型", example = "purchase_apply")
    private String bizType;

    @Schema(description = "最新版本状态", example = "30")
    @InEnum(BpmApprovalSchemeStatusEnum.class)
    private Integer latestVersionStatus;

}
