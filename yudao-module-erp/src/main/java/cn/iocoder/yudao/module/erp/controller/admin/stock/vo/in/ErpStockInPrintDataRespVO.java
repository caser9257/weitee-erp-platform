package cn.iocoder.yudao.module.erp.controller.admin.stock.vo.in;

import lombok.Data;

import java.util.List;

@Data
public class ErpStockInPrintDataRespVO {

    private ErpStockInRespVO stockIn;

    private List<SourceAttachment> sourceAttachments;

    @Data
    public static class SourceAttachment {

        private String name;
        private String url;

    }

}
