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
import java.time.LocalDateTime;

@TableName("erp_ap_statement")
@KeySequence("erp_ap_statement_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpApStatementDO extends BaseDO {

    @TableId
    private Long id;

    private String statementNo;
    private Integer bizType;
    private Long bizId;
    private String bizNo;
    private Long sourceOrderId;
    private String sourceOrderNo;
    private Long supplierId;
    private Long accountId;
    private BigDecimal amount;
    private BigDecimal paidAmount;
    private BigDecimal remainAmount;
    private String currencyCode;
    private LocalDateTime bizDate;
    private LocalDateTime dueDate;
    private Integer invoiceStatus;
    private String invoiceNo;
    private BigDecimal invoiceAmount;
    private Integer status;
    private String remark;

}
