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

@TableName("erp_finance_ledger_mapping")
@KeySequence("erp_finance_ledger_mapping_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class ErpFinanceLedgerMappingDO extends BaseDO {
    
    @TableId
    private Long id;
    
    /**
     * 对外账簿编号
     */
    private Long externalLedgerId;
    
    /**
     * 内部账簿编号
     */
    private Long internalLedgerId;
    
    /**
     * 映射类型
     */
    private String mappingType;
    
    /**
     * 映射规则
     */
    private String mappingRule;
    
    /**
     * 状态
     */
    private Integer status;
    
    /**
     * 备注
     */
    private String remark;
}