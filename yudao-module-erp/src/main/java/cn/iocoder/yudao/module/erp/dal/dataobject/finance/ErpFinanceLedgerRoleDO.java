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

/**
 * ERP 账簿角色关联 DO
 */
@TableName("erp_finance_ledger_role")
@KeySequence("erp_finance_ledger_role_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class ErpFinanceLedgerRoleDO extends BaseDO {

    @TableId
    private Long id;

    /**
     * 账簿编号
     */
    private Long ledgerId;

    /**
     * 角色编号
     */
    private Long roleId;

    /**
     * 启用状态
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

}
