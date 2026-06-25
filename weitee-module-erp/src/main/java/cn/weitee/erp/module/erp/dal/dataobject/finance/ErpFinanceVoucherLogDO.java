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

@TableName("erp_finance_voucher_log")
@KeySequence("erp_finance_voucher_log_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class ErpFinanceVoucherLogDO extends BaseDO {

    @TableId
    private Long id;

    /**
     * 凭证编号
     */
    private Long voucherId;

    /**
     * 操作类型（生成、审核、过账等）
     */
    private String operationType;

    /**
     * 操作结果
     */
    private String operationResult;

    /**
     * 操作详情
     */
    private String operationDetail;

    /**
     * 操作人
     */
    private String operator;

    /**
     * 备注
     */
    private String remark;
}
