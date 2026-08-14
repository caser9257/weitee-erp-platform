package cn.weitee.erp.module.erp.dal.dataobject.finance;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 租赁合同审批记录 DO
 *
 * @author weitee
 */
@TableName("erp_lease_contract_approval")
@KeySequence("erp_lease_contract_approval_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpLeaseContractApprovalDO extends BaseDO {

    @TableId
    private Long id;

    /**
     * 租赁合同ID
     */
    private Long leaseContractId;

    /**
     * 租赁合同编号
     */
    private String leaseContractNo;

    /**
     * 操作：SUBMIT/APPROVE/REJECT
     */
    private String action;

    /**
     * 操作前状�?
     */
    private Integer statusBefore;

    /**
     * 操作后状�?
     */
    private Integer statusAfter;

    /**
     * 操作人ID
     */
    private Long operatorId;

    /**
     * 操作人名�?
     */
    private String operatorName;

    /**
     * 备注
     */
    private String remark;

}
