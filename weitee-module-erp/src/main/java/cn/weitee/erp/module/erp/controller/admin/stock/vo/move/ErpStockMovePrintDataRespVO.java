package cn.weitee.erp.module.erp.controller.admin.stock.vo.move;

import lombok.Data;

import java.util.List;

@Data
public class ErpStockMovePrintDataRespVO {

    private ErpStockMoveRespVO stockMove;

    private List<SourceAttachment> sourceAttachments;

    @Data
    public static class SourceAttachment {

        private String name;
        private String url;

    }

}
