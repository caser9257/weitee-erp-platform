package cn.iocoder.yudao.module.erp.dal.dataobject.mrp;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@TableName("erp_mrp_shortage")
@KeySequence("erp_mrp_shortage_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpMrpShortageDO extends BaseDO {

    @TableId
    private Long id;

    private Long planId;

    private Long traceNodeId;

    private Long rootProductId;

    private Long materialId;

    private String tracePathKey;

    private Integer traceLevel;

    private Long parentMaterialId;

    private Long bomItemId;

    private BigDecimal shortageQty;

    private LocalDate requiredDate;

    private Long sourceOrderId;

    private Long sourceItemId;

}
