package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.in;

import lombok.Data;

import java.util.List;

@Data
public class ErpPurchaseInPrintDataRespVO {

    private ErpPurchaseInRespVO purchaseIn;

    private List<SourceAttachment> sourceAttachments;

    @Data
    public static class SourceAttachment {

        private String name;
        private String url;

    }

}
