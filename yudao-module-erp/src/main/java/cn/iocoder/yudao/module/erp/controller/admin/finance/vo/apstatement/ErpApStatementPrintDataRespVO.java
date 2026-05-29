package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apstatement;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - ERP 应付台账打印数据 Response VO")
@Data
public class ErpApStatementPrintDataRespVO {

    @Schema(description = "应付台账详情")
    private ErpApStatementRespVO statement;

    @Schema(description = "来源附件列表")
    private List<AttachmentLink> sourceAttachments;

    @Data
    public static class AttachmentLink {

        @Schema(description = "附件名称", example = "采购入库单附件.docx")
        private String name;

        @Schema(description = "附件地址", example = "https://www.iocoder.cn/1.doc")
        private String url;

    }

}
