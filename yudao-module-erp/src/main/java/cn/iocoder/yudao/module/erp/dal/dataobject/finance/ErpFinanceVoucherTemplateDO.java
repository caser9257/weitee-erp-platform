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
import lombok.experimental.Accessors;

@TableName("erp_finance_voucher_template")
@KeySequence("erp_finance_voucher_template_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class ErpFinanceVoucherTemplateDO extends BaseDO {

    @TableId
    private Long id;

    private Long ledgerId;

    private Integer bizType;

    private String name;

    private Integer status;

    private Boolean autoGenerate;

    private String defaultSummary;

    private String remark;

    /**
     * 研发支出分类（费用化/资本化）
     */
    private Integer researchCategory;

    /**
     * 是否研发专项模板
     */
    private Boolean researchTemplate;
}
