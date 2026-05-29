package cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.rule;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - ERP 计划规则分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpMaterialPlanRulePageReqVO extends PageParam {

    private Long productId;

    private String supplyType;

    private String replenishMode;

    private Boolean enableFlag;

}
