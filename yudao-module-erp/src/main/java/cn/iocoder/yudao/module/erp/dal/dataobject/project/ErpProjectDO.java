package cn.iocoder.yudao.module.erp.dal.dataobject.project;

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

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ERP 项目 DO
 */
@TableName("erp_project")
@KeySequence("erp_project_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpProjectDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 项目编号
     */
    private String no;
    /**
     * 项目名称
     */
    private String name;
    /**
     * 项目类型
     */
    private String projectType;
    /**
     * 业务类型
     */
    private String businessType;
    /**
     * 来源类型
     */
    private String sourceType;
    /**
     * 来源项目编号
     */
    private Long sourceProjectId;
    /**
     * 销售订单编号
     */
    private Long saleOrderId;
    /**
     * 项目经理
     */
    private Long projectManagerId;
    /**
     * 计划负责人
     */
    private Long planCoordinatorId;
    /**
     * 物控负责人
     */
    private Long materialControllerId;
    /**
     * 归属部门
     */
    private Long ownerDeptId;
    /**
     * 当前阶段编码
     */
    private String currentStageCode;
    /**
     * 风险等级
     */
    private String riskLevel;
    /**
     * 客户编号
     */
    private Long customerId;
    /**
     * 状态
     */
    private Integer status;
    /**
     * PC状态
     */
    private String pcStatus;
    /**
     * MC状态
     */
    private String mcStatus;
    /**
     * PC确认时间
     */
    private LocalDateTime pcConfirmTime;
    /**
     * MC确认时间
     */
    private LocalDateTime mcConfirmTime;
    /**
     * PC备注
     */
    private String pcRemark;
    /**
     * MC备注
     */
    private String mcRemark;
    /**
     * 交期
     */
    private LocalDate deliveryDate;
    /**
     * 备注
     */
    private String remark;

    // ========== 生命周期字段（销售执行闭环） ==========

    /**
     * 当前生命周期阶段
     *
     * 枚举值：
     * - CONTRACT: 合同
     * - ORDER: 订单
     * - PAYMENT: 收款
     * - SHIPMENT: 发货
     * - OUTBOUND: 出库
     * - INVOICE: 开票
     * - CLOSED: 关闭
     * - CONTRACT_REJECTED: 合同驳回
     * - BLOCKED: 阻塞
     * - RETURNED: 退货
     * - DISPUTED: 争议
     */
    private String lifecycleStage;
    /**
     * 当前阻塞项
     */
    private String currentBlocker;
    /**
     * 当前待办角色
     */
    private String currentPendingRole;
    /**
     * 合同执行状态
     */
    private String contractExecStatus;
    /**
     * 收款执行状态
     */
    private String receiptExecStatus;
    /**
     * 发货执行状态
     */
    private String shipmentExecStatus;
    /**
     * 开票执行状态
     */
    private String invoiceExecStatus;

}
