package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.dualledger;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - ERP 双账套账簿映射分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpFinanceDualLedgerConfigPageReqVO extends PageParam {

    @Schema(description = "业务类型", example = "11")
    private Integer bizType;

    @Schema(description = "对外账账簿编号", example = "99601")
    private Long externalLedgerId;

    @Schema(description = "内部账账簿编号", example = "99602")
    private Long internalLedgerId;

    @Schema(description = "状态", example = "0")
    private Integer status;

    @Schema(description = "备注", example = "采购入库双账套映射")
    private String remark;
}
