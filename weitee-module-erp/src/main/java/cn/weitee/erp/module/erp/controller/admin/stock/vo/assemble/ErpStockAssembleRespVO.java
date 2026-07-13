package cn.weitee.erp.module.erp.controller.admin.stock.vo.assemble;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ErpStockAssembleRespVO {

    private Long id;
    private String no;
    private String actionType;
    private String actionTypeName;
    private Long warehouseId;
    private String warehouseName;
    private Long productId;
    private String productName;
    private Long bomId;
    private BigDecimal count;
    private BigDecimal totalCost;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
    private List<Item> items;

    @Data
    public static class Item {
        private Long id;
        private Long productId;
        private String productName;
        private BigDecimal count;
        private BigDecimal unitCost;
        private Integer stockDirection;
    }
}
