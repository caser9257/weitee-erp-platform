package cn.iocoder.yudao.module.erp.dal.dataobject.finance;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
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
import java.time.LocalDateTime;

@TableName("erp_ap_invoice")
@KeySequence("erp_ap_invoice_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpApInvoiceDO extends BaseDO {

    @TableId
    private Long id;

    private Long supplierId;
    private String invoiceNo;
    private LocalDateTime invoiceDate;
    private Integer invoiceType;
    private BigDecimal totalCount;
    private BigDecimal matchedCount;
    private BigDecimal totalAmount;
    private BigDecimal matchedAmount;
    private BigDecimal unmatchedAmount;
    private BigDecimal toleranceAmount;
    private BigDecimal differenceAmount;
    private Integer matchStatus;
    private String differenceReason;
    private String remark;

}
