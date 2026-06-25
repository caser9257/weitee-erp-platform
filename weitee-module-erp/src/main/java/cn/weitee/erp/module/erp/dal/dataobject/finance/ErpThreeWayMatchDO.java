package cn.weitee.erp.module.erp.dal.dataobject.finance;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/**
 * 三单匹配 DO
 *
 * @author ruoyi-vue-pro
 */
@TableName("erp_three_way_match")
@KeySequence("erp_three_way_match_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpThreeWayMatchDO extends BaseDO {

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
     * 服务接收单ID
     */
    private Long serviceReceiptId;

    /**
     * 服务接收单编号
     */
    private String serviceReceiptNo;

    /**
     * 发票号
     */
    private String invoiceNo;

    /**
     * 发票金额
     */
    private BigDecimal invoiceAmount;

    /**
     * 合同金额
     */
    private BigDecimal contractAmount;

    /**
     * 接收单金额
     */
    private BigDecimal receiptAmount;

    /**
     * 匹配结果：0-不匹配 1-完全匹配 2-部分匹配
     */
    private Integer matchResult;

    /**
     * 匹配说明
     */
    private String matchRemark;

    /**
     * 生成的应付台账ID
     */
    private Long apStatementId;

    /**
     * 状态：0-待匹配 10-已匹配 20-已生成应付
     */
    private Integer status;

}
