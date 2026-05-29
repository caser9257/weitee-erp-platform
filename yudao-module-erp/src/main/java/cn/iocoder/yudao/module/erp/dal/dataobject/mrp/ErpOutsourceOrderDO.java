package cn.iocoder.yudao.module.erp.dal.dataobject.mrp;

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

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("erp_outsource_order")
@KeySequence("erp_outsource_order_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpOutsourceOrderDO extends BaseDO {

    @TableId
    private Long id;

    private String no;

    private Integer orderType;

    private Long supplierId;

    private Long productId;

    private Long bomId;

    private Long projectId;

    private String processName;

    private BigDecimal plannedQty;

    private BigDecimal issuedQty;

    private BigDecimal returnedQty;

    private BigDecimal finishedQty;

    private BigDecimal lossQty;

    private Integer status;

    private String closeRemark;

    private LocalDateTime closeTime;

    private String remark;

}
