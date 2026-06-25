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

/**
 * ERP 双账套账簿映射配置 DO
 */
@TableName("erp_finance_dual_ledger_config")
@KeySequence("erp_finance_dual_ledger_config_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class ErpFinanceDualLedgerConfigDO extends BaseDO {

    @TableId
    private Long id;

    private Integer bizType;

    private Long externalLedgerId;

    private Long internalLedgerId;

    private Integer status;

    private String remark;

}
