package cn.iocoder.yudao.module.crm.dal.dataobject.contract;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.contact.CrmContactDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import cn.iocoder.yudao.module.crm.enums.common.CrmAuditStatusEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * CRM 合同 DO
 *
 * @author dhb52
 */
@TableName("crm_contract")
@KeySequence("crm_contract_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrmContractDO extends BaseDO {

    /**
     * 合同编号
     */
    @TableId
    private Long id;
    /**
     * 合同名称
     */
    private String name;
    /**
     * 合同编号
     */
    private String no;
    /**
     * 客户编号
     *
     * 关联 {@link CrmCustomerDO#getId()}
     */
    private Long customerId;
    /**
     * 商机编号，非必须
     *
     * 关联 {@link CrmBusinessDO#getId()}
     */
    private Long businessId;

    /**
     * 最后跟进时间
     */
    private LocalDateTime contactLastTime;

    /**
     * 负责人的用户编号
     *
     * 关联 AdminUserDO 的 id 字段
     */
    private Long ownerUserId;

    /**
     * 工作流编号
     *
     * 关联 ProcessInstance 的 id 属性
     */
    private String processInstanceId;
    /**
     * 审批状态
     *
     * 枚举 {@link CrmAuditStatusEnum}
     */
    private Integer auditStatus;

    /**
     * 下单日期
     */
    private LocalDateTime orderDate;
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    /**
     * 产品总金额，单位：元
     */
    private BigDecimal totalProductPrice;
    /**
     * 整单折扣
     */
    private BigDecimal discountPercent;
    /**
     * 合同总金额，单位：分
     */
    private BigDecimal totalPrice;
    /**
     * 客户签约人，非必须
     *
     * 关联 {@link CrmContactDO#getId()}
     */
    private Long signContactId;
    /**
     * 公司签约人，非必须
     *
     * 关联 AdminUserDO 的 id 字段
     */
    private Long signUserId;
    /**
     * 备注
     */
    private String remark;

    // ========== 商业条款字段（销售执行闭环） ==========

    /**
     * 发货放行规则
     *
     * 枚举值：
     * - SIGN_AND_SHIP: 签约即发
     * - AFTER_PAYMENT: 到账后发
     * - AFTER_PREPAYMENT: 达到预付款比例后发
     * - FINANCE_APPROVAL: 财务审核后发
     */
    private String shipmentReleaseRule;
    /**
     * 开票触发条件
     *
     * 枚举值：
     * - PREPAYMENT_FULL: 预付款全额开票
     * - PREPAYMENT_PARTIAL: 预付款部分开票
     * - PREPAYMENT_ONLY: 仅预付款开票
     * - AFTER_SHIPMENT: 发货后开票
     * - AFTER_DELIVERY_RECEIPT: 交付收款后开票
     * - MANUAL: 手工决定
     */
    private String invoiceTrigger;
    /**
     * 收款规则
     *
     * 枚举值：
     * - BEFORE_SHIPMENT: 发货前付款
     * - ON_SHIPMENT: 发货时付款
     * - AFTER_SHIPMENT: 发货后约定期限付款
     */
    private String collectionRule;
    /**
     * 预付款金额，单位：元
     */
    private BigDecimal prepaymentAmount;
    /**
     * 预付款比例，单位：%
     */
    private BigDecimal prepaymentRatio;
    /**
     * 是否需要财务审核放行
     *
     * 枚举值：0-否，1-是
     * 业务确认：所有发货都需要财务审核，默认为1
     */
    private Integer financeApprovalRequired;
    /**
     * 是否需要验收
     *
     * 枚举值：0-否，1-是
     */
    private Integer acceptanceRequired;
    /**
     * 付款条件说明
     */
    private String paymentTerms;
    /**
     * 发货条件说明
     */
    private String shipmentConditions;
    /**
     * 开票条件说明
     */
    private String invoiceConditions;

}
