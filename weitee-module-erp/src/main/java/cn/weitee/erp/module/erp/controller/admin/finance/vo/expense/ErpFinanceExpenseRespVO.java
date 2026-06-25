package cn.weitee.erp.module.erp.controller.admin.finance.vo.expense;

import cn.idev.excel.annotation.ExcelIgnore;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - ERP 费用报销 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ErpFinanceExpenseRespVO {

    @Schema(description = "编号", example = "23752")
    private Long id;

    @Schema(description = "报销单号", example = "LSBX20260428000001")
    @ExcelProperty("报销单号")
    private String no;

    @Schema(description = "状态", example = "10")
    @ExcelProperty("状态")
    private Integer status;

    @Schema(description = "流程实例编号", example = "1789573096506331136")
    private String processInstanceId;

    @Schema(description = "报销时间")
    @ExcelProperty("报销时间")
    private LocalDateTime expenseTime;

    @Schema(description = "费用类型", example = "10")
    private Integer expenseType;

    @Schema(description = "费用类型名称", example = "研发费用")
    @ExcelProperty("费用类型")
    private String expenseTypeName;

    @Schema(description = "研发支出分类")
    @ExcelIgnore
    private Integer researchCategory;

    @Schema(description = "研发支出分类名称")
    @ExcelProperty("研发支出分类")
    private String researchCategoryName;

    @Schema(description = "研发支出口径（10-费用化，20-资本化）", example = "10")
    private Integer rdAccountingType;

    @Schema(description = "研发支出口径名称", example = "费用化")
    @ExcelProperty("研发口径")
    private String rdAccountingTypeName;

    @Schema(description = "部门编号", example = "1001")
    private Long deptId;

    @Schema(description = "部门名称", example = "研发部")
    @ExcelProperty("部门")
    private String deptName;

    @Schema(description = "项目编号", example = "1001")
    private Long projectId;

    @Schema(description = "项目名称", example = "高新研发项目")
    @ExcelProperty("项目")
    private String projectName;

    @Schema(description = "付款对象编号", example = "2001")
    private Long supplierId;

    @Schema(description = "付款对象名称", example = "张三供应服务")
    @ExcelProperty("付款对象")
    private String supplierName;

    @Schema(description = "财务人员编号", example = "19690")
    private Long financeUserId;

    @Schema(description = "财务人员名称", example = "张三")
    @ExcelProperty("财务人员")
    private String financeUserName;

    @Schema(description = "结算账户编号", example = "28989")
    private Long accountId;

    @Schema(description = "结算账户名称", example = "中国银行")
    @ExcelProperty("结算账户")
    private String accountName;

    @Schema(description = "报销金额，单位：元", example = "10000")
    @ExcelProperty("报销金额")
    private BigDecimal expensePrice;

    @Schema(description = "已付款金额，单位：元", example = "0")
    @ExcelProperty("已付款金额")
    private BigDecimal paidPrice;

    @Schema(description = "剩余金额，单位：元", example = "10000")
    @ExcelProperty("剩余金额")
    private BigDecimal remainPrice;

    @Schema(description = "备注", example = "零星采购")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "租赁合同编号", example = "LEASE-2026-001")
    @ExcelProperty("租赁合同编号")
    private String leaseContractNo;

    @Schema(description = "创建人", example = "1")
    private String creator;

    @Schema(description = "创建人名称", example = "芋道")
    @ExcelProperty("创建人")
    private String creatorName;

    @Schema(description = "创建时间")
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "费用明细列表")
    @ExcelIgnore
    private List<ErpFinanceExpenseItemRespVO> items;

}
