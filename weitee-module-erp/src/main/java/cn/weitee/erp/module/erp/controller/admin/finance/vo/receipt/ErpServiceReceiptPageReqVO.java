package cn.weitee.erp.module.erp.controller.admin.finance.vo.receipt;

import cn.weitee.erp.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 服务接收单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpServiceReceiptPageReqVO extends PageParam {

    @Schema(description = "单号", example = "SR-2026-001")
    private String no;

    @Schema(description = "租赁合同ID", example = "1")
    private Long leaseContractId;

    @Schema(description = "供应商ID", example = "1")
    private Long supplierId;

    @Schema(description = "归属期间（YYYY-MM）", example = "2026-06")
    private String period;

    @Schema(description = "状态", example = "0")
    private Integer status;

}
