package cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.outsource;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - ERP 委外发料分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpOutsourceIssuePageReqVO extends PageParam {
    private String issueNo;
    private Long orderId;
    private Integer issueType;
    private Integer status;
}
