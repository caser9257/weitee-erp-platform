package cn.iocoder.yudao.module.erp.dal.dataobject.project;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 项目生命周期时间线 DO
 *
 * @author system
 */
@TableName("erp_project_lifecycle_timeline")
@KeySequence("erp_project_lifecycle_timeline_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpProjectLifecycleTimelineDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;

    /**
     * 项目编号
     *
     * 关联 ErpProjectDO.id
     */
    private Long projectId;

    /**
     * 阶段编码
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
    private String stageCode;

    /**
     * 阶段名称
     */
    private String stageName;

    /**
     * 发生时间
     */
    private LocalDateTime happenTime;

    /**
     * 操作人编号
     */
    private Long operatorId;

    /**
     * 操作人姓名
     */
    private String operatorName;

    /**
     * 备注
     */
    private String remark;

}
