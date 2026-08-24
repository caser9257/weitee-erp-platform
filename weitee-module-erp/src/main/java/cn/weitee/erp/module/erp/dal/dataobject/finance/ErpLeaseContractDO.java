package cn.weitee.erp.module.erp.dal.dataobject.finance;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 租赁合同 DO
 *
 * @author weitee
 */
@TableName("erp_lease_contract")
@KeySequence("erp_lease_contract_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpLeaseContractDO extends BaseDO {

    @TableId
    private Long id;

    /**
     * 合同编号
     */
    private String no;

    /**
     * 合同名称
     */
    private String name;

    /**
     * 供应商ID
     */
    private Long supplierId;

    /**
     * 供应商名称（冗余锛?
     */
    private String supplierName;

    /**
     * 开始日鏈?
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    /**
     * 月租閲?
     */
    private BigDecimal monthlyRent;

    /**
     * 付款周期（月锛?
     */
    private Integer paymentCycle;

    /**
     * 合同总金棰?
     */
    private BigDecimal totalAmount;

    /**
     * 成本中心ID（部门ID锛?
     */
    private Long costCenterId;

    /**
     * 状态：0-草稿 5-审批涓?10-生效 20-到期 30-终止
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 合同附件URL
     */
    private String fileUrl;

}
