package cn.iocoder.yudao.module.erp.dal.dataobject.finance;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/**
 * ERP 销项发票明细 DO
 *
 * @author system
 */
@TableName("erp_invoice_item")
@KeySequence("erp_invoice_item_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpInvoiceItemDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;

    /**
     * 发票编号
     *
     * 关联 ErpInvoiceDO.id
     */
    private Long invoiceId;

    /**
     * 产品编号
     */
    private Long productId;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 产品规格
     */
    private String productSpec;

    /**
     * 单位
     */
    private String unit;

    /**
     * 数量
     */
    private BigDecimal count;

    /**
     * 单价（不含税）
     */
    private BigDecimal price;

    /**
     * 金额（不含税）
     */
    private BigDecimal amount;

    /**
     * 税率
     */
    private BigDecimal taxRate;

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
