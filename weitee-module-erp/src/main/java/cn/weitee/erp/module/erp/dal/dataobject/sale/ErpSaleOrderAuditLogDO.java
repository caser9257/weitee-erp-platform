package cn.weitee.erp.module.erp.dal.dataobject.sale;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 销售订单审批流转日志 DO
 */
@TableName("erp_sale_order_audit_log")
@KeySequence("erp_sale_order_audit_log_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpSaleOrderAuditLogDO extends BaseDO {

    @TableId
    private Long id;

    private Long orderId;

    private String actionType;

    private Integer beforeStatus;

    private Integer afterStatus;

    private String reason;

}
