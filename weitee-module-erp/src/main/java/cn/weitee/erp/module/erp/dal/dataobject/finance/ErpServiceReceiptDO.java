package cn.weitee.erp.module.erp.dal.dataobject.finance;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 服务接收鍗?DO
 *
 * @author weitee
 */
@TableName("erp_service_receipt")
@KeySequence("erp_service_receipt_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpServiceReceiptDO extends BaseDO {

    @TableId
    private Long id;

    /**
     * 单号
     */
    private String no;

    /**
     * 租赁合同ID
     */
    private Long leaseContractId;

    /**
     * 租赁合同编号（冗余）
     */
    private String leaseContractNo;

    /**
     * 供应商ID
     */
    private Long supplierId;

    /**
     * 供应商名称（冗余锛?
     */
    private String supplierName;

    /**
     * 接收日期
     */
    private LocalDate receiptDate;

    /**
     * 归属期间（YYYY-MM锛?
     */
    private String period;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 成本中心ID（部门ID锛?
     */
    private Long costCenterId;

    /**
     * 成本中心名称（冗余）
     */
    private String costCenterName;

    /**
     * 状态：0-草稿 10-已确璁?20-已生成应浠?
     */
    private Integer status;

    /**
     * 生成的应付台账ID
     */
    private Long apStatementId;

    /**
     * 备注
     */
    private String remark;

    /**
     * 附件URL
     */
    private String fileUrl;

}
