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
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("erp_finance_voucher")
@KeySequence("erp_finance_voucher_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class ErpFinanceVoucherDO extends BaseDO {

    @TableId
    private Long id;

    private String voucherNo;

    private Long ledgerId;

    /**
     * 凭证生成时固化的业务归属部门；空值表示历史或来源无法可靠识别，待归属。
     */
    private Long deptId;

    private Long periodId;

    private Long templateId;

    private Integer bizType;

    private Long bizId;

    private String bizNo;

    private LocalDateTime voucherTime;

    private Integer status;

    private BigDecimal totalDebitAmount;

    private BigDecimal totalCreditAmount;

    private Long approveUserId;

    private LocalDateTime approveTime;

    private Long postUserId;

    private LocalDateTime postTime;

    private Long reverseUserId;

    private LocalDateTime reverseTime;

    private Long reverseVoucherId;

    private Long reverseFromVoucherId;

    private String reverseRemark;

    private String remark;
}
