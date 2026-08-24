package cn.weitee.erp.module.erp.dal.dataobject.mrp;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/** 工艺路线工序主数据。 */
@TableName("erp_process_route_step")
@KeySequence("erp_process_route_step_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpProcessRouteStepDO extends BaseDO {

    @TableId
    private Long id;
    private Long routeId;
    private Integer stepNo;
    private String stepCode;
    private String stepName;
    private Long workCenterId;
    private Boolean outsourceFlag;
    private Boolean qcFlag;
    private Boolean reportRequired;
    private Boolean inspectRequired;
    private BigDecimal prepareTime;
    private BigDecimal processTime;
    private BigDecimal moveTime;
    private BigDecimal waitTime;
    private BigDecimal batchSize;
    private Integer sort;
    private Integer status;
    private String remark;
}
