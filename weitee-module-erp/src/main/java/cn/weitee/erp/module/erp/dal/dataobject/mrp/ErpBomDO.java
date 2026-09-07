package cn.weitee.erp.module.erp.dal.dataobject.mrp;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@TableName("erp_bom")
@KeySequence("erp_bom_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpBomDO extends BaseDO {

    @TableId
    private Long id;

    private String bomCode;

    private Long productId;

    private Long routeId;

    private String version;

    private BigDecimal yieldRate;

    private Integer status;

    private LocalDate effectiveDate;

    private LocalDate expireDate;

    private Long sourceRdBomId;

    private String remark;
    /**
     * 停用审批在途流程实例 ID；审批中非空，通过后落 DISABLE 并清空
     */
    private String processInstanceId;

}
