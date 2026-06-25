package cn.weitee.erp.module.erp.controller.admin.finance.vo.prepayment;

import cn.weitee.erp.module.erp.controller.admin.finance.vo.apstatement.ErpApStatementRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - ERP 预付款追溯 Response VO")
@Data
public class ErpFinancePrepaymentTraceRespVO {

    @Schema(description = "预付款详情")
    private ErpFinancePrepaymentRespVO prepayment;

    @Schema(description = "关联应付台账列表")
    private List<ErpApStatementRespVO> statements;

    @Schema(description = "核销记录列表")
    private List<AllocateItem> allocates;

    @Data
    public static class AllocateItem {

        @Schema(description = "编号", example = "1")
        private Long id;

        @Schema(description = "预付款编号", example = "1")
        private Long prepaymentId;

        @Schema(description = "应付台账编号", example = "1")
        private Long apStatementId;

        @Schema(description = "核销金额", example = "100.00")
        private BigDecimal allocateAmount;

        @Schema(description = "供应商编号", example = "1")
        private Long supplierId;

        @Schema(description = "业务类型", example = "11")
        private Integer bizType;

        @Schema(description = "业务编号", example = "1")
        private Long bizId;

        @Schema(description = "业务单号", example = "PI-001")
        private String bizNo;

        @Schema(description = "状态", example = "20")
        private Integer status;

        @Schema(description = "状态名称", example = "已生效")
        private String statusName;

        @Schema(description = "备注", example = "预付核销")
        private String remark;

        @Schema(description = "创建时间")
        private LocalDateTime createTime;

    }

}
