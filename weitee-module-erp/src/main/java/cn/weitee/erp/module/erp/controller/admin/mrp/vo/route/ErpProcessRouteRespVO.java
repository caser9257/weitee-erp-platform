package cn.weitee.erp.module.erp.controller.admin.mrp.vo.route;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ErpProcessRouteRespVO {
    private Long id;
    private String routeCode;
    private String routeName;
    private Long productId;
    private String productName;
    private String version;
    private Boolean defaultFlag;
    private Integer status;
    private LocalDate effectiveDate;
    private LocalDate expireDate;
    private String remark;
    private LocalDateTime createTime;
    private List<Step> steps;

    @Data
    public static class Step {
        private Long id;
        private Integer stepNo;
        private String stepCode;
        private String stepName;
        private Long workCenterId;
        private String workCenterName;
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
