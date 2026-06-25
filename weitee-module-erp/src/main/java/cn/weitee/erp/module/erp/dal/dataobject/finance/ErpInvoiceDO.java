package cn.weitee.erp.module.erp.dal.dataobject.finance;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpCustomerDO;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpSaleOrderDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ERP 销项发票 DO
 *
 * @author system
 */
@TableName("erp_invoice")
@KeySequence("erp_invoice_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpInvoiceDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;

    /**
     * 发票号
     */
    private String no;

    /**
     * 发票状态
     *
     * 枚举值：
     * - DRAFT: 草稿
     * - ISSUED: 已开票
     * - VOIDED: 已作废
     */
    private String status;

    /**
     * 客户编号
     *
     * 关联 ErpCustomerDO.id
     */
    private Long customerId;

    /**
     * 销售订单编号
     *
     * 关联 ErpSaleOrderDO.id
     */
    private Long orderId;

    /**
     * 销售订单号（冗余）
     */
    private String orderNo;

    /**
     * 开票时间
     */
    private LocalDateTime invoiceTime;

    /**
     * 开票人编号
     */
    private Long invoiceUserId;

    /**
     * 发票类型
     *
     * 枚举值：
     * - NORMAL: 普通发票
     * - SPECIAL: 增值税专用发票
     */
    private String invoiceType;

    /**
     * 发票抬头
     */
    private String invoiceTitle;

    /**
     * 纳税人识别号
     */
    private String taxpayerNo;

    /**
     * 开票金额（不含税）
     */
    private BigDecimal amountWithoutTax;

    /**
     * 税额
     */
    private BigDecimal taxAmount;

    /**
     * 价税合计
     */
    private BigDecimal totalAmount;

    /**
     * 备注
     */
    private String remark;

}
