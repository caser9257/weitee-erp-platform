package cn.weitee.erp.module.erp.controller.admin.sale.vo.out;

import lombok.Data;

import java.util.List;

@Data
public class ErpSaleOutPrintDataRespVO {

    private ErpSaleOutRespVO saleOut;

    private List<SourceAttachment> sourceAttachments;

    @Data
    public static class SourceAttachment {

        private String name;

        private String url;

    }

}
