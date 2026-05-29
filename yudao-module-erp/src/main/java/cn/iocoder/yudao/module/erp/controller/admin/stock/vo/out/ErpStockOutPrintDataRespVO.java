package cn.iocoder.yudao.module.erp.controller.admin.stock.vo.out;

import lombok.Data;

import java.util.List;

@Data
public class ErpStockOutPrintDataRespVO {

    private ErpStockOutRespVO stockOut;

    private List<SourceAttachment> sourceAttachments;

    @Data
    public static class SourceAttachment {

        private String name;
        private String url;

    }

}
