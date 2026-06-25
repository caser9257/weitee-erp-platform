package cn.weitee.erp.module.erp.dal.dataobject.finance;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@TableName("erp_ap_invoice_match_item")
@KeySequence("erp_ap_invoice_match_item_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpApInvoiceMatchItemDO extends BaseDO {

    @TableId
    private Long id;

    private Long invoiceId;
    private Long apStatementId;
    private Long sourceOrderId;
    private String sourceOrderNo;
    private Long sourcePurchaseInId;
    private String sourcePurchaseInNo;
    private Long sourcePurchaseInItemId;
    private Long productId;
    private Long supplierId;
    private BigDecimal matchCount;
    private BigDecimal matchAmount;
    private Integer status;
    private String remark;

}
