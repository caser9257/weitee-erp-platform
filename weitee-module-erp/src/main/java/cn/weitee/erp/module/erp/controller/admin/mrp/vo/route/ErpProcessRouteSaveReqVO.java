package cn.weitee.erp.module.erp.controller.admin.mrp.vo.route;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class ErpProcessRouteSaveReqVO {
    private Long id;
    @NotBlank(message = "工艺编码不能为空") private String routeCode;
    @NotBlank(message = "工艺名称不能为空") private String routeName;
    @NotNull(message = "产品不能为空") private Long productId;
    @NotBlank(message = "版本不能为空") private String version;
    private Boolean defaultFlag;
    private LocalDate effectiveDate;
    private LocalDate expireDate;
    private String remark;
    @Valid @NotEmpty(message = "工序不能为空") private List<Step> steps;

    @Data
    public static class Step {
        @NotNull(message = "工序编号不能为空") private Integer stepNo;
        @NotBlank(message = "工序编码不能为空") private String stepCode;
        @NotBlank(message = "工序名称不能为空") private String stepName;
        private Long workCenterId;
        private Boolean outsourceFlag;
        private Boolean qcFlag;
        private Boolean reportRequired;
        private Boolean inspectRequired;
        private BigDecimal prepareTime;
        private BigDecimal processTime;
        private BigDecimal moveTime;
        private BigDecimal waitTime;
        private BigDecimal batchSize;
        private Integer sort;
        private String remark;
    }
}
