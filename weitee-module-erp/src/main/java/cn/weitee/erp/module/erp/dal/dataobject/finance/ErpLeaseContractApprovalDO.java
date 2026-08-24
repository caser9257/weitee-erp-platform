package cn.weitee.erp.module.erp.dal.dataobject.finance;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 绉熻祦鍚堝悓瀹℃壒璁板綍 DO
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
     * 绉熻祦鍚堝悓ID
     */
    private Long leaseContractId;

    /**
     * 绉熻祦鍚堝悓缂栧彿
     */
    private String leaseContractNo;

    /**
     * 鎿嶄綔锛歋UBMIT/APPROVE/REJECT
     */
    private String action;

    /**
     * 鎿嶄綔鍓嶇姸鎬?
     */
    private Integer statusBefore;

    /**
     * 鎿嶄綔鍚庣姸鎬?
     */
    private Integer statusAfter;

    /**
     * 鎿嶄綔浜篒D
     */
    private Long operatorId;

    /**
     * 鎿嶄綔浜哄悕绉?
     */
    private String operatorName;

    /**
     * 澶囨敞
     */
    private String remark;

}
