package cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.rule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "管理后台 - ERP 计划规则新增/修改 Request VO")
@Data
public class ErpMaterialPlanRuleSaveReqVO {

    private Long id;

    @NotNull(message = "产品不能为空")
    private Long productId;

    @NotBlank(message = "供给方式不能为空")
    private String supplyType;

    @NotBlank(message = "补货策略不能为空")
    private String replenishMode;

    private BigDecimal safetyStock;

    private BigDecimal minOrderQty;

    private BigDecimal orderMultiple;

    private BigDecimal fixedOrderQty;

    private Integer purchaseLeadDay;

    private Integer makeLeadDay;

    private Boolean enableFlag;

    private Boolean shortageWarnFlag;

    private Long defaultSupplierId;

    private String remark;

}
