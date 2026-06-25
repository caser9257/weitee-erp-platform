package cn.weitee.erp.module.erp.controller.admin.finance.vo.lease;

import cn.weitee.erp.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 租赁合同分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpLeaseContractPageReqVO extends PageParam {

    @Schema(description = "合同编号", example = "LC-2026-001")
    private String no;

    @Schema(description = "合同名称", example = "检测仪器租赁合同")
    private String name;

    @Schema(description = "供应商ID", example = "1")
    private Long supplierId;

    @Schema(description = "状态", example = "0")
    private Integer status;

}
