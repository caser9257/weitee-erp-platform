package cn.weitee.erp.module.mes.controller.admin.vo.taskexecution;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/** 现场执行报工请求。 */
@Data
public class MesTaskExecutionReportReqVO {

    @NotBlank(message = "任务号不能为空")
    private String taskNo;

    @NotNull(message = "报工数量不能为空")
    @DecimalMin(value = "0.000001", message = "报工数量必须大于 0")
    private BigDecimal reportedQty;

    @NotNull(message = "合格数量不能为空")
    @DecimalMin(value = "0", message = "合格数量不能小于 0")
    private BigDecimal qualifiedQty;

    @NotNull(message = "报废数量不能为空")
    @DecimalMin(value = "0", message = "报废数量不能小于 0")
    private BigDecimal scrapQty;

    @DecimalMin(value = "0", message = "工时不能小于 0")
    private BigDecimal workHour;

    @Size(max = 64, message = "批次号不能超过64个字符")
    private String batchNo;

    @Size(max = 255, message = "备注不能超过255个字符")
    private String remark;
}
