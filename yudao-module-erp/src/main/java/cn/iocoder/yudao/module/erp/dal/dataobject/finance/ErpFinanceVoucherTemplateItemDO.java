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

@TableName("erp_finance_voucher_template_item")
@KeySequence("erp_finance_voucher_template_item_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpFinanceVoucherTemplateItemDO extends BaseDO {

    @TableId
    private Long id;

    private Long templateId;

    private Integer entryNo;

    private Integer entryDirection;

    private String subjectCode;

    private String subjectName;

    private Integer amountSource;

    private BigDecimal amountSourceValue;

    private String summary;
}
