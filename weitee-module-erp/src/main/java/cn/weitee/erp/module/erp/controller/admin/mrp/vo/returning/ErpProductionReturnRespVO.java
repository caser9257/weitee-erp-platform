package cn.weitee.erp.module.erp.controller.admin.mrp.vo.returning;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ErpProductionReturnRespVO {
    private Long id;
    private String returnNo;
    private Long productionOrderId;
    private LocalDateTime returnTime;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
}
