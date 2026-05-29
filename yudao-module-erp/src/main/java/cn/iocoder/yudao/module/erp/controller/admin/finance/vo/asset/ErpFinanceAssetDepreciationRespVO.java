package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.asset;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ErpFinanceAssetDepreciationRespVO {

    private Long id;
    private Long assetId;
    private String assetNo;
    private String period;
    private BigDecimal depreciationAmount;
    private BigDecimal beforeDepreciatedAmount;
    private BigDecimal afterDepreciatedAmount;
    private BigDecimal beforeCurrentAmount;
    private BigDecimal afterCurrentAmount;
    private Integer status;
    private String remark;
    private String creator;
    private LocalDateTime createTime;
}
