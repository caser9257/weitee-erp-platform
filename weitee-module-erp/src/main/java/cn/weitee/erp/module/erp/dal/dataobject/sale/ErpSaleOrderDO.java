package cn.weitee.erp.module.erp.dal.dataobject.sale;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpAccountDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ERP 销售订单 DO
 *
 * @author WeTai
 */
@TableName(value = "erp_sale_order")
@KeySequence("erp_sale_order_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpSaleOrderDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 销售订单号
     */
    private String no;
    /**
     * 销售状态
     *
     * 枚举 {@link cn.weitee.erp.module.erp.enums.ErpAuditStatus}
     */
    private Integer status;
    /**
     * BPM 流程实例编号
     */
    private String processInstanceId;
    /**
     * 客户编号
     *
     * 关联 {@link ErpCustomerDO#getId()}
     */
    private Long customerId;
    /**
     * 项目编号
     */
    private Long projectId;
    /**
     * 业务类型
     */
    private String businessType;
    /**
     * 来源研发项目编号
     */
    private Long sourceProjectId;
    /**
     * 结算类型
     */
    private String settlementType;
    /**
     * 来源产品编号
     */
    private Long sourceProductId;
    /**
     * 结算账户编号
     *
     * 关联 {@link ErpAccountDO#getId()}
     */
    private Long accountId;
    /**
     * 销售员编号
     *
     * 关联 AdminUserDO 的 id 字段
     */
    private Long saleUserId;
    /**
     * 下单时间
     */
    private LocalDateTime orderTime;
    /**
     * 交期
     */
    private LocalDate deliveryDate;

    /**
     * 合计数量
     */
    private BigDecimal totalCount;
    /**
     * 最终合计价格，单位：元
     *
     * totalPrice = totalProductPrice + totalTaxPrice - discountPrice
     */
    private BigDecimal totalPrice;

    /**
     * 合计产品价格，单位：元
     */
    private BigDecimal totalProductPrice;
    /**
     * 合计税额，单位：元
     */
    private BigDecimal totalTaxPrice;
    /**
     * 优惠率，百分比
     */
    private BigDecimal discountPercent;
    /**
     * 优惠金额，单位：元
     *
     * discountPrice = (totalProductPrice + totalTaxPrice) * discountPercent
     */
    private BigDecimal discountPrice;
    /**
     * 定金金额，单位：元
     */
    private BigDecimal depositPrice;
    /**
     * 最近一次驳回原因
     */
    private String lastRejectReason;
    /**
     * 最近一次驳回时间
     */
    private LocalDateTime lastRejectTime;
    /**
     * 最近一次驳回人编号
     */
    private Long lastRejectUserId;

    /**
     * 附件地址
     */
    private String fileUrl;
    /**
     * 备注
     */
    private String remark;

    // ========== 销售出库 ==========
    /**
     * 销售出库数量
     */
    private BigDecimal outCount;

    // ========== 销售退货（入库）） ==========
    /**
     * 销售退货数量
     */
    private BigDecimal returnCount;

    private String deliveryReadyStatus;

    // ========== 合同关联与放行状态（销售执行闭环） ==========

    /**
     * 关联合同编号
     *
     * 关联 crm_contract.id
     */
    private Long contractId;
    /**
     * 合同编号（冗余存储）
     */
    private String contractNo;
    /**
     * 发货放行状态
     *
     * 枚举值：
     * - PENDING: 待校验
     * - BLOCKED: 阻塞
     * - RELEASED: 已放行
     * - FINANCE_REVIEW: 待财务审核
     */
    private String shipmentReleaseStatus;
    /**
     * 放行阻塞原因
     */
    private String shipmentReleaseReason;
    /**
     * 开票状态
     *
     * 枚举值：
     * - NOT_INVOICED: 未开票
     * - PARTIAL_INVOICED: 部分开票
     * - FULLY_INVOICED: 全额开票
     */
    private String invoiceStatus;
    /**
     * 验收状态
     *
     * 枚举值：
     * - NOT_REQUIRED: 无需验收
     * - PENDING: 待验收
     * - ACCEPTED: 已验收
     * - REJECTED: 验收不通过
     */
    private String acceptanceStatus;

    // ========== 收款状态（财务闭环） ==========

    /**
     * 收款状态
     *
     * 枚举值：
     * - 0: 未收款
     * - 1: 部分收款
     * - 2: 全额收款
     */
    private Integer receiptStatus;
    /**
     * 已收款金额，单位：元
     */
    private BigDecimal receiptPrice;

}
