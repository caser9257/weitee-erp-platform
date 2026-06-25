package cn.weitee.erp.module.erp.dal.dataobject.mrp;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@TableName("erp_mrp_plan")
@KeySequence("erp_mrp_plan_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpMrpPlanDO extends BaseDO {

    @TableId
    private Long id;

    private String planNo;

    private String planName;

    private LocalDate planStartDate;

    private LocalDate planEndDate;

    private Integer status;

    private LocalDateTime runTime;

    private Long operatorId;

    private String remark;

}
