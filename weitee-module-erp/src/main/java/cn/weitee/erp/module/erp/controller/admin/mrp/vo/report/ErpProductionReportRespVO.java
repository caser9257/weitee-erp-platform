package cn.weitee.erp.module.erp.controller.admin.mrp.vo.report;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ErpProductionReportRespVO {

    private Long id;

    private String reportNo;

    private Long productionOrderId;

    private String productionOrderNo;

    private LocalDateTime reportDate;

    private Long reportUserId;

    private Integer reportType;

    private String batchNo;

    private Integer status;

    private String remark;

    private LocalDateTime createTime;

    private List<Item> items;

    @Data
    public static class Item {

        private Long id;

        private Long productionOrderStepId;

        private String stepCode;

        private String stepName;

        private Long deviceId;

        private Long workerUserId;

        private BigDecimal reportedQty;

        private BigDecimal qualifiedQty;

        private BigDecimal scrapQty;

        private BigDecimal workHour;

        private String batchNo;

        private String remark;
    }

}
