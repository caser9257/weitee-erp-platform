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

/**
 * ERP 暂估单 DO
 */
@TableName("erp_ap_estimate")
@KeySequence("erp_ap_estimate_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpApEstimateDO extends BaseDO {

    @TableId
    private Long id;

    private String estimateNo;
    private String estimateMonth;

    private Integer sourceBizType;
    private Long sourceBizId;
    private String sourceBizNo;

    private Long sourcePurchaseInId;
    private String sourcePurchaseInNo;

    private Long sourceOrderId;
    private String sourceOrderNo;

    private Long supplierId;
    private Long accountId;
    private String currencyCode;

    /**
     * 来源含税前金额，单位：元
     */
    private BigDecimal sourceAmount;

    /**
     * 暂估金额，单位：元
     */
    private BigDecimal amount;

    private Integer status;

    private Long confirmUserId;
    private LocalDateTime confirmTime;

    private Long reverseUserId;
    private LocalDateTime reverseTime;
    private Integer reverseType;
    private Long reverseSourceId;
    private String reverseSourceNo;
    private String reverseRemark;

    private String remark;

}
