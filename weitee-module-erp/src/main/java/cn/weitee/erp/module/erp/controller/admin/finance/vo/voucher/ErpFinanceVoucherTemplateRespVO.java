package cn.weitee.erp.module.erp.controller.admin.finance.vo.voucher;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 财务凭证模板 Response VO")
@Data
public class ErpFinanceVoucherTemplateRespVO {

    private Long id;
    private Long ledgerId;
    private String ledgerName;
    private Integer bizType;
    private String bizTypeName;
    private String name;
    private Integer status;
    private Boolean autoGenerate;
    private String defaultSummary;
    private String remark;
    private Integer researchCategory;
    private String researchCategoryName;
    private Boolean researchTemplate;
    private LocalDateTime createTime;
    private List<Item> items;

    @Data
    public static class Item {
        private Long id;
        private Integer entryNo;
        private Integer entryDirection;
        private String entryDirectionName;
        private String subjectCode;
        private String subjectName;
        private Integer amountSource;
        private String amountSourceName;
        private BigDecimal amountSourceValue;
        private String summary;
    }
}
