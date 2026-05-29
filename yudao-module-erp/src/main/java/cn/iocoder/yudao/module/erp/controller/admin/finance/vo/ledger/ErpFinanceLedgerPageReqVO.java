package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.ledger;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - ERP 财务账簿分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpFinanceLedgerPageReqVO extends PageParam {

    @Schema(description = "账簿编码", example = "BOOK-STD")
    private String no;

    @Schema(description = "账簿名称", example = "标准账簿")
    private String name;

    @Schema(description = "启用状态", example = "0")
    private Integer status;

    @Schema(description = "是否默认账簿", example = "true")
    private Boolean defaultStatus;

    @Schema(description = "备注", example = "采购财务主账簿")
    private String remark;
}
