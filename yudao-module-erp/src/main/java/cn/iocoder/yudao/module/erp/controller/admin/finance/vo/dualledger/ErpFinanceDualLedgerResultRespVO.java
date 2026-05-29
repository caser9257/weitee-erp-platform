package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.dualledger;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - ERP 双账套结果 Response VO")
@Data
public class ErpFinanceDualLedgerResultRespVO {

    @Schema(description = "业务类型", example = "11")
    private Integer bizType;

    @Schema(description = "业务类型名称", example = "采购入库")
    private String bizTypeName;

    @Schema(description = "业务主键", example = "88")
    private Long bizId;

    @Schema(description = "业务单号", example = "CGRK202605240001")
    private String bizNo;

    @Schema(description = "对比状态", example = "20")
    private Integer compareStatus;

    @Schema(description = "对比状态名称", example = "缺少凭证")
    private String compareStatusName;

    @Schema(description = "是否一致", example = "false")
    private Boolean consistent;

    @Schema(description = "问题列表")
    private List<String> issueMessages;

    @Schema(description = "外账账簿编号", example = "1")
    private Long externalLedgerId;

    @Schema(description = "外账账簿名称", example = "财务账")
    private String externalLedgerName;

    @Schema(description = "外账凭证编号", example = "101")
    private Long externalVoucherId;

    @Schema(description = "外账凭证号", example = "V-EXT")
    private String externalVoucherNo;

    @Schema(description = "外账凭证状态", example = "10")
    private Integer externalVoucherStatus;

    @Schema(description = "外账借方金额", example = "500.00")
    private BigDecimal externalDebitAmount;

    @Schema(description = "外账贷方金额", example = "500.00")
    private BigDecimal externalCreditAmount;

    @Schema(description = "内账账簿编号", example = "2")
    private Long internalLedgerId;

    @Schema(description = "内账账簿名称", example = "内部账")
    private String internalLedgerName;

    @Schema(description = "内账凭证编号", example = "102")
    private Long internalVoucherId;

    @Schema(description = "内账凭证号", example = "V-INT")
    private String internalVoucherNo;

    @Schema(description = "内账凭证状态", example = "10")
    private Integer internalVoucherStatus;

    @Schema(description = "内账借方金额", example = "500.00")
    private BigDecimal internalDebitAmount;

    @Schema(description = "内账贷方金额", example = "500.00")
    private BigDecimal internalCreditAmount;

    @Schema(description = "借方金额差额（外账 - 内账）", example = "0.00")
    private BigDecimal debitAmountDiff;

    @Schema(description = "贷方金额差额（外账 - 内账）", example = "0.00")
    private BigDecimal creditAmountDiff;

    @Schema(description = "差异项详情列表")
    private List<DiffItemDetail> diffItemDetails;

    @Schema(description = "差异项汇总差异金额（所有 diffItemDetails.diffAmount 之和）", example = "15.00")
    private BigDecimal totalItemDiffAmount;

    @Schema(description = "业务时间")
    private LocalDateTime voucherTime;

    /**
     * 差异项详情
     */
    @Schema(description = "差异项详情")
    @Data
    public static class DiffItemDetail {

        @Schema(description = "差异项类型", example = "20")
        private Integer diffItemType;

        @Schema(description = "差异项类型名称", example = "人工成本")
        private String diffItemTypeName;

        @Schema(description = "计算类型", example = "1")
        private Integer calculationType;

        @Schema(description = "计算类型名称", example = "按比例分摊")
        private String calculationTypeName;

        @Schema(description = "内部账金额", example = "100.00")
        private BigDecimal internalAmount;

        @Schema(description = "外部账金额", example = "85.00")
        private BigDecimal externalAmount;

        @Schema(description = "差异金额", example = "15.00")
        private BigDecimal diffAmount;

        @Schema(description = "差异比例（%）", example = "15.00")
        private BigDecimal diffRatio;

    }
}
