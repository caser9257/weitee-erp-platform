package cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.policy;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - ERP 净需求策略分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpMrpNettingPolicyPageReqVO extends PageParam {

    private String code;

    private String name;

    private String businessType;

    private Boolean enableFlag;
}
