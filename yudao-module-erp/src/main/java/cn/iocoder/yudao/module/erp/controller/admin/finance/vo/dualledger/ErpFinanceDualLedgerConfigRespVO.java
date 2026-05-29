package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.dualledger;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 双账套账簿映射 Response VO")
@Data
public class ErpFinanceDualLedgerConfigRespVO {

    @Schema(description = "映射编号", example = "1")
    private Long id;

    @Schema(description = "业务类型", example = "11")
    private Integer bizType;

    @Schema(description = "业务类型名称", example = "采购入库")
    private String bizTypeName;

    @Schema(description = "对外账账簿编号", example = "99601")
    private Long externalLedgerId;

    @Schema(description = "对外账账簿名称", example = "财务主账簿")
    private String externalLedgerName;

    @Schema(description = "内部账账簿编号", example = "99602")
    private Long internalLedgerId;

    @Schema(description = "内部账账簿名称", example = "内部管理账簿")
    private String internalLedgerName;

    @Schema(description = "状态", example = "0")
    private Integer status;

    @Schema(description = "备注", example = "采购入库双账套映射")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
