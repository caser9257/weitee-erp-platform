package cn.weitee.erp.module.erp.controller.admin.finance.vo.prepayment;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - ERP 预付款 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ErpFinancePrepaymentRespVO {

    @Schema(description = "编号", example = "23752")
    private Long id;

    @Schema(description = "预付款单号", example = "YFK20260427000001")
    private String no;

    @Schema(description = "状态", example = "10")
    private Integer status;

    @Schema(description = "预付款时间")
    private LocalDateTime prepaymentTime;

    @Schema(description = "财务人员编号", example = "19690")
    private Long financeUserId;

    @Schema(description = "财务人员名称", example = "张三")
    private String financeUserName;

    @Schema(description = "供应商编号", example = "29399")
    private Long supplierId;

    @Schema(description = "供应商名称", example = "小番茄有限公司")
    private String supplierName;

    @Schema(description = "结算账户编号", example = "28989")
    private Long accountId;

    @Schema(description = "结算账户名称", example = "中国银行")
    private String accountName;

    @Schema(description = "预付金额", example = "10000")
    private BigDecimal prepaymentPrice;

    @Schema(description = "已核销金额", example = "2000")
    private BigDecimal allocatedPrice;

    @Schema(description = "剩余金额", example = "8000")
    private BigDecimal remainPrice;

    @Schema(description = "备注", example = "预付")
    private String remark;

    @Schema(description = "创建人", example = "1")
    private String creator;

    @Schema(description = "创建人名称", example = "芋道")
    private String creatorName;

    @Schema(description = "创建时间")
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "核销明细列表")
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
