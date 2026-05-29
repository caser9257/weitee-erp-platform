package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.inquality;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 采购入库 IQC 套打数据 Response VO")
@Data
public class ErpPurchaseInQualityPrintDataRespVO {

    @Schema(description = "质检单数据")
    private ErpPurchaseInQualityRespVO purchaseInQuality;

    @Schema(description = "来源附件")
    private List<SourceAttachment> sourceAttachments;

    @Schema(description = "来源附件")
    @Data
    public static class SourceAttachment {

        @Schema(description = "附件名称", example = "来料检验报告.pdf")
        private String name;

        @Schema(description = "附件地址", example = "https://example.com/attachment.pdf")
        private String url;

    }

}
