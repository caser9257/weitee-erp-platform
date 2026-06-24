package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.voucher;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "管理后台 - 凭证完整性检查结果 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErpFinanceVoucherIntegrityCheckRespVO {

    @Schema(description = "缺失凭证的业务单据列表")
    private List<MissingVoucherItem> missingVouchers;

    @Schema(description = "借贷不平衡的凭证列表")
    private List<UnbalancedVoucherItem> unbalancedVouchers;

    @Schema(description = "缺失凭证总数")
    private Integer missingCount;

    @Schema(description = "不平衡凭证总数")
    private Integer unbalancedCount;

    @Schema(description = "缺失凭证的业务单据明细")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MissingVoucherItem {

        @Schema(description = "业务类型", example = "21")
        private Integer bizType;

        @Schema(description = "业务类型名称", example = "销售出库")
        private String bizTypeName;

        @Schema(description = "业务单据编号", example = "1")
        private Long bizId;

        @Schema(description = "业务单号", example = "XSCK20260429000001")
        private String bizNo;

        @Schema(description = "单据状态", example = "已审核")
        private String statusName;

        @Schema(description = "账簿名称", example = "默认账簿")
        private String ledgerName;
    }

    @Schema(description = "借贷不平衡的凭证明细")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UnbalancedVoucherItem {

        @Schema(description = "凭证编号", example = "1")
        private Long voucherId;

        @Schema(description = "凭证号", example = "CWPZ20260429000001")
        private String voucherNo;

        @Schema(description = "借方合计")
        private java.math.BigDecimal totalDebit;

        @Schema(description = "贷方合计")
        private java.math.BigDecimal totalCredit;

        @Schema(description = "差额")
        private java.math.BigDecimal difference;
    }
}
